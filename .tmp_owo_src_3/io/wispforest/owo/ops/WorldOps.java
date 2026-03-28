package io.wispforest.owo.ops;

import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2343;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2783;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of common operations done on {@link class_1937}
 */
public final class WorldOps {

    private WorldOps() {}

    /**
     * Break the specified block with the given item
     *
     * @param world     The world the block is in
     * @param pos       The position of the block to break
     * @param breakItem The item to break the block with
     */
    public static void breakBlockWithItem(class_1937 world, class_2338 pos, class_1799 breakItem) {
        breakBlockWithItem(world, pos, breakItem, null);
    }

    /**
     * Break the specified block with the given item
     *
     * @param world          The world the block is in
     * @param pos            The position of the block to break
     * @param breakItem      The item to break the block with
     * @param breakingEntity The entity which is breaking the block
     */
    public static void breakBlockWithItem(class_1937 world, class_2338 pos, class_1799 breakItem, @Nullable class_1297 breakingEntity) {
        class_2586 breakEntity = world.method_8320(pos).method_26204() instanceof class_2343 ? world.method_8321(pos) : null;
        class_2248.method_9511(world.method_8320(pos), world, pos, breakEntity, breakingEntity, breakItem);
        world.method_8651(pos, false, breakingEntity);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server. Volume and pitch default to 1
     *
     * @param world    The world to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     */
    public static void playSound(class_1937 world, class_243 pos, class_3414 sound, class_3419 category) {
        playSound(world, class_2338.method_49638(pos), sound, category, 1, 1);
    }

    public static void playSound(class_1937 world, class_2338 pos, class_3414 sound, class_3419 category) {
        playSound(world, pos, sound, category, 1, 1);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server
     *
     * @param world    The world to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     * @param volume   The volume to play the sound at
     * @param pitch    The pitch, or speed, to play the sound at
     */
    public static void playSound(class_1937 world, class_243 pos, class_3414 sound, class_3419 category, float volume, float pitch) {
        world.method_8396(null, class_2338.method_49638(pos), sound, category, volume, pitch);
    }

    public static void playSound(class_1937 world, class_2338 pos, class_3414 sound, class_3419 category, float volume, float pitch) {
        world.method_8396(null, pos, sound, category, volume, pitch);
    }

    /**
     * Causes a block update at the given position, if {@code world}
     * is an instance of {@link class_3218}
     *
     * @param world The target world
     * @param pos   The target position
     */
    public static void updateIfOnServer(class_1937 world, class_2338 pos) {
        if (!(world instanceof class_3218 serverWorld)) return;
        serverWorld.method_14178().method_14128(pos);
    }

    /**
     * Same as {@link WorldOps#teleportToWorld(class_3222, class_3218, class_243, float, float)} but defaults
     * to {@code 0} for {@code pitch} and {@code yaw}
     */
    public static void teleportToWorld(class_3222 player, class_3218 target, class_243 pos) {
        teleportToWorld(player, target, pos, 0, 0);
    }

    /**
     * Teleports the given player to the given world, syncing all the annoying data
     * like experience and status effects that minecraft doesn't
     *
     * @param player The player to teleport
     * @param target The world to teleport to
     * @param pos    The target position
     * @param yaw    The target yaw
     * @param pitch  The target pitch
     */
    public static void teleportToWorld(class_3222 player, class_3218 target, class_243 pos, float yaw, float pitch) {
        player.method_14251(target, pos.field_1352, pos.field_1351, pos.field_1350, yaw, pitch);
        player.method_7255(0);

        player.method_6026().forEach(effect -> {
            player.field_13987.method_14364(new class_2783(player.method_5628(), effect, false));
        });
    }

}
