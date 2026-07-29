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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import illusnow.tjchase.TJChase;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class EntityCommand {
    public static final String FAILURE_MSG = TJChase.prefixCommand("entity.resumeai.failure");
    public static final String SUCCESS_SINGLE_MSG = TJChase.prefixCommand("entity.resumeai.successSingle");
    public static final String SUCCESS_MULTIPLE_MSG = TJChase.prefixCommand("entity.resumeai.successMultiple");

    private EntityCommand() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("entity")
                .then(literal("resumeai")
                        .executes(context -> resumeAi(context, 64))
                        .then(argument("distance", IntegerArgumentType.integer(1, 64))
                                .executes(EntityCommand::resumeAi)));
    }

    private static int resumeAi(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int radius = IntegerArgumentType.getInteger(context, "distance");
        return resumeAi(context, radius);
    }

    private static int resumeAi(CommandContext<CommandSourceStack> context, int radius) throws CommandSyntaxException {
        int count = 0;
        ServerLevel level = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        Component name = null;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius), mob -> mob.distanceToSqr(player) <= radius * radius)) {
            if (mob.isNoAi()) {
                mob.setNoAi(false);
                name = mob.getDisplayName();
                count++;
            }
        }
        if (count == 0) {
            context.getSource().sendFailure(Component.translatable(FAILURE_MSG));
        } else if (count == 1) {
            Component finalName = name;
            context.getSource().sendSuccess(() -> Component.translatable(SUCCESS_SINGLE_MSG, finalName), true);
        } else {
            int finalCount = count;
            context.getSource().sendSuccess(() -> Component.translatable(SUCCESS_MULTIPLE_MSG, finalCount), true);
        }
        return count;
    }
}
