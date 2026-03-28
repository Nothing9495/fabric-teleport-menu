package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public interface BlockRegistryContainer extends AutoRegistryContainer<class_2248> {

    @Override
    default class_2378<class_2248> getRegistry() {
        return class_7923.field_41175;
    }

    @Override
    default Class<class_2248> getTargetFieldType() {
        return class_2248.class;
    }

    @Override
    default void postProcessField(String namespace, class_2248 value, String identifier, Field field) {
        if (field.isAnnotationPresent(NoBlockItem.class)) return;
        class_2378.method_10230(class_7923.field_41178, class_2960.method_60655(namespace, identifier), createBlockItem(value, identifier));
    }

    /**
     * Creates a block item for the given block
     *
     * @param block      The block to create an item for
     * @param identifier The identifier the field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     * @return The created BlockItem instance
     */
    default class_1747 createBlockItem(class_2248 block, String identifier) {
        return new class_1747(block, new class_1792.class_1793());
    }

    /**
     * Declares that the annotated field should not
     * have a block item created for it
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NoBlockItem {}
}
