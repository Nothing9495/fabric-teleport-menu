package io.wispforest.owo.ui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class UISounds {

    public static final class_3414 UI_INTERACTION = class_3414.method_47908(class_2960.method_60655("owo", "ui.owo.interaction"));

    private UISounds() {}

    @Environment(EnvType.CLIENT)
    public static void playButtonSound() {
        class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1));
    }

    @Environment(EnvType.CLIENT)
    public static void playInteractionSound() {
        class_310.method_1551().method_1483().method_4873(class_1109.method_4758(UI_INTERACTION, 1));
    }

}
