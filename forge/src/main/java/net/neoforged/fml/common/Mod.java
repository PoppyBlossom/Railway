package net.neoforged.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Minimal Mod annotation placeholder with nested EventBusSubscriber for compile-time.
 * Real behaviour is provided by NeoForge at runtime.
 */
public @interface Mod {
    String value() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface EventBusSubscriber {
        public enum Bus { MOD, FORGE }
        Bus bus() default Bus.MOD;

        // Some usages call @EventBusSubscriber(Dist.CLIENT) or @EventBusSubscriber(value = Dist.CLIENT,...)
        net.neoforged.neoforge.api.distmarker.Dist value() default net.neoforged.neoforge.api.distmarker.Dist.CLIENT;
    }
}
