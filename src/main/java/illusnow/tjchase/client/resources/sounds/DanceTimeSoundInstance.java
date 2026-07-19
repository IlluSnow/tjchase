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
