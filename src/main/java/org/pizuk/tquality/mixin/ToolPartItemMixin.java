package org.pizuk.tquality.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;

import java.util.List;

@Mixin(value = slimeknights.tconstruct.library.tools.part.ToolPartItem.class)
public class ToolPartItemMixin {

    @Unique
    private ItemStack currentStack;


    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void getCurrentStackForTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        currentStack = stack;
    }


    @Inject(method = "addStatInfoTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    public void addStatInfoTooltip(MaterialId material, List<Component> tooltip, CallbackInfo ci) {
        CompoundTag tag = currentStack.getTag().getCompound(Tquality.TQUALITY_DATA);
        if (!tag.isEmpty()) {
            IMaterialStats modifiedStat = StatsUtils.getStatsFromNbt(tag);
            List<Component> text = null;
            if (modifiedStat != null) {
                text = modifiedStat.getLocalizedInfo();
            }
            if (text != null && !text.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.add(modifiedStat.getLocalizedName().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));
                tooltip.addAll(modifiedStat.getLocalizedInfo());
            }
            ci.cancel();
        }
    }
}
