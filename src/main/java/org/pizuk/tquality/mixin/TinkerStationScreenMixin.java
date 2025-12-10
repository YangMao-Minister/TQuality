package org.pizuk.tquality.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;
import slimeknights.tconstruct.tables.client.inventory.ToolTableScreen;

@Mixin(ToolTableScreen.class)
public class TinkerStationScreenMixin {
    @ModifyVariable(method = "updateToolPanel", at = @At("STORE"), name = "tool", remap = false)
    public ToolStack rebuildToolForRender(ToolStack tool) {
        tool.rebuildStats();
        return tool;
    }
}
