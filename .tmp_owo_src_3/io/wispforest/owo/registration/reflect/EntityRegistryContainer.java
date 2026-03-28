package io.wispforest.owo.registration.reflect;

import net.minecraft.class_1299;
import net.minecraft.class_2378;
import net.minecraft.class_7923;

public interface EntityRegistryContainer extends AutoRegistryContainer<class_1299<?>> {

    @Override
    default class_2378<class_1299<?>> getRegistry() {
        return class_7923.field_41177;
    }

    @Override
    default Class<class_1299<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(class_1299.class);
    }
}
