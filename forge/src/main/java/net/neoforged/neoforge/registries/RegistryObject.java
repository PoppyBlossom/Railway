package net.neoforged.neoforge.registries;

public class RegistryObject<T> {
    private final T value;
    public RegistryObject(T value) { this.value = value; }
    public T get() { return value; }
}
