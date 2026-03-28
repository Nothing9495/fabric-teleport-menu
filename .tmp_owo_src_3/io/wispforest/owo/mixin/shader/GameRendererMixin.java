package io.wispforest.owo.mixin.shader;

import com.mojang.datafixers.util.Pair;
import io.wispforest.owo.shader.GlProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_281;
import net.minecraft.class_5912;
import net.minecraft.class_5944;
import net.minecraft.class_757;

@Mixin(class_757.class)
public class GameRendererMixin {

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    void loadAllTheShaders(class_5912 factory, CallbackInfo ci, List<class_281> stages, List<Pair<class_5944, Consumer<class_5944>>> shadersToLoad) {
        GlProgram.forEachProgram(loader -> shadersToLoad.add(new Pair<>(loader.method_15442().apply(factory), loader.method_15441())));
    }

}
