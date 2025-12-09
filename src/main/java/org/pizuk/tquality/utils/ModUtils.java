package org.pizuk.tquality.utils;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.pizuk.tquality.Tquality;

public class ModUtils {
    public static TextColor getQualityColor(float quality) {
        return TextColor.fromRgb(Mth.hsvToRgb(quality / 3.0f, 1.0F, 1.0F));
    }

    public static void setQuality(ItemStack stack, float quality) {
        stack.getOrCreateTag().putFloat(Tquality.QUALITY, quality);
    }

    public static float normalDistribution(float mean, float stdDev) {
        double u1 = Math.random();
        double u2 = Math.random();
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return (float) (z * stdDev + mean);
    }


}

