package org.pizuk.tquality.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;
import java.util.Optional;

@Mixin(slimeknights.tconstruct.library.tools.definition.module.material.MaterialStatsModule.class)
public abstract class MaterialStatsModuleMixin {

    @Shadow
    @Final
    private List<MaterialStatsId> statTypes;

    @Shadow
    @Final
    private float[] scales;

    @Inject(method = "addToolStats",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    public void addToolStats(IToolContext context, ModifierStatsBuilder builder, CallbackInfo ci) {
        MaterialNBT materials = context.getMaterials();
        if (!materials.isEmpty()) {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            for (int i = 0; i < statTypes.size(); i++) {
                MaterialStatsId statType = statTypes.get(i);
                // apply the stats if they exist for the material
                Optional<IMaterialStats> stats = registry.getMaterialStats(materials.get(i).getId(), statType);
                ToolDataNBT toolData = (ToolDataNBT) context.getPersistentData();
                ListTag toolPartData = (ListTag) toolData.get(Tquality.modResource(Tquality.TOOL_DATA));
                if (toolPartData != null && toolPartData.get(i) instanceof CompoundTag tag && tag.contains(Tquality.STATS)) {
                    stats = Optional.ofNullable(StatsUtils.getStatsFromNbt(tag));
                }
                if (stats.isPresent()) {
                    stats.get().apply(builder, scales[i]);
                } else {
                    // fallback to the default stats if present
                    IMaterialStats defaultStats = registry.getDefaultStats(statType);
                    if (defaultStats != null) {
                        defaultStats.apply(builder, scales[i]);
                    }
                }
            }
        }
        ci.cancel();
    }
}
