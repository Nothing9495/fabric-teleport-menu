package io.wispforest.owo;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_2520;

public class Bruh {

    public static void main(String[] args) {
        var optionalListEndec = Endec.STRING.optionalOf().listOf();

        var nbt = optionalListEndec.encodeFully(NbtSerializer::of, List.of(Optional.empty(), Optional.of("a"), Optional.empty(), Optional.of("b")));
        System.out.println(nbt);
        System.out.println(optionalListEndec.decodeFully(NbtDeserializer::of, nbt));
    }

}
