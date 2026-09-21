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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, TJChase.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUEPRINT_FOLD = register(ModSoundNames.BLUEPRINT_FOLD);
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUEPRINT_RELEASE = register(ModSoundNames.BLUEPRINT_RELEASE);
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUEPRINT_THROW = register(ModSoundNames.BLUEPRINT_THROW);
    public static final DeferredHolder<SoundEvent, SoundEvent> DANCE_TIME = register(ModSoundNames.DANCE_TIME);
    public static final DeferredHolder<SoundEvent, SoundEvent> EVILINIA_AMBIENT = register(ModSoundNames.EVILINIA_AMBIENT);
    public static final DeferredHolder<SoundEvent, SoundEvent> EVILINIA_DEATH = register(ModSoundNames.EVILINIA_DEATH);
    public static final DeferredHolder<SoundEvent, SoundEvent> EVILINIA_HURT = register(ModSoundNames.EVILINIA_HURT);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_ATTRACT_BLOCKS = register(ModSoundNames.HARP_ATTRACT_BLOCKS);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_1 = register(ModSoundNames.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_1);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_2 = register(ModSoundNames.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_2);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_3 = register(ModSoundNames.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_3);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_THROW_BLOCK = register(ModSoundNames.HARP_THROW_BLOCK);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_THROW_BLOCK_LIGHTWEIGHT_1 = register(ModSoundNames.HARP_THROW_BLOCK_LIGHTWEIGHT_1);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_THROW_BLOCK_LIGHTWEIGHT_2 = register(ModSoundNames.HARP_THROW_BLOCK_LIGHTWEIGHT_2);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_THROW_BLOCK_LIGHTWEIGHT_3 = register(ModSoundNames.HARP_THROW_BLOCK_LIGHTWEIGHT_3);
    public static final DeferredHolder<SoundEvent, SoundEvent> LINIA_AMBIENT = register(ModSoundNames.LINIA_AMBIENT);
    public static final DeferredHolder<SoundEvent, SoundEvent> LINIA_DEATH = register(ModSoundNames.LINIA_DEATH);
    public static final DeferredHolder<SoundEvent, SoundEvent> LINIA_HURT = register(ModSoundNames.LINIA_HURT);
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMED_ROCKET = register(ModSoundNames.PRIMED_ROCKET);
    public static final DeferredHolder<SoundEvent, SoundEvent> REMOTE_CONTROL_PRESS = register(ModSoundNames.REMOTE_CONTROL_PRESS);
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_LAUNCH = register(ModSoundNames.ROCKET_LAUNCH);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_SEED_THROW = register(ModSoundNames.VINE_SEED_THROW);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_GROW = register(ModSoundNames.VINE_GROW);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_HEAL = register(ModSoundNames.VINE_HEAL);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_VANISH = register(ModSoundNames.VINE_VANISH);
    public static final DeferredHolder<SoundEvent, SoundEvent> YOGA_BALL_HIT = register(ModSoundNames.YOGA_BALL_HIT);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZURI_AMBIENT = register(ModSoundNames.ZURI_AMBIENT);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZURI_ATTACK = register(ModSoundNames.ZURI_ATTACK);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZURI_HURT = register(ModSoundNames.ZURI_HURT);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZURI_THROW_YOGA_BALL = register(ModSoundNames.ZURI_THROW_YOGA_BALL);
    public static final DeferredHolder<SoundEvent, SoundEvent> ZURI_WEAK = register(ModSoundNames.ZURI_WEAK);

    private ModSoundEvents() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = TJChase.prefix(name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
