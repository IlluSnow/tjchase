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

package illusnow.tjchase.block.entity;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlockNames;
import illusnow.tjchase.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TJChase.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MailboxBlockEntity>> MAILBOX = BLOCK_ENTITIES.register(ModBlockNames.MAILBOX, () ->
            new BlockEntityType<>(MailboxBlockEntity::new, ModBlocks.MAILBOX.get()));

    private ModBlockEntities() {}
}
