package net.neoforged.fml.common.Mod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Minimal compatibility annotation to satisfy imports referencing Mod.EventBusSubscriber */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EventBusSubscriber {
    public enum Bus { MOD, FORGE }
    Bus bus() default Bus.MOD;
}
