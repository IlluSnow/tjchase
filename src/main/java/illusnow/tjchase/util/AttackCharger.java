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
