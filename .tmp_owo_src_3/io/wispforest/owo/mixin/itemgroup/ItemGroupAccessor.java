package io.wispforest.owo.mixin.itemgroup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.class_1761;
import net.minecraft.class_1799;
import net.minecraft.class_2561;

@Mixin(class_1761.class)
public interface ItemGroupAccessor {

    @Accessor("entryCollector")
    class_1761.class_7914 owo$getEntryCollector();

    @Mutable
    @Accessor("entryCollector")
    void owo$setEntryCollector(class_1761.class_7914 collector);

    @Accessor("searchTabStacks")
    void owo$setSearchTabStacks(Set<class_1799> searchTabStacks);

    @Mutable
    @Accessor("displayName")
    void owo$setDisplayName(class_2561 displayName);

    @Mutable
    @Accessor("column")
    void owo$setColumn(int column);

    @Mutable
    @Accessor("row")
    void owo$setRow(class_1761.class_7915 row);
}
