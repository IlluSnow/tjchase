package illusnow.tjchase.entity;

import illusnow.tjchase.entity.proficency.ProficiencyLevel;

public interface TJChaseCharacter {
    int getProficiencyPoints();

    void setProficiencyPoints(int points);

    default void addProficiencyPoints(int points) {
        setProficiencyPoints(getProficiencyPoints() + points);
    }

    default ProficiencyLevel getProficiencyLevel() {
        return ProficiencyLevel.fromTotalPoints(getProficiencyPoints());
    }

    default boolean hasSuperArmor() {
        return false;
    }

    default float getReducedStartDamage(float damage, float originalDamage) {
        return damage;
    }

    default float getReducedFinalDamage(float damage, float originalDamage) {
        return damage;
    }
}
