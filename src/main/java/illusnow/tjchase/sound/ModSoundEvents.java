package illusnow.tjchase.sound;

import illusnow.tjchase.TJChase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, TJChase.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_ATTRACT_BLOCKS = register(ModSoundNames.HARP_ATTRACT_BLOCKS);
    public static final DeferredHolder<SoundEvent, SoundEvent> HARP_THROW_BLOCK = register(ModSoundNames.HARP_THROW_BLOCK);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_SEED_THROW = register(ModSoundNames.VINE_SEED_THROW);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_GROW = register(ModSoundNames.VINE_GROW);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_HEAL = register(ModSoundNames.VINE_HEAL);
    public static final DeferredHolder<SoundEvent, SoundEvent> VINE_VANISH = register(ModSoundNames.VINE_VANISH);

    private ModSoundEvents() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = TJChase.prefix(name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
