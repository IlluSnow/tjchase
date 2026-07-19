package illusnow.tjchase.util;

import java.util.function.IntSupplier;

public class AttackCharger {
    private final IntSupplier maxCooldown;
    private final DamageModifier damageModifier;
    private int cooldown;

    public AttackCharger(IntSupplier maxCooldown, boolean disabled) {
        this(maxCooldown, disabled ? damage -> damage : damage -> damage * 2);
    }

    public AttackCharger(IntSupplier maxCooldown, DamageModifier damageModifier) {
        this.maxCooldown = maxCooldown;
        this.damageModifier = damageModifier;
        reset();
    }

    public void reset() {
        cooldown = maxCooldown.getAsInt();
    }

    public void refresh() {
        cooldown = 0;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public void tick() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public boolean isCharged() {
        return cooldown == 0;
    }

    public float modifyDamage(float damage) {
        reset();
        return isCharged() ? getChargedDamage(damage) : damage;
    }

    private float getChargedDamage(float damage) {
        return damageModifier.modify(damage);
    }

    @FunctionalInterface
    public interface DamageModifier {
        float modify(float damage);
    }
}
