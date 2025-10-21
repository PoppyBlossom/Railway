/*
 * Compatibility shim for LazyOptional removed in NeoForge 1.21+
 * 
 * This is a minimal implementation to enable compilation during the porting process.
 * The new NeoForge capability system does not use LazyOptional - capabilities are 
 * now directly returned or null.
 * 
 * TODO: Refactor all capability code to use the new NeoForge 1.21+ capability API
 * and remove this shim once the migration is complete.
 */
package net.neoforged.neoforge.common.util;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Compatibility shim for net.neoforged.neoforge.common.util.LazyOptional.
 * This is NOT a full reimplementation and should be replaced with proper capability API usage.
 */
public final class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null, null, false, true);

    @Nullable
    private T value;
    @Nullable
    private Supplier<? extends T> supplier;
    private boolean resolved;
    private boolean valid;

    private LazyOptional(@Nullable T value, @Nullable Supplier<? extends T> supplier, boolean valid, boolean resolved) {
        this.value = value;
        this.supplier = supplier;
        this.valid = valid;
        this.resolved = resolved;
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    public static <T> LazyOptional<T> of(Supplier<T> supplier) {
        if (supplier == null) throw new NullPointerException("Supplier must not be null for LazyOptional.of(supplier)");
        // Start valid, resolve lazily on first access
        return new LazyOptional<>(null, supplier, true, false);
    }

    public static <T> LazyOptional<T> of(T value) {
        if (value == null) {
            throw new NullPointerException("Value must not be null for LazyOptional.of()");
        }
        return new LazyOptional<>(value, null, true, true);
    }

    public boolean isPresent() {
        resolveIfNeeded();
        return valid && value != null;
    }

    public T orElse(T other) {
        resolveIfNeeded();
        return isPresent() ? value : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        resolveIfNeeded();
        return isPresent() ? value : supplier.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        resolveIfNeeded();
        if (!isPresent()) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        resolveIfNeeded();
        if (isPresent()) {
            consumer.accept(value);
        }
    }

    public <U> LazyOptional<U> map(Function<? super T, ? extends U> mapper) {
        resolveIfNeeded();
        if (!isPresent()) {
            return empty();
        }
        return new LazyOptional<>(null, () -> mapper.apply(value), true, false);
    }

    public <U> LazyOptional<U> lazyMap(Function<? super T, ? extends LazyOptional<U>> mapper) {
        resolveIfNeeded();
        if (!isPresent()) {
            return empty();
        }
        // Wrap lazily to avoid premature resolution
        return new LazyOptional<>(null, () -> {
            LazyOptional<U> next = mapper.apply(value);
            return next.getOrNull();
        }, true, false);
    }

    @SuppressWarnings("unchecked")
    public <U> LazyOptional<U> cast() {
        return (LazyOptional<U>) this;
    }

    public void invalidate() {
        this.valid = false;
        this.value = null;
        this.supplier = null;
        this.resolved = true;
    }

    public Optional<T> resolve() {
        resolveIfNeeded();
        return isPresent() ? Optional.of(value) : Optional.empty();
    }

    public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
        // Compatibility shim - listeners not implemented
        // In new API, use capability invalidation events instead
    }

    private void resolveIfNeeded() {
        if (!valid || resolved)
            return;
        if (supplier != null) {
            T v = supplier.get();
            this.value = v;
            this.supplier = null;
            this.resolved = true;
            if (v == null) {
                this.valid = false;
            }
        } else {
            this.resolved = true;
        }
    }

    @Nullable
    private T getOrNull() {
        resolveIfNeeded();
        return value;
    }

    @FunctionalInterface
    public interface NonNullConsumer<T> {
        void accept(T t);
    }
}
