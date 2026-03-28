package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.class_1799;
import net.minecraft.class_2168;
import net.minecraft.class_2232;
import net.minecraft.class_2287;
import net.minecraft.class_2960;
import net.minecraft.class_3039;
import net.minecraft.class_7157;
import net.minecraft.class_9279;
import net.minecraft.class_9334;

import static net.minecraft.class_2170.method_9244;
import static net.minecraft.class_2170.method_9247;

public class MakeLootContainerCommand {

    public static void register(CommandDispatcher<class_2168> dispatcher, class_7157 registryAccess) {
        dispatcher.register(method_9247("make-loot-container")
                .then(method_9244("item", class_2287.method_9776(registryAccess))
                        .then(method_9244("loot_table", class_2232.method_9441())
                                .suggests(class_3039.field_13605)
                                .executes(MakeLootContainerCommand::execute))));
    }

    private static int execute(CommandContext<class_2168> context) throws CommandSyntaxException {
        var targetStack = class_2287.method_9777(context, "item").method_9781(1, false);
        var tableId = class_2232.method_9443(context, "loot_table");

        var blockEntityTag = targetStack.method_57825(class_9334.field_49611, class_9279.field_49302);
        blockEntityTag = blockEntityTag.method_57451(x -> {
            x.method_10582("LootTable", tableId.toString());
        });
        targetStack.method_57379(class_9334.field_49611, blockEntityTag);

        context.getSource().method_44023().method_31548().method_7398(targetStack);

        return 0;
    }
}
