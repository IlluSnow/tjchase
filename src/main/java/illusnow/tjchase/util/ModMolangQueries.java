package illusnow.tjchase.util;

import illusnow.tjchase.TJChase;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.loading.math.MolangQueries;

public final class ModMolangQueries {
    private static final String PREFIX = "query." + TJChase.MODID + "_";
    public static final String LERPED_LIMB_SWING = PREFIX + "lerped_limb_swing";

    private ModMolangQueries() {}

    public static void register() {
        MolangQueries.<LivingEntity>setActorVariable(LERPED_LIMB_SWING, actor -> actor.animatable().walkAnimation.position(actor.partialTick()));
    }
}
