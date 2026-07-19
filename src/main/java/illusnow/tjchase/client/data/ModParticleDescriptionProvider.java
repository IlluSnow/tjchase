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

package illusnow.tjchase.client.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.particle.ModParticleTypes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class ModParticleDescriptionProvider extends ParticleDescriptionProvider {
    protected ModParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(ModParticleTypes.TJCHASE_BUFF.get(), TJChase.prefix("tjchase_buff"));
        spriteSet(ModParticleTypes.HARP_PLAYED_NOTE.get(), TJChase.prefix("8th_note"), TJChase.prefix("16th_note"), TJChase.prefix("beamed_8th_note"), TJChase.prefix("treble_clef"));
    }

    @Override
    public String getName() {
        return super.getName() + ": " + TJChase.MODID;
    }
}
