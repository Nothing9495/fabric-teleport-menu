package io.wispforest.owo.offline;

import java.util.Map;
import net.minecraft.class_167;
import net.minecraft.class_2960;
import net.minecraft.class_8779;

/**
 * A utility class that allows easy
 * modification of the advancement data
 * of offline players
 *
 * @author BasiqueEvangelist
 */
public class OfflineAdvancementState {

    private final Map<class_2960, class_167> advancementData;

    OfflineAdvancementState(Map<class_2960, class_167> advancementData) {
        this.advancementData = advancementData;
    }

    /**
     * @return The raw data this state holds
     */
    public Map<class_2960, class_167> advancementData() {
        return advancementData;
    }

    /**
     * Either obtains or creates an entry for
     * the given advancement
     *
     * @param advancement The target advancement
     * @return The progress of the targeted advancement
     */
    public class_167 getOrAddProgress(class_8779 advancement) {
        return advancementData.computeIfAbsent(advancement.comp_1919(), id -> {
            class_167 progress = new class_167();
            progress.method_727(advancement.comp_1920().comp_1916());
            return progress;
        });
    }

    /**
     * Grants the given advancement to the player
     * this state refers to
     *
     * @param advancement The advancement to grant
     */
    public void grant(class_8779 advancement) {
        class_167 progress = getOrAddProgress(advancement);
        for (String criterion : progress.method_731()) {
            progress.method_743(criterion);
        }
    }

    /**
     * Revokes the given advancement from the player
     * this state refers to
     *
     * @param advancement The advancement to revoke
     */
    public void revoke(class_8779 advancement) {
        class_167 progress = getOrAddProgress(advancement);
        for (String criterion : progress.method_734()) {
            progress.method_729(criterion);
        }
    }
}
