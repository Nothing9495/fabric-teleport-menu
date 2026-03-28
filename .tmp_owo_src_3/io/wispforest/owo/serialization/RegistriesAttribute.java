package io.wispforest.owo.serialization;

import io.wispforest.endec.SerializationAttribute;
import io.wispforest.owo.mixin.CachedRegistryInfoGetterAccessor;
import net.minecraft.class_5455;
import net.minecraft.class_6903;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistriesAttribute implements SerializationAttribute.Instance {

    public static final SerializationAttribute.WithValue<RegistriesAttribute> REGISTRIES = SerializationAttribute.withValue("registries");

    private final class_6903.class_7863 infoGetter;
    private final @Nullable class_5455 registryManager;

    private RegistriesAttribute(class_6903.class_7863 infoGetter, @Nullable class_5455 registryManager) {
        this.infoGetter = infoGetter;
        this.registryManager = registryManager;
    }

    public static RegistriesAttribute of(class_5455 registryManager) {
        return new RegistriesAttribute(
                new class_6903.class_9683(registryManager),
                registryManager
        );
    }

    @ApiStatus.Internal
    public static RegistriesAttribute tryFromCachedInfoGetter(class_6903.class_7863 lookup) {
        return (lookup instanceof class_6903.class_9683 cachedGetter)
                ? fromCachedInfoGetter(cachedGetter)
                : fromInfoGetter(lookup);
    }

    public static RegistriesAttribute fromCachedInfoGetter(class_6903.class_9683 cachedGetter) {
        class_5455 registryManager = null;

        if(((CachedRegistryInfoGetterAccessor) (Object) cachedGetter).owo$getRegistriesLookup() instanceof class_5455 drm) {
            registryManager = drm;
        }

        return new RegistriesAttribute(cachedGetter, registryManager);
    }

    public static RegistriesAttribute fromInfoGetter(class_6903.class_7863 lookup) {
        return new RegistriesAttribute(lookup, null);
    }

    public class_6903.class_7863 infoGetter() {
        return this.infoGetter;
    }

    public boolean hasRegistryManager() {
        return this.registryManager != null;
    }

    public @NotNull class_5455 registryManager() {
        if (!this.hasRegistryManager()) {
            throw new IllegalStateException("This instance of RegistriesAttribute does not supply a DynamicRegistryManager");
        }

        return this.registryManager;
    }

    @Override
    public SerializationAttribute attribute() {
        return REGISTRIES;
    }

    @Override
    public Object value() {
        return this;
    }
}
