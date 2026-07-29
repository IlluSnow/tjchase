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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.proficiency.ProficiencyLevel;
import illusnow.tjchase.entity.proficiency.ProficiencyMainLevel;
import illusnow.tjchase.item.ModDataComponents;
import illusnow.tjchase.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.commands.Commands.*;

public final class ProficiencyCommand {
    public static final String INVALID_REMAINING_MSG = TJChase.prefixCommand("proficiency.invalidRemaining");
    public static final String INVALID_ITEM_MSG = TJChase.prefixCommand("proficiency.invalidItem");
    public static final String SUCCESS_MSG = TJChase.prefixCommand("proficiency.success");
    private static final Dynamic2CommandExceptionType INVALID_REMAINING = new Dynamic2CommandExceptionType(
            (remaining, level) -> Component.translatable(INVALID_REMAINING_MSG, remaining, level)
    );
    private static final DynamicCommandExceptionType INVALID_ITEM = new DynamicCommandExceptionType(itemName -> Component.translatable(INVALID_ITEM_MSG, itemName));

    private ProficiencyCommand() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        LiteralArgumentBuilder<CommandSourceStack> nodeSet = literal("set");
        for (ProficiencyMainLevel mainLevel : ProficiencyMainLevel.LEVELS) {
            nodeSet = nodeSet.then(literal(mainLevel.getName()).then(subNode(mainLevel)));
        }
        return literal("proficiency")
                .requires(hasPermission(LEVEL_GAMEMASTERS))
                .then(nodeSet);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subNode(ProficiencyMainLevel mainLevel) {
        return argument("sublevel", IntegerArgumentType.integer(0, mainLevel.getMaxSublevel()))
                .then(argument("remaining", IntegerArgumentType.integer(0))
                        .executes(context -> setProficiency(context, mainLevel)));
    }

    private static int setProficiency(CommandContext<CommandSourceStack> context, ProficiencyMainLevel mainLevel) throws CommandSyntaxException {
        int sublevel = IntegerArgumentType.getInteger(context, "sublevel");
        int remaining = IntegerArgumentType.getInteger(context, "remaining");
        if (remaining >= mainLevel.getUpgradeNeed(sublevel)) {
            throw INVALID_REMAINING.create(remaining, mainLevel.makeDisplayName(sublevel));
        }
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.PROFICIENCY_STICK)) {
            throw INVALID_ITEM.create(stack.getHoverName().copy().withStyle(ChatFormatting.RED));
        }
        ProficiencyLevel proficiencyLevel = new ProficiencyLevel(mainLevel, sublevel, remaining);
        stack.set(ModDataComponents.PROFICIENCY, proficiencyLevel.getTotalPoints());
        context.getSource().sendSuccess(() -> Component.translatable(SUCCESS_MSG, proficiencyLevel.makeDisplayName()), true);
        return 1;
    }
}
