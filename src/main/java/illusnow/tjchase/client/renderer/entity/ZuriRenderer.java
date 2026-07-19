package illusnow.tjchase.client.renderer.entity;

import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.Zuri;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ZuriRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<Zuri, R> {
    public ZuriRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.ZURI.get());
    }

    @Override
    public void addRenderData(Zuri zuri, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(ModDataTickets.DANCING, zuri.isDancing());
    }
}
