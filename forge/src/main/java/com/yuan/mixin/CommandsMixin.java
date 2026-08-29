package com.yuan.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.CommandDispatcher;
import com.yuan.event.YuanDefenseState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Commands.class)
public class CommandsMixin {
    @Redirect(method = "performCommand", at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I",
            remap = false),
            require = 1, expect = 1)
    private int aroundAdministrativeCommand(CommandDispatcher<CommandSourceStack> dispatcher,
                                             ParseResults<CommandSourceStack> results) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!results.getContext().getSource().hasPermission(2)) return dispatcher.execute(results);
        try (YuanDefenseState.Scope ignored = YuanDefenseState.enterAdministrativeCommand()) {
            return dispatcher.execute(results);
        }
    }
}
