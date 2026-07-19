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

        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS, "");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_1, "1");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_2, "2");
        harpAttract(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_3, "3");

        harpThrow(ModSoundEvents.HARP_THROW_BLOCK, "");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_1, "1");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_2, "2");
        harpThrow(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_3, "3");

        add(ModSoundEvents.VINE_SEED_THROW, definition()
                .subtitle(ModSoundSubtitles.VINE_SEED_THROW)
                .with(sound("random/bow")));
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
        add(ModSoundEvents.ZURI_AMBIENT, definition()
                .subtitle(ModSoundSubtitles.ZURI_AMBIENT)
                .with(modSound("entity/%s/idle1".formatted(ModEntityNames.ZURI)))
                .with(modSound("entity/%s/idle2".formatted(ModEntityNames.ZURI)))
                .with(modSound("entity/%s/idle3".formatted(ModEntityNames.ZURI))));
        add(ModSoundEvents.ZURI_ATTACK, definition()
                .subtitle(ModSoundSubtitles.ZURI_ATTACK)
                .with(modSound("entity/tjchase_cat_attack1"))
                .with(modSound("entity/tjchase_cat_attack2"))
                .with(modSound("entity/tjchase_cat_attack3")));
        add(ModSoundEvents.ZURI_HURT, definition()
                .subtitle(ModSoundSubtitles.ZURI_HURT)
                .with(modSound("entity/%s/hurt1".formatted(ModEntityNames.ZURI)))
                .with(modSound("entity/%s/hurt2".formatted(ModEntityNames.ZURI))));
        add(ModSoundEvents.ZURI_THROW_YOGA_BALL, definition()
                .subtitle(ModSoundSubtitles.ZURI_THROW_YOGA_BALL)
                .with(modSound("entity/tjchase_throw")));
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
