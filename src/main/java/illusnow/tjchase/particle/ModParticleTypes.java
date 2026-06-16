package illusnow.tjchase.particle;

import com.mojang.serialization.MapCodec;
import illusnow.tjchase.TJChase;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TJChase.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HARP_PLAYED_NOTE = register("harp_played_note", false);
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> TJCHASE_BUFF = register(
            "tjchase_buff", false, ColorParticleOption::codec, ColorParticleOption::streamCodec
    );

    private ModParticleTypes() {}

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String name, boolean overrideLimiter) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(overrideLimiter));
    }

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(
            String name,
            boolean overrideLimiter,
            final Function<ParticleType<T>, MapCodec<T>> codecGetter,
            final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter
    ) {
        return PARTICLE_TYPES.register(name, () -> new ParticleType<T>(overrideLimiter) {
            @Override
            public MapCodec<T> codec() {
                return codecGetter.apply(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodecGetter.apply(this);
            }
        });
    }
}
