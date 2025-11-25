package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.slf4j.Logger;

public class ReloadableServerResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableServerRegistries.Holder fullRegistryHolder;
    private final ReloadableServerResources.ConfigurableRegistryLookup registryLookup;
    private final Commands commands;
    private final RecipeManager recipes;
    private final TagManager tagManager;
    private final ServerAdvancementManager advancements;
    private final ServerFunctionLibrary functionLibrary;

    private ReloadableServerResources(RegistryAccess.Frozen p_206857_, FeatureFlagSet p_250695_, Commands.CommandSelection p_206858_, int p_206859_) {
        this.fullRegistryHolder = new ReloadableServerRegistries.Holder(p_206857_);
        this.registryLookup = new ReloadableServerResources.ConfigurableRegistryLookup(p_206857_);
        this.registryLookup.missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.CREATE_NEW);
        this.recipes = new RecipeManager(this.registryLookup);
        this.tagManager = new TagManager(p_206857_);
        this.commands = new Commands(p_206858_, CommandBuildContext.simple(this.registryLookup, p_250695_));
        this.advancements = new ServerAdvancementManager(this.registryLookup);
        this.functionLibrary = new ServerFunctionLibrary(p_206859_, this.commands.getDispatcher());
    }

    public ServerFunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    public ReloadableServerRegistries.Holder fullRegistries() {
        return this.fullRegistryHolder;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners() {
        return List.of(this.tagManager, this.recipes, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(
        ResourceManager p_248588_,
        LayeredRegistryAccess<RegistryLayer> p_335667_,
        FeatureFlagSet p_250212_,
        Commands.CommandSelection p_249301_,
        int p_251126_,
        Executor p_249136_,
        Executor p_249601_
    ) {
        return ReloadableServerRegistries.reload(p_335667_, p_248588_, p_249136_)
            .thenCompose(
                p_335211_ -> {
                    ReloadableServerResources reloadableserverresources = new ReloadableServerResources(
                        p_335211_.compositeAccess(), p_250212_, p_249301_, p_251126_
                    );
                    return SimpleReloadInstance.create(
                            p_248588_, reloadableserverresources.listeners(), p_249136_, p_249601_, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()
                        )
                        .done()
                        .whenComplete(
                            (p_323178_, p_323179_) -> reloadableserverresources.registryLookup
                                    .missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.FAIL)
                        )
                        .thenApply(p_214306_ -> reloadableserverresources);
                }
            );
    }

    public void updateRegistryTags() {
        this.tagManager.getResult().forEach(p_335204_ -> updateRegistryTags(this.fullRegistryHolder.get(), (TagManager.LoadResult<?>)p_335204_));
        AbstractFurnaceBlockEntity.invalidateCache();
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(RegistryAccess p_206871_, TagManager.LoadResult<T> p_206872_) {
        ResourceKey<? extends Registry<T>> resourcekey = p_206872_.key();
        Map<TagKey<T>, List<Holder<T>>> map = p_206872_.tags()
            .entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(p_214303_ -> TagKey.create(resourcekey, p_214303_.getKey()), p_214312_ -> List.copyOf(p_214312_.getValue())));
        p_206871_.registryOrThrow(resourcekey).bindTags(map);
    }

    static class ConfigurableRegistryLookup implements HolderLookup.Provider {
        private final RegistryAccess registryAccess;
        ReloadableServerResources.MissingTagAccessPolicy missingTagAccessPolicy = ReloadableServerResources.MissingTagAccessPolicy.FAIL;

        ConfigurableRegistryLookup(RegistryAccess p_324146_) {
            this.registryAccess = p_324146_;
        }

        public void missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy p_324138_) {
            this.missingTagAccessPolicy = p_324138_;
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_323818_) {
            return this.registryAccess.registry(p_323818_).map(p_324050_ -> this.createDispatchedLookup(p_324050_.asLookup(), p_324050_.asTagAddingLookup()));
        }

        private <T> HolderLookup.RegistryLookup<T> createDispatchedLookup(
            final HolderLookup.RegistryLookup<T> p_324196_, final HolderLookup.RegistryLookup<T> p_323710_
        ) {
            return new HolderLookup.RegistryLookup.Delegate<T>() {
                @Override
                public HolderLookup.RegistryLookup<T> parent() {
                    return switch (ConfigurableRegistryLookup.this.missingTagAccessPolicy) {
                        case CREATE_NEW -> p_323710_;
                        case FAIL -> p_324196_;
                    };
                }
            };
        }
    }

    static enum MissingTagAccessPolicy {
        CREATE_NEW,
        FAIL;
    }
}
