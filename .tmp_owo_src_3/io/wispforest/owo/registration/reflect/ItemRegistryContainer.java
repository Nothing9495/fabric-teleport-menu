package io.wispforest.owo.registration.reflect;

import net.minecraft.class_1792;
import net.minecraft.class_2378;
import net.minecraft.class_7923;

public interface ItemRegistryContainer extends AutoRegistryContainer<class_1792> {
    @Override
    default class_2378<class_1792> getRegistry() {
        return class_7923.field_41178;
    }

    @Override
    default Class<class_1792> getTargetFieldType() {
        return class_1792.class;
    }
}
