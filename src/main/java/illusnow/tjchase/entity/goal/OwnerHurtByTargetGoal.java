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

package illusnow.tjchase.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * [VanillaCopy]
 * {@link net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal}
 */
public class OwnerHurtByTargetGoal<T extends Mob & OwnableEntity> extends TargetGoal {
    private final T ownableMob;
    private final Predicate<? super T> canUse;
    private final TriPredicate<? super T, ? super LivingEntity, ? super LivingEntity> wantsToAttack;
    @Nullable
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public OwnerHurtByTargetGoal(T ownableMob, Predicate<? super T> canUse, TriPredicate<? super T, ? super LivingEntity, ? super LivingEntity>wantsToAttack) {
        super(ownableMob, false);
        this.ownableMob = ownableMob;
        this.canUse = canUse;
        this.wantsToAttack = wantsToAttack;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (canUse.test(ownableMob)) {
            LivingEntity owner = ownableMob.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                return owner.getLastHurtByMobTimestamp() != timestamp
                        && canAttack(ownerLastHurtBy, TargetingConditions.DEFAULT)
                        && wantsToAttack.test(ownableMob, Objects.requireNonNull(ownerLastHurtBy), owner);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurtBy);
        LivingEntity owner = ownableMob.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
