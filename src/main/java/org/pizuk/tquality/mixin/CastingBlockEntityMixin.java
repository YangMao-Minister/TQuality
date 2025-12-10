package org.pizuk.tquality.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.CompositeCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.inventory.MoldingContainerWrapper;

import javax.annotation.Nullable;

@Mixin(CastingBlockEntity.class)
public abstract class CastingBlockEntityMixin {

    @Shadow
    @Final
    private MoldingContainerWrapper moldingInventory;

    @Shadow
    @Nullable
    protected abstract MoldingRecipe findMoldingRecipe();

    @Shadow
    private ICastingRecipe currentRecipe;

    @ModifyVariable(method = "serverTick", at = @At("STORE"), name = "output", remap = false)
    public ItemStack modifyPartItem(ItemStack result) {
        ItemStack castingStack = moldingInventory.getMaterial();
        if (castingStack.isEmpty())
            return result;
        if (currentRecipe instanceof MaterialCastingRecipe) {
            if (castingStack.is(TinkerTags.Items.GOLD_CASTS)) {
                return StatsUtils.injectStatsNbtToPart(result, castingStack);
            }
            if (castingStack.is(TinkerTags.Items.SAND_CASTS) || castingStack.is(TinkerTags.Items.RED_SAND_CASTS)) {
                return StatsUtils.injectStatsNbtToPart(result, castingStack, 0.5f);
            }
        }
        float quality;
        CompoundTag tag = castingStack.getTag();
        if (tag != null) {
            if (tag.contains(Tquality.PART_DATA))
                quality = tag.getCompound(Tquality.PART_DATA).getFloat(Tquality.QUALITY);
            else if (tag.contains(Tquality.QUALITY))
                quality = tag.getFloat(Tquality.QUALITY);
            else
                return result;
        } else
            quality = 0.5f;
        if (currentRecipe instanceof ItemCastingRecipe) {
            result.getOrCreateTag().putFloat(Tquality.QUALITY, quality);
        } else if (currentRecipe instanceof CompositeCastingRecipe) {
            ItemStack recipeOutItem = currentRecipe.getResultItem(null);
            if (recipeOutItem.getItem() instanceof ToolPartItem) {
                return StatsUtils.injectStatsNbtToPart(result, recipeOutItem, quality);
            }
        }
        return result;
    }
}
