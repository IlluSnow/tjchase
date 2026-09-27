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

package illusnow.tjchase.world.gameplay.action;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.world.gameplay.ModPlayerAnimationIDs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModActions {
    public static final DeferredRegister<Action> ACTIONS = DeferredRegister.create(ModRegistries.ACTIONS_KEY, TJChase.MODID);
    public static final DeferredHolder<Action, Action> STRUGGLE = ACTIONS.register("tied_struggle", id -> new StruggleAction(id, ModPlayerAnimationIDs.TIED_STRUGGLE, Action.MEDIUM_PRIORITY));
    public static final DeferredHolder<Action, Action> PRAY = ACTIONS.register("pray", id -> new StruggleAction(id, ModPlayerAnimationIDs.PRAY, Action.MEDIUM_PRIORITY));
    public static final DeferredHolder<Action, Action> HUG = ACTIONS.register("hug", id -> new HugAction(id, ModPlayerAnimationIDs.HUG, Action.MEDIUM_PRIORITY));
    public static final DeferredHolder<Action, Action> TIE = ACTIONS.register("tie", id -> new TieAction(id, ModPlayerAnimationIDs.TIE, Action.MEDIUM_PRIORITY));

    private ModActions() {}
}
