package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, TJChase.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ModEntities.HARP_TESTER.get());
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ModEntities.VINE_MANAGER.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(ModEntities.HARP_TESTER.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(ModEntities.VINE_MANAGER.get());
    }
}
