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

import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public class PrimedRocketSoundInstance extends AbstractTickableSoundInstance {
    private final Rocket rocket;

    public PrimedRocketSoundInstance(Rocket rocket) {
        super(ModSoundEvents.PRIMED_ROCKET.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.rocket = rocket;
        looping = true;
        volume = 0.25F;
        delay = 0;
    }

    @Override
    public void tick() {
        if (!rocket.shouldPlayFuseSound()) {
            stop();
        }
        x = rocket.getX();
        y = rocket.getY();
        z = rocket.getZ();
    }
}
