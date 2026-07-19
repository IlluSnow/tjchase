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
                profilerFiller.push("dancingWithZuriTick");
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
