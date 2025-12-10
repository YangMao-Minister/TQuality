package org.pizuk.tquality.mixin;

import net.minecraft.world.item.ItemStack;
import org.pizuk.tquality.Tquality;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

@Mixin(MultiModuleContainerMenu.class)
public abstract class MultiModuleContainerMenuMixin {

    @ModifyVariable(method = "quickMoveStack", at = @At("STORE"), name = "itemstack")
    public ItemStack removeTag(ItemStack stack) {
        if (stack.getItem() instanceof ToolPartItem && stack.getTag().contains(Tquality.ON_TABLE)) {
            stack.getTag().remove(Tquality.ON_TABLE);
        }
        return stack;
    }
}
