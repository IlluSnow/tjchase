package illusnow.tjchase.sound;

import illusnow.tjchase.TJChase;

public final class ModSoundSubtitles {
    public static final String HARP_ATTRACT_BLOCKS = prefix(ModSoundNames.HARP_ATTRACT_BLOCKS);
    public static final String HARP_THROW_BLOCK = prefix(ModSoundNames.HARP_THROW_BLOCK);
    public static final String VINE_SEED_THROW = prefix(ModSoundNames.VINE_SEED_THROW);
    public static final String VINE_GROW = prefix(ModSoundNames.VINE_GROW);
    public static final String VINE_HEAL = prefix(ModSoundNames.VINE_HEAL);
    public static final String VINE_VANISH = prefix(ModSoundNames.VINE_VANISH);

    private ModSoundSubtitles() {}

    private static String prefix(String subtitle) {
        return "subtitles." + TJChase.MODID + "." + subtitle;
    }
}
