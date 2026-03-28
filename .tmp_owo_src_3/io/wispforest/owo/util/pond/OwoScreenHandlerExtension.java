package io.wispforest.owo.util.pond;

import io.wispforest.owo.client.screens.ScreenInternals;
import net.minecraft.class_1657;

public interface OwoScreenHandlerExtension {
    void owo$attachToPlayer(class_1657 player);

    void owo$readPropertySync(ScreenInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(ScreenInternals.LocalPacket packet, boolean clientbound);
}
