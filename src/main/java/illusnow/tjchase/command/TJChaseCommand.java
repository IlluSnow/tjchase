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

import com.mojang.brigadier.CommandDispatcher;
import illusnow.tjchase.TJChase;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class TJChaseCommand {
    private TJChaseCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(TJChase.MODID)
                        .then(ProficiencyCommand.register())
        );
    }
}
