package io.wispforest.owo.mixin.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.text.LanguageAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import java.util.function.BiConsumer;
import net.minecraft.class_2477;
import net.minecraft.class_3518;
import net.minecraft.class_5250;
import net.minecraft.class_8824;

@Mixin(class_2477.class)
public class LanguageMixin {

    @Unique private static boolean skipNext;

    @Redirect(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;asString(Lcom/google/gson/JsonElement;Ljava/lang/String;)Ljava/lang/String;"))
    private static String skipIfObjectOrArray(JsonElement el, String str, InputStream inputStream, BiConsumer<String, String> entryConsumer) {
        if (!el.isJsonPrimitive() && LanguageAccess.textConsumer != null) {
            skipNext = true;

            class_5250 text = (class_5250) class_8824.field_46597.parse(JsonOps.INSTANCE, el).getOrThrow(JsonParseException::new);
            LanguageAccess.textConsumer.accept(str, text);

            return "";
        } else if (el.isJsonPrimitive()) {
            skipNext = false;
            return class_3518.method_15287(el, str);
        } else {
            skipNext = true;
            return "";
        }
    }

    @Redirect(method = "load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void doSkip(BiConsumer<Object, Object> biConsumer, Object t, Object u) {
        if (!skipNext)
            biConsumer.accept(t, u);
    }
}
