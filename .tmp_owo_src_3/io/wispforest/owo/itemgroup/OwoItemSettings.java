package io.wispforest.owo.itemgroup;

import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1761;
import net.minecraft.class_1792;
import net.minecraft.class_1814;
import net.minecraft.class_1937;
import java.util.function.BiConsumer;

/**
 * @deprecated Replaced with {@link OwoItemSettingsExtension}.
 */
@Deprecated(forRemoval = true)
public class OwoItemSettings extends class_1792.class_1793 {
    public OwoItemSettings group(ItemGroupReference ref) {
        return (OwoItemSettings) super.group(ref);
    }

    /**
     * @param group The item group this item should appear in
     */
    public OwoItemSettings group(OwoItemGroup group) {
        return (OwoItemSettings) super.group(group);
    }

    public OwoItemGroup group() {
        return super.group();
    }

    public OwoItemSettings tab(int tab) {
        return (OwoItemSettings) super.tab(tab);
    }

    public int tab() {
        return super.tab();
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    public OwoItemSettings stackGenerator(BiConsumer<class_1792, class_1761.class_7704> generator) {
        return (OwoItemSettings) super.stackGenerator(generator);
    }

    public BiConsumer<class_1792, class_1761.class_7704> stackGenerator() {
        return super.stackGenerator();
    }

    /**
     * Automatically increment {@link net.minecraft.class_3468#field_15372}
     * for this item every time {@link class_1792#method_7836(class_1937, class_1657, class_1268)}
     * returns an accepted result
     */
    public OwoItemSettings trackUsageStat() {
        return (OwoItemSettings) super.trackUsageStat();
    }

    public boolean shouldTrackUsageStat() {
        return super.shouldTrackUsageStat();
    }

    @Override
    public OwoItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
        return (OwoItemSettings) super.equipmentSlot(equipmentSlotProvider);
    }

    @Override
    public OwoItemSettings customDamage(CustomDamageHandler handler) {
        return (OwoItemSettings) super.customDamage(handler);
    }

    @Override
    public OwoItemSettings method_7889(int maxCount) {
        return (OwoItemSettings) super.method_7889(maxCount);
    }

    @Override
    public OwoItemSettings method_7895(int maxDamage) {
        return (OwoItemSettings) super.method_7895(maxDamage);
    }

    @Override
    public OwoItemSettings method_7896(class_1792 recipeRemainder) {
        return (OwoItemSettings) super.method_7896(recipeRemainder);
    }

    @Override
    public OwoItemSettings method_7894(class_1814 rarity) {
        return (OwoItemSettings) super.method_7894(rarity);
    }

    @Override
    public OwoItemSettings method_24359() {
        return (OwoItemSettings) super.method_24359();
    }
}