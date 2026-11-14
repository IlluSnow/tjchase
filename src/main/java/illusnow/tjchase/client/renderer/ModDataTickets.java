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

package illusnow.tjchase.client.renderer;

import illusnow.tjchase.TJChase;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.dataticket.DataTicket;

public class ModDataTickets {
    public static final DataTicket<Float> RENDER_SCALE = create("render_scale", Float.class);
    public static final DataTicket<Boolean> DANCING = create("dancing", Boolean.class);
    public static final DataTicket<Double> CUSTOM_ENTITY_ALPHA = create("custom_player", Double.class);
    public static final DataTicket<Boolean> ALLOW_LAYER_RENDERING = create("allow_layer_rendering", Boolean.class);
    public static final DataTicket<Boolean> COCOON_DAMAGED = create("cocoon_damaged", Boolean.class);
    public static final DataTicket<Vec3> BAX_SPIDER_BACK_POS = create("bax_spider_back_pos", Vec3.class);
    public static final DataTicket<Vec3> BAX_SPIDER_LINKING_POS = create("bax_spider_linking_pos", Vec3.class);

    private ModDataTickets() {}

    private static <D> DataTicket<D> create(String id, Class<? extends D> type) {
        return DataTicket.create(TJChase.MODID + "_" + id, type);
    }
}
