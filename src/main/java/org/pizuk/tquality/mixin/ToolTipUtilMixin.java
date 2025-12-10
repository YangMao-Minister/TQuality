package org.pizuk.tquality.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;

import java.util.List;
import java.util.Optional;

@Mixin(TooltipUtil.class)
public abstract class ToolTipUtilMixin {

    @Unique
    private static int currentIndex;

    @Unique
    private static List<Optional<IMaterialStats>> currentStats;


    @Inject(method = "getComponents", remap = false, at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/definition/module/material/ToolPartsHook;parts(Lslimeknights/tconstruct/library/tools/definition/ToolDefinition;)Ljava/util/List;"))
    private static void setCurrentStats(IModifiable item, ItemStack stack, List<Component> tooltips, TooltipFlag flag, CallbackInfo ci) {
        currentStats = StatsUtils.getStatsFromTool(stack);
    }

    @Inject(method = "getComponents", locals = LocalCapture.CAPTURE_FAILSOFT, remap = false, at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/materials/IMaterialRegistry;getMaterialStats(Lslimeknights/tconstruct/library/materials/definition/MaterialId;Lslimeknights/tconstruct/library/materials/stats/MaterialStatsId;)Ljava/util/Optional;"))
    private static void setStackAndIndex(IModifiable item, ItemStack stack, List<Component> tooltips, TooltipFlag flag, CallbackInfo ci, List components, MaterialNBT materials, int max, List parts, int partCount, int i, MaterialVariantId material, Component componentName) {
        currentIndex = i;
    }

    @Redirect(method = "getComponents", remap = false, at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/materials/IMaterialRegistry;getMaterialStats(Lslimeknights/tconstruct/library/materials/definition/MaterialId;Lslimeknights/tconstruct/library/materials/stats/MaterialStatsId;)Ljava/util/Optional;"))
    private static Optional<IMaterialStats> getComponents(IMaterialRegistry instance, MaterialId materialId, MaterialStatsId materialStatsId) {
        return currentStats.get(currentIndex);
    }
}
