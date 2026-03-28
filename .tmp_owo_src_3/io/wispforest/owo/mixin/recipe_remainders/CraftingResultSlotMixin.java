package io.wispforest.owo.mixin.recipe_remainders;

import net.minecraft.class_1657;
import net.minecraft.class_1734;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_9694;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(class_1734.class)
public class CraftingResultSlotMixin {

    @Shadow
    @Final
    private class_1657 player;

    @Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/RecipeInputInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fixRemainderStacking(class_1657 player, class_1799 stack, CallbackInfo ci, class_9694.class_9765 positioned, class_9694 craftingRecipeInput, int i, int j, class_2371 defaultedList, int k, int l, int m, class_1799 itemStack, class_1799 remainderStack) {
        if (remainderStack.method_7947() > remainderStack.method_7914()) {
            int excess = remainderStack.method_7947() - remainderStack.method_7914();
            remainderStack.method_7934(excess);

            var insertStack = remainderStack.method_7972();
            insertStack.method_7939(excess);

            if (!this.player.method_31548().method_7394(insertStack)) {
                this.player.method_7328(insertStack, false);
            }
        }
    }

}
