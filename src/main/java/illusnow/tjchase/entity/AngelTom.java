package illusnow.tjchase.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class AngelTom extends Monster implements TJChaseCharacter {
    public AngelTom(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public int getProficiencyPoints() {
        return 0;
    }

    @Override
    public void setProficiencyPoints(int points) {}
}
