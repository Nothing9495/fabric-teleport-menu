package io.wispforest.owo.mixin.recipe_remainders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.util.RecipeRemainderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1860;
import net.minecraft.class_1863;
import net.minecraft.class_1937;
import net.minecraft.class_2371;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3518;
import net.minecraft.class_3695;
import net.minecraft.class_3956;
import net.minecraft.class_6880;
import net.minecraft.class_8786;
import net.minecraft.class_9695;

@Mixin(class_1863.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeEntry;<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/recipe/Recipe;)V"))
    private void deserializeRecipeSpecificRemainders(Map<class_2960, JsonElement> map, class_3300 resourceManager, class_3695 profiler, CallbackInfo ci, @Local Map.Entry<class_2960, JsonElement> entry) {
        var json = entry.getValue().getAsJsonObject();
        if (!json.has("owo:remainders")) return;

        var remainders = new HashMap<class_1792, class_1799>();
        for (var remainderEntry : json.getAsJsonObject("owo:remainders").entrySet()) {
            var item = class_3518.method_15256(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

            if (remainderEntry.getValue().isJsonObject()) {
                var remainderStack = class_1799.field_24671.parse(JsonOps.INSTANCE, remainderEntry.getValue().getAsJsonObject()).getOrThrow(JsonParseException::new);
                remainders.put(item.comp_349(), remainderStack);
            } else {
                var remainderItem = class_3518.method_15256(remainderEntry.getValue(), "item");
                remainders.put(item.comp_349(), new class_1799(remainderItem));
            }
        }

        if (remainders.isEmpty()) return;
        RecipeRemainderStorage.store(entry.getKey(), remainders);
    }

    @Inject(method = "getRemainingStacks", at = @At(value = "RETURN", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private <I extends class_9695, R extends class_1860<I>> void addRecipeSpecificRemainders(class_3956<R> type, I inventory, class_1937 world, CallbackInfoReturnable<class_2371<class_1799>> cir, Optional<class_8786<R>> optional) {
        if (optional.isEmpty() || !RecipeRemainderStorage.has(optional.get().comp_1932())) return;

        var remainders = cir.getReturnValue();
        var owoRemainders = RecipeRemainderStorage.get(optional.get().comp_1932());

        for (int i = 0; i < remainders.size(); ++i) {
            var item = inventory.method_59984(i).method_7909();
            if (!owoRemainders.containsKey(item)) continue;

            remainders.set(i, owoRemainders.get(item).method_7972());
        }
    }
}
