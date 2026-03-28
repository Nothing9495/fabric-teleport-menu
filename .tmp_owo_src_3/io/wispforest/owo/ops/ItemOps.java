package io.wispforest.owo.ops;

import io.wispforest.owo.Owo;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_7225;

/**
 * A collection of common checks and operations done on {@link class_1799}
 */
public final class ItemOps {

    private ItemOps() {
    }

    /**
     * Checks if stack one can stack onto stack two
     *
     * @param base     The base stack
     * @param addition The stack to be added
     * @return {@code true} if addition can stack onto base
     */
    public static boolean canStack(class_1799 base, class_1799 addition) {
        return base.method_7960() || (canIncreaseBy(base, addition.method_7947()) && class_1799.method_31577(base, addition));
    }

    /**
     * Checks if a stack can increase
     *
     * @param stack The stack to test
     * @return stack.getCount() &lt; stack.getMaxCount()
     */
    public static boolean canIncrease(class_1799 stack) {
        return stack.method_7946() && stack.method_7947() < stack.method_7914();
    }

    /**
     * Checks if a stack can increase by the given amount
     *
     * @param stack The stack to test
     * @param by    The amount to test for
     * @return {@code true} if the stack can increase by the given amount
     */
    public static boolean canIncreaseBy(class_1799 stack, int by) {
        return stack.method_7946() && stack.method_7947() + by <= stack.method_7914();
    }

    /**
     * Returns a copy of the given stack with count set to 1
     */
    public static class_1799 singleCopy(class_1799 stack) {
        class_1799 copy = stack.method_7972();
        copy.method_7939(1);
        return copy;
    }
    
    /**
     * Decrements the stack
     *
     * @param stack The stack to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(class_1799 stack) {
        return emptyAwareDecrement(stack, 1);
    }

    /**
     * Decrements the stack
     *
     * @param stack The stack to decrement
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean emptyAwareDecrement(class_1799 stack, int amount) {
        stack.method_7934(amount);
        return !stack.method_7960();
    }

    /**
     * Decrements the stack in the players hand and replaces it with {@link class_1799#field_8037}
     * if the result would be an empty stack
     *
     * @param player The player to operate on
     * @param hand   The hand to affect
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean decrementPlayerHandItem(class_1657 player, class_1268 hand) {
        return decrementPlayerHandItem(player, hand, 1);
    }

    /**
     * Decrements the stack in the players hand and replaces it with {@link class_1799#field_8037}
     * if the result would be an empty stack
     *
     * @param player The player to operate on
     * @param hand The hand to affect
     * @param amount The amount to decrement
     * @return {@code false} if the stack is empty after the operation
     */
    public static boolean decrementPlayerHandItem(class_1657 player, class_1268 hand, int amount) {
        var stack = player.method_5998(hand);
        if (!player.method_7337()) {
            if (!emptyAwareDecrement(stack, amount)) player.method_6122(hand, class_1799.field_8037);
        }
        return !stack.method_7960();
    }

    /**
     * Stores the given ItemStack with the specified key
     * into the given nbt compound
     *
     * @param stack The stack to store
     * @param nbt   The nbt compound to write to
     * @param key   The key to prefix the stack with
     */
    public static void store(class_7225.class_7874 registries, class_1799 stack, class_2487 nbt, String key) {
        if (stack.method_7960()) return;

        nbt.method_10566(key, stack.method_57358(registries));
    }

    /**
     * Loads the ItemStack stored at the specified key
     * in the given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key to load from
     * @return The deserialized stack
     */
    public static class_1799 get(class_7225.class_7874 registries, class_2487 nbt, String key) {
        if (!nbt.method_10573(key, class_2520.field_33260)) return class_1799.field_8037;

        var stackNbt = nbt.method_10562(key);
        return class_1799.method_57359(registries, stackNbt);
    }

}
