package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItemNames;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.sound.ModSoundSubtitles;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    public ModSoundProvider(PackOutput output) {
        super(output, TJChase.MODID);
    }

    @Override
    public void registerSounds() {
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
    }

    private static SoundDefinition.Sound modSound(String path) {
        return sound(TJChase.prefix(path));
    }
}
