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
