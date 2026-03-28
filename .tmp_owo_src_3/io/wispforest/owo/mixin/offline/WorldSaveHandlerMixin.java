package io.wispforest.owo.mixin.offline;

import io.wispforest.owo.offline.DataSavedEvents;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_29;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(class_29.class)
public class WorldSaveHandlerMixin {
    @Inject(method = "savePlayerData", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;createTempFile(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPlayerDataSaved(class_1657 player, CallbackInfo ci, class_2487 tag) {
        DataSavedEvents.PLAYER_DATA.invoker().onSaved(player.method_5667(), tag);
    }
}
