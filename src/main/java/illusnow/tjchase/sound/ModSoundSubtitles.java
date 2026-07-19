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

package illusnow.tjchase.sound;

import illusnow.tjchase.TJChase;

public final class ModSoundSubtitles {
    public static final String DANCE_TIME = prefix(ModSoundNames.DANCE_TIME);
    public static final String HARP_ATTRACT_BLOCKS = prefix(ModSoundNames.HARP_ATTRACT_BLOCKS);
    public static final String HARP_THROW_BLOCK = prefix(ModSoundNames.HARP_THROW_BLOCK);
    public static final String VINE_SEED_THROW = prefix(ModSoundNames.VINE_SEED_THROW);
    public static final String VINE_GROW = prefix(ModSoundNames.VINE_GROW);
    public static final String VINE_HEAL = prefix(ModSoundNames.VINE_HEAL);
    public static final String VINE_VANISH = prefix(ModSoundNames.VINE_VANISH);
    public static final String YOGA_BALL_HIT = prefix(ModSoundNames.YOGA_BALL_HIT);
    public static final String ZURI_AMBIENT = prefix(ModSoundNames.ZURI_AMBIENT);
    public static final String ZURI_ATTACK = prefix(ModSoundNames.ZURI_ATTACK);
    public static final String ZURI_HURT = prefix(ModSoundNames.ZURI_HURT);
    public static final String ZURI_THROW_YOGA_BALL = prefix(ModSoundNames.ZURI_THROW_YOGA_BALL);

    private ModSoundSubtitles() {}

    private static String prefix(String subtitle) {
        return "subtitles." + TJChase.MODID + "." + subtitle;
    }
}
