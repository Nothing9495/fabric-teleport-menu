package io.wispforest.owo.offline;

import io.wispforest.owo.Owo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import net.minecraft.class_156;
import net.minecraft.class_2487;
import net.minecraft.class_2505;
import net.minecraft.class_2507;
import net.minecraft.class_3551;
import net.minecraft.class_4284;
import net.minecraft.class_5218;

/**
 * Allows retrieving and editing the saved
 * NBT data of a player
 * <p>
 * <b>This only works while the given player is offline</b>
 *
 * @author BasiqueEvangelist
 */
public final class OfflineDataLookup {

    private OfflineDataLookup() {}

    /**
     * Saves the given NBT tag
     * for the given player to disk
     *
     * @param player The player to modify
     * @param nbt    The data to save
     */
    public static void put(UUID player, class_2487 nbt) {
        DataSavedEvents.PLAYER_DATA.invoker().onSaved(player, nbt);

        try {
            var savedPlayersPath = Owo.currentServer().method_27050(class_5218.field_24182);
            var file = Files.createTempFile(savedPlayersPath, player.toString() + "-", ".dat");
            class_2507.method_30614(nbt, file);
            var newDataFile = savedPlayersPath.resolve(player + ".dat");
            var oldDataFile = savedPlayersPath.resolve(player + ".dat_old");
            class_156.method_30626(newDataFile, file, oldDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the playerdata
     * of the given player from disk
     *
     * @param player The player to query
     * @return The saved playerdata, or {@code null} if none is saved
     */
    public static @Nullable class_2487 get(UUID player) {
        try {
            Path savedPlayersPath = Owo.currentServer().method_27050(class_5218.field_24182);
            Path savedDataPath = savedPlayersPath.resolve(player.toString() + ".dat");
            class_2487 rawNbt = class_2507.method_30613(savedDataPath, class_2505.method_53898());
            int dataVersion = rawNbt.method_10573("DataVersion", 3) ? rawNbt.method_10550("DataVersion") : -1;
            return class_4284.field_19213.method_48130(class_3551.method_15450(), rawNbt, dataVersion);
        } catch (IOException e) {
            Owo.LOGGER.error("Couldn't get player data for offline player {}", player, e);
            return null;
        }
    }

    /**
     * Edits the saved data of the given player
     * with the given editing function
     *
     * @param player The player to target
     * @param editor The function to apply to the saved data
     */
    public static void edit(UUID player, Function<class_2487, class_2487> editor) {
        put(player, editor.apply(get(player)));
    }

    /**
     * @return The UUID of every player that has saved playerdata
     */
    public static List<UUID> savedPlayers() {
        List<UUID> list = new ArrayList<>();
        Path savedPlayersPath = Owo.currentServer().method_27050(class_5218.field_24182);

        if (!Files.isDirectory(savedPlayersPath))
            return Collections.emptyList();

        try {
            Iterator<Path> iterator = Files.list(savedPlayersPath).iterator();
            while (iterator.hasNext()) {
                Path savedPlayerFile = iterator.next();

                if (Files.isDirectory(savedPlayerFile) || !savedPlayerFile.toString().endsWith(".dat")) {
                    continue;
                }

                try {
                    String filename = savedPlayerFile.getFileName().toString();
                    String uuidStr = filename.substring(0, filename.lastIndexOf('.'));
                    UUID uuid = UUID.fromString(uuidStr);
                    list.add(uuid);
                } catch (IllegalArgumentException iae) {
                    Owo.LOGGER.error("Encountered invalid UUID in playerdata directory", iae);
                }
            }
        } catch (IOException e) {
            Owo.LOGGER.error("Couldn't list offline player UUIDs", e);
            throw new RuntimeException(e);
        }

        return list;
    }
}
