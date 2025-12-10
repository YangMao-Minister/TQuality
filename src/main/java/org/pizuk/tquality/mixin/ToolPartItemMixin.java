package org.pizuk.tquality.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.ModUtils;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import java.util.List;

@Mixin(value = slimeknights.tconstruct.library.tools.part.ToolPartItem.class)
public class ToolPartItemMixin {

    @Shadow
    @Final
    public MaterialStatsId materialStatId;
    @Unique
    private ItemStack currentStack;


    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void getCurrentStackForTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        currentStack = stack;
    }


    @Inject(method = "addStatInfoTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    public void addStatInfoTooltip(MaterialId material, List<Component> tooltip, CallbackInfo ci) {
        if (currentStack.isEmpty() || currentStack.getTag() == null)
            return;
        CompoundTag data = currentStack.getTag().getCompound(Tquality.PART_DATA);
        boolean isOnTable = currentStack.getTag().getBoolean(Tquality.ON_TABLE);
        boolean isDefaultQuality = currentStack.getTag().getBoolean(Tquality.DEFAULT_QUALITY);
        if (!data.isEmpty()) {
            IMaterialStats modifiedStat;
            if (!isDefaultQuality) {
                modifiedStat = StatsUtils.getStatsFromNbt(data);
            } else {
                modifiedStat = MaterialRegistry.getInstance().getMaterialStats(material, materialStatId).orElse(null);
            }
            List<Component> text = null;
            float quality = data.getFloat(Tquality.QUALITY);
            if (modifiedStat != null) {
                text = modifiedStat.getLocalizedInfo();
            }
            if (text != null && !text.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.add(modifiedStat.getLocalizedName().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));
                StatsUtils.addQualityTooltip(tooltip, quality);
                if (isDefaultQuality) {
                    tooltip.add(Component.translatable("tquality.tooltip.default_quality").withStyle(ChatFormatting.RED));
                }
                if (!isOnTable) {
                    tooltip.addAll(modifiedStat.getLocalizedInfo());
                } else {
                    tooltip.add(Component.translatable("tquality.tooltip.uncrafted_stats").withStyle(ChatFormatting.RED));
                }
            }
            ci.cancel();
        }
    }
}
