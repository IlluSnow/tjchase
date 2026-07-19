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
 * {@link net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal}
 */
public class OwnerHurtTargetGoal<T extends Mob & OwnableEntity> extends TargetGoal {
    private final T ownableMob;
    private final Predicate<? super T> canUse;
    private final TriPredicate<? super T, ? super LivingEntity, ? super LivingEntity> wantsToAttack;
    @Nullable
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public OwnerHurtTargetGoal(T ownableMob, Predicate<? super T> canUse, TriPredicate<? super T, ? super LivingEntity, ? super LivingEntity>wantsToAttack) {
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
                this.ownerLastHurt = owner.getLastHurtMob();
                return owner.getLastHurtMobTimestamp() != timestamp
                        && canAttack(ownerLastHurt, TargetingConditions.DEFAULT)
                        && wantsToAttack.test(ownableMob, Objects.requireNonNull(ownerLastHurt), owner);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurt);
        LivingEntity owner = ownableMob.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
