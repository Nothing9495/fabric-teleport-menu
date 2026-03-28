package io.wispforest.owo.network;

import net.minecraft.class_3222;
import net.minecraft.class_3244;
import net.minecraft.server.MinecraftServer;

public record ServerAccess(class_3222 player) implements
        OwoNetChannel.EnvironmentAccess<class_3222, MinecraftServer, class_3244> {

    @Override
    public MinecraftServer runtime() {
        return player.field_13995;
    }

    @Override
    public class_3244 netHandler() {
        return player.field_13987;
    }
}
