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

package illusnow.tjchase.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public class MobMixin {
    @WrapWithCondition(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tick()V"))
    public boolean controlGoals1(GoalSelector instance) {
        return !DancingHelper.isAiAffectedByZuri((Mob) (Object) this);
    }

    @WrapWithCondition(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tickRunningGoals(Z)V"))
    public boolean controlGoals2(GoalSelector instance, boolean tickAllRunning) {
        return !DancingHelper.isAiAffectedByZuri((Mob) (Object) this);
    }

    @WrapOperation(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V"))
    public void controlCustomServerAiStep(Mob self, ServerLevel level, Operation<Void> original, @Local ProfilerFiller profilerFiller) {
        if (DancingHelper.getDancingData(self).isPresent()) {
            if (DancingHelper.isAiAffectedByZuri(self)) {
                Zuri zuri = DancingHelper.getZuriDancingWithDirectly(self);
                profilerFiller.push("danceWithZuriTick");
                DancingHelper.controlDancingMob(self, zuri);
                profilerFiller.pop();
                return;
            } else if (DancingHelper.getDanceEffectType(self) == DancingHelper.DanceEffectType.NONE) {
                DancingHelper.clearAttachmentData(self);
            }
        }
        original.call(self, level);
    }
}
