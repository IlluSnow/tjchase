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

package illusnow.tjchase.client.resources.sounds;

import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public class DanceTimeSoundInstance extends AbstractTickableSoundInstance {
    private final Zuri zuri;

    public DanceTimeSoundInstance(Zuri zuri) {
        super(ModSoundEvents.DANCE_TIME.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.zuri = zuri;
        looping = true;
        volume = 1;
        delay = 0;
    }

    @Override
    public void tick() {
        if (!zuri.isAlive() || !zuri.isDancing()) {
            stop();
        }
        x = zuri.getX();
        y = zuri.getY();
        z = zuri.getZ();
    }
}
