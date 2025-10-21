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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Compatibility shim for net.neoforged.neoforge.common.util.LazyOptional.
 * This is NOT a full reimplementation and should be replaced with proper capability API usage.
 */
public final class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);

    @Nullable
    private T value;
    private boolean valid;

    private LazyOptional(@Nullable T value) {
        this.value = value;
        this.valid = value != null;
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    public static <T> LazyOptional<T> of(Supplier<T> supplier) {
        return new LazyOptional<>(supplier.get());
    }

    public static <T> LazyOptional<T> of(T value) {
        if (value == null) {
            throw new NullPointerException("Value must not be null for LazyOptional.of()");
        }
        return new LazyOptional<>(value);
    }

    public boolean isPresent() {
        return valid && value != null;
    }

    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (!isPresent()) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(value);
        }
    }

    public <U> LazyOptional<U> map(Function<? super T, ? extends U> mapper) {
        if (!isPresent()) {
            return empty();
        }
        return new LazyOptional<>(mapper.apply(value));
    }

    public <U> LazyOptional<U> lazyMap(Function<? super T, ? extends LazyOptional<U>> mapper) {
        if (!isPresent()) {
            return empty();
        }
        return mapper.apply(value);
    }

    @SuppressWarnings("unchecked")
    public <U> LazyOptional<U> cast() {
        return (LazyOptional<U>) this;
    }

    public void invalidate() {
        this.valid = false;
        this.value = null;
    }

    public Optional<T> resolve() {
        return isPresent() ? Optional.of(value) : Optional.empty();
    }

    public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
        // Compatibility shim - listeners not implemented
        // In new API, use capability invalidation events instead
    }

    @FunctionalInterface
    public interface NonNullConsumer<T> {
        void accept(T t);
    }
}
