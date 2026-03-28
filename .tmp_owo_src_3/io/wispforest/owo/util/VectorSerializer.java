package io.wispforest.owo.util;

import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2540;
import net.minecraft.nbt.*;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;

/**
 * Utility class for reading and storing {@link class_243} and
 * {@link Vector3f} from and into {@link net.minecraft.class_2487}
 */
public final class VectorSerializer {

    private VectorSerializer() {}

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @param vec3d The vector to serialize
     * @return {@code nbt}
     */
    public static class_2487 put(class_2487 nbt, String key, class_243 vec3d) {

        class_2499 vectorArray = new class_2499();
        vectorArray.add(class_2489.method_23241(vec3d.field_1352));
        vectorArray.add(class_2489.method_23241(vec3d.field_1351));
        vectorArray.add(class_2489.method_23241(vec3d.field_1350));

        nbt.method_10566(key, vectorArray);

        return nbt;
    }

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3f The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static class_2487 putf(class_2487 nbt, String key, Vector3f vec3f) {

        class_2499 vectorArray = new class_2499();
        vectorArray.add(class_2494.method_23244(vec3f.x));
        vectorArray.add(class_2494.method_23244(vec3f.y));
        vectorArray.add(class_2494.method_23244(vec3f.z));

        nbt.method_10566(key, vectorArray);

        return nbt;
    }

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3i The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static class_2487 puti(class_2487 nbt, String key, class_2382 vec3i) {

        nbt.method_10572(key, List.of(vec3i.method_10263(), vec3i.method_10264(), vec3i.method_10260()));

        return nbt;
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static class_243 get(class_2487 nbt, String key) {

        class_2499 vectorArray = nbt.method_10554(key, class_2520.field_33256);
        double x = vectorArray.method_10611(0);
        double y = vectorArray.method_10611(1);
        double z = vectorArray.method_10611(2);

        return new class_243(x, y, z);
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vector3f getf(class_2487 nbt, String key) {

        class_2499 vectorArray = nbt.method_10554(key, class_2520.field_33255);
        float x = vectorArray.method_10604(0);
        float y = vectorArray.method_10604(1);
        float z = vectorArray.method_10604(2);

        return new Vector3f(x, y, z);
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static class_2382 geti(class_2487 nbt, String key) {

        int[] vectorArray = nbt.method_10561(key);
        int x = vectorArray[0];
        int y = vectorArray[1];
        int z = vectorArray[2];

        return new class_2382(x, y, z);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3d  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void write(class_2540 buffer, class_243 vec3d) {
        buffer.method_52940(vec3d.field_1352);
        buffer.method_52940(vec3d.field_1351);
        buffer.method_52940(vec3d.field_1350);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3f  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void writef(class_2540 buffer, Vector3f vec3f) {
        buffer.method_52941(vec3f.x);
        buffer.method_52941(vec3f.y);
        buffer.method_52941(vec3f.z);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3i  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void writei(class_2540 buffer, class_2382 vec3i) {
        buffer.method_53002(vec3i.method_10263());
        buffer.method_53002(vec3i.method_10264());
        buffer.method_53002(vec3i.method_10260());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static class_243 read(class_2540 buffer) {
        return new class_243(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vector3f readf(class_2540 buffer) {
        return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static class_2382 readi(class_2540 buffer) {
        return new class_2382(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }
}
