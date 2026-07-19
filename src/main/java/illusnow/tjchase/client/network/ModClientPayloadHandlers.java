package illusnow.tjchase.client.network;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.client.resources.sounds.DanceTimeSoundInstance;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.network.PlayDanceTimePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public final class ModClientPayloadHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ModClientPayloadHandlers() {}

    public static void handlePlayDanceTime(PlayDanceTimePayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Zuri zuri = payload.getZuri(minecraft.level);
            if (zuri != null) {
                if (!zuri.isSilent()) {
                    minecraft.getSoundManager().play(new DanceTimeSoundInstance(zuri));
                }
            }
        }
    }
}
