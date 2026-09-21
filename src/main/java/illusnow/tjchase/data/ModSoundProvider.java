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

package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntityNames;
import illusnow.tjchase.item.ModItemNames;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.sound.ModSoundSubtitles;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    public ModSoundProvider(PackOutput output) {
        super(output, TJChase.MODID);
    }

    @Override
    public void registerSounds() {
        add(ModSoundEvents.DANCE_TIME, definition()
                .subtitle(ModSoundSubtitles.DANCE_TIME)
                .with(modSound("entity/" + ModEntityNames.ZURI + "/dance_time")));

        add(ModSoundEvents.BLUEPRINT_FOLD, definition()
                .subtitle(ModSoundSubtitles.BLUEPRINT_FOLD)
                .with(modSound("item/%s/fold".formatted(ModItemNames.BLUEPRINT))));
        add(ModSoundEvents.BLUEPRINT_RELEASE, definition()
                .subtitle(ModSoundSubtitles.BLUEPRINT_RELEASE)
                .with(modSound("item/%s/release".formatted(ModItemNames.BLUEPRINT))));
        add(ModSoundEvents.BLUEPRINT_THROW, definition()
                .subtitle(ModSoundSubtitles.BLUEPRINT_THROW)
                .with(modSound("item/%s/fold".formatted(ModItemNames.BLUEPRINT))));

        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS, "");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_1, "1");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_2, "2");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_3, "3");

        harpThrow(ModSoundEvents.HARP_THROW_BLOCK, "");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_1, "1");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_2, "2");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_3, "3");

        String catAttack = "tjchase_cat_attack";
        add(ModSoundEvents.VINE_SEED_THROW, definition()
                .subtitle(ModSoundSubtitles.VINE_SEED_THROW)
                .with(sound("random/bow")));
        add(ModSoundEvents.REMOTE_CONTROL_PRESS, definition()
                .subtitle(ModSoundSubtitles.REMOTE_CONTROL_PRESS)
                .with(modSound("item/%s/press".formatted(ModItemNames.REMOTE_CONTROL))));
        add(ModSoundEvents.VINE_GROW, definition()
                .subtitle(ModSoundSubtitles.VINE_GROW)
                .with(modSound("item/%s/grow1".formatted(ModItemNames.VINE_SEED)))
                .with(modSound("item/%s/grow2".formatted(ModItemNames.VINE_SEED))));
        add(ModSoundEvents.VINE_HEAL, definition()
                .subtitle(ModSoundSubtitles.VINE_HEAL)
                .with(modSound("item/%s/heal".formatted(ModItemNames.VINE_SEED))));
        add(ModSoundEvents.VINE_VANISH, definition()
                .subtitle(ModSoundSubtitles.VINE_VANISH)
                .with(modSound("item/%s/vanish1".formatted(ModItemNames.VINE_SEED)))
                .with(modSound("item/%s/vanish2".formatted(ModItemNames.VINE_SEED))));
        add(ModSoundEvents.YOGA_BALL_HIT, definition()
                .subtitle(ModSoundSubtitles.YOGA_BALL_HIT)
                .with(modSound("entity/%s/yoga_ball_hit1".formatted(ModEntityNames.ZURI)))
                .with(modSound("entity/%s/yoga_ball_hit2".formatted(ModEntityNames.ZURI)))
                .with(modSound("entity/%s/yoga_ball_hit3".formatted(ModEntityNames.ZURI))));

        modIdleSound(ModSoundEvents.ZURI_AMBIENT, ModSoundSubtitles.ZURI_AMBIENT, ModEntityNames.ZURI, 3);
        modEntitySoundWithType(ModSoundEvents.ZURI_ATTACK, catAttack, ModSoundSubtitles.ZURI_ATTACK, "", 3);
        modHurtSound(ModSoundEvents.ZURI_HURT, ModSoundSubtitles.ZURI_HURT, ModEntityNames.ZURI, 2);

        modIdleSound(ModSoundEvents.LINIA_AMBIENT, ModSoundSubtitles.LINIA_AMBIENT, ModEntityNames.LINIA, 3);
        modDeathSound(ModSoundEvents.LINIA_DEATH, ModSoundSubtitles.LINIA_DEATH, ModEntityNames.LINIA, 2);
        modHurtSound(ModSoundEvents.LINIA_HURT, ModSoundSubtitles.LINIA_HURT, ModEntityNames.LINIA, 2);
        modIdleSound(ModSoundEvents.EVILINIA_AMBIENT, ModSoundSubtitles.EVILINIA_AMBIENT, ModEntityNames.LINIA, 3);
        modDeathSound(ModSoundEvents.EVILINIA_DEATH, ModSoundSubtitles.EVILINIA_DEATH, ModEntityNames.LINIA, 2);
        modHurtSound(ModSoundEvents.EVILINIA_HURT, ModSoundSubtitles.EVILINIA_HURT, ModEntityNames.LINIA, 2);

        add(ModSoundEvents.ZURI_THROW_YOGA_BALL, definition()
                .subtitle(ModSoundSubtitles.ZURI_THROW_YOGA_BALL)
                .with(modSound("entity/tjchase_throw")));
        add(ModSoundEvents.ZURI_WEAK, definition()
                .subtitle(ModSoundSubtitles.ZURI_WEAK)
                .with(modSound("entity/tjchase_cat_weak")));

        add(ModSoundEvents.PRIMED_ROCKET, definition()
                .subtitle(ModSoundSubtitles.PRIMED_ROCKET)
                .with(modSound("entity/%s/primed".formatted(ModEntityNames.ROCKET))));
        add(ModSoundEvents.ROCKET_LAUNCH, definition()
                .subtitle(ModSoundSubtitles.ROCKET_LAUNCH)
                .with(modSound("entity/%s/launch".formatted(ModEntityNames.ROCKET))));
    }

    private void modIdleSound(Holder<SoundEvent> sound, String subtitle, String name, int count) {
        modEntitySoundWithType(sound, "idle", subtitle, name, count);
    }

    private void modHurtSound(Holder<SoundEvent> sound, String subtitle, String name, int count) {
        modEntitySoundWithType(sound, "hurt", subtitle, name, count);
    }

    private void modDeathSound(Holder<SoundEvent> sound, String subtitle, String name, int count) {
        modEntitySoundWithType(sound, "death", subtitle, name, count);
    }

    private void modEntitySoundWithType(Holder<SoundEvent> sound, String type, String subtitle, String name, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Count must not be less than 2");
        }
        SoundDefinition def = definition().subtitle(subtitle);
        for (int i = 1; i <= count; i++) {
            def.with(modSound("entity%s/%s%d".formatted(name.isEmpty() ? "" : "/" + name, type, i)));
        }
        add(sound, def);
    }

    private void harpAttract(Holder<SoundEvent> sound, String suffix) {
        add(sound, definition()
                .subtitle(ModSoundSubtitles.HARP_ATTRACT_BLOCKS)
                .with(modSound("item/%s/attract%s".formatted(ModItemNames.HARP, suffix))));
    }

    private void harpThrow(Holder<SoundEvent> sound, String suffix) {
        add(sound, definition()
                .subtitle(ModSoundSubtitles.HARP_THROW_BLOCK)
                .with(modSound("item/%s/throw%s".formatted(ModItemNames.HARP, suffix))));
    }

    private static SoundDefinition.Sound modSound(String path) {
        return sound(TJChase.prefix(path));
    }
}
