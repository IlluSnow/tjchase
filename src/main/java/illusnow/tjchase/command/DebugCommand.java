/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package illusnow.tjchase.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class DebugCommand {
    public static final String CLEAR_ACTIONS_MSG_FAIL = TJChase.prefixCommand("debug.reset.actions.fail");
    public static final String CLEAR_ACTIONS_MSG_SUCCESS_SINGLE = TJChase.prefixCommand("debug.reset.actions.success_single");
    public static final String CLEAR_ACTIONS_MSG_SUCCESS_MULTIPLE = TJChase.prefixCommand("debug.reset.actions.success_multiple");
    public static final String CLEAR_GRAVITY_MSG_FAIL = TJChase.prefixCommand("debug.reset.gravity.fail");
    public static final String CLEAR_GRAVITY_MSG_SUCCESS_SINGLE = TJChase.prefixCommand("debug.reset.gravity.success_single");
    public static final String CLEAR_GRAVITY_MSG_SUCCESS_MULTIPLE = TJChase.prefixCommand("debug.reset.gravity.success_multiple");

    private DebugCommand() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("debug")
                .then(literal("reset")
                        .then(literal("actions")
                                .then(argument("entities", EntityArgument.players())
                                        .executes(DebugCommand::resetActions)))
                        .then(literal("gravity")
                                .then(argument("entities", EntityArgument.entities())
                                        .executes(DebugCommand::resetGravity))));
    }

    private static int resetActions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reset(context, CLEAR_ACTIONS_MSG_FAIL, CLEAR_ACTIONS_MSG_SUCCESS_SINGLE, CLEAR_ACTIONS_MSG_SUCCESS_MULTIPLE, ActionHolder::stopAllActions);
    }

    private static int resetGravity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reset(context, CLEAR_GRAVITY_MSG_FAIL, CLEAR_GRAVITY_MSG_SUCCESS_SINGLE, CLEAR_GRAVITY_MSG_SUCCESS_MULTIPLE, player -> {
            if (player.isNoGravity()) {
                player.setNoGravity(false);
                return true;
            }
            return false;
        });
    }

    private static int reset(CommandContext<CommandSourceStack> context,
                             String msgFail,
                             String msgSingle,
                             String msgMultiple,
                             Predicate<? super Player> function) throws CommandSyntaxException {
        int successCount = 0;
        Player knownPlayer = null;
        for (Player player : EntityArgument.getPlayers(context, "entities")) {
            if (function.test(player)) {
                knownPlayer = player;
                successCount++;
            }
        }
        if (successCount == 0) {
            context.getSource().sendFailure(Component.translatable(msgFail));
        } else if (successCount == 1) {
            Player knownPlayer1 = knownPlayer;
            context.getSource().sendSuccess(() -> Component.translatable(msgSingle, knownPlayer1.getDisplayName()), true);
        } else {
            int successCount1 = successCount;
            context.getSource().sendSuccess(() -> Component.translatable(msgMultiple, successCount1), true);
        }
        return successCount;
    }
}
