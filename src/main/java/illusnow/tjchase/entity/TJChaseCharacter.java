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

package illusnow.tjchase.entity;

import illusnow.tjchase.entity.proficiency.ProficiencyLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface TJChaseCharacter {
    int getProficiencyPoints();

    void setProficiencyPoints(int points);

    default void addProficiencyPoints(int points) {
        setProficiencyPoints(getProficiencyPoints() + points);
    }

    default void addProficiencyPoints(float points) {
        addProficiencyPoints(Math.round(points));
    }

    default void awardDamageProficiencyPoints(LivingEntity entity, float damageDealt) {
        addProficiencyPoints(Math.min(damageDealt, entity.getMaxHealth()));
    }

    default void awardKillProficiencyPoints(LivingEntity entity, float maxHealth) {
        addProficiencyPoints(maxHealth);
    }

    default ProficiencyLevel getProficiencyLevel() {
        return ProficiencyLevel.fromTotalPoints(getProficiencyPoints());
    }

    default boolean hasSuperArmor() {
        return false;
    }

    default boolean isWeak() {
        return false;
    }

    default void setWeak() {}

    default void recoverFromWeak() {}

    default float getReducedStartDamage(DamageSource source, float damage, float originalDamage) {
        return damage;
    }

    default float getReducedFinalDamage(DamageSource source, float damage, float originalDamage) {
        return damage;
    }
}
