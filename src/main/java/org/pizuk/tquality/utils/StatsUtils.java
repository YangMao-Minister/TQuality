package org.pizuk.tquality.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.TierSortingRegistry;
import org.pizuk.tquality.Tquality;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatsUtils {
    public static <T extends IMaterialStats> JsonObject serializeStats(T stats) {
        @SuppressWarnings("unchecked")
        RecordLoadable<T> loadable = (RecordLoadable<T>) stats.getType().getLoadable();
        return loadable.serialize(stats).getAsJsonObject();
    }

    public static <T extends IMaterialStats> T deserializeStats(CompoundTag tag, MaterialStatType<?> materialStatType) {
        RecordLoadable<T> loadable = (RecordLoadable<T>) materialStatType.getLoadable();
        return loadable.deserialize(statsNbtToJson(tag));
    }

    public static JsonObject statsNbtToJson(CompoundTag tag) {
        JsonObject jsonObject = new JsonObject();
        for (String key : tag.getAllKeys()) {
            switch (tag.getTagType(key)) {
                case Tag.TAG_STRING -> jsonObject.add(key, new JsonPrimitive(tag.getString(key).toLowerCase()));
                case Tag.TAG_DOUBLE -> jsonObject.add(key, new JsonPrimitive(tag.getDouble(key)));
                case Tag.TAG_INT -> jsonObject.add(key, new JsonPrimitive(tag.getInt(key)));
            }
        }
        return jsonObject;
    }

    public static CompoundTag jsonToStatsNbt(JsonObject jsonObject) {
        try {
            String jsonString = jsonObject.toString();
            return TagParser.parseTag(jsonString);
        } catch (Exception e) {
            return new CompoundTag();
        }
    }

    public static float modifyStatsValue(float value, MaterialId materialId, float quality, float seed) {
        // TODO: KJS Events here
        // float value : mining speed, attack damage
        // Desmos graph here:
        // https://www.desmos.com/calculator/htzwkeuqhp
        return value * ModUtils.normalDistribution(1.0f + (quality - 0.5f) * 0.15f, 0.07f * (1.05f - 0.5f * quality), seed);
    }

    public static int modifyStatsValue(int value, MaterialId materialId, float quality, float seed) {
        // int value : durability
        return (int) (value * ModUtils.normalDistribution(1.0f + (quality - 0.5f) * 0.5f, 0.25f * (1.05f - 0.5f * quality), seed));
    }

    public static String modifyStatsValue(Tier tier, MaterialId materialId, float quality, float seed) {
        // String value : tier
        return tier.toString();
    }

    public static ItemStack injectStatsNbtToPart(ItemStack partItemStack, MaterialId material, float quality, float seed) {
        if (partItemStack.isEmpty() || !(partItemStack.getItem() instanceof ToolPartItem partItem))
            return partItemStack;

        CompoundTag statsNbt = new CompoundTag();
        statsNbt.putFloat(Tquality.QUALITY, quality);
        boolean isDefault = partItemStack.getTag().getBoolean(Tquality.DEFAULT_QUALITY);
        if (!isDefault) {
            MaterialRegistry.getInstance().getMaterialStats(material, partItem.materialStatId).ifPresent(stat -> {
                JsonObject json = new JsonObject();
                serializeStats(stat).entrySet().forEach(entry -> {
                    JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                    if (primitive.isNumber()) {
                        Number num = primitive.getAsNumber();
                        if (num instanceof Float || num instanceof Double)
                            json.addProperty(entry.getKey(), modifyStatsValue(num.floatValue(), material, quality, seed));
                        if (num instanceof Integer || num instanceof Long)
                            json.addProperty(entry.getKey(), modifyStatsValue(num.intValue(), material, quality, seed));
                    } else if (primitive.isString()) {
                        Tier tier = TierSortingRegistry.byName(ResourceLocation.parse(primitive.getAsString()));
                        if (tier != null) {
                            json.addProperty(entry.getKey(), modifyStatsValue(tier, material, quality, seed));
                        }
                    }
                });
                statsNbt.put(Tquality.STATS, jsonToStatsNbt(json));
                statsNbt.putString(Tquality.STATS_TYPE, stat.getType().getId().toString());
            });
        }
        partItemStack.getOrCreateTag().put(Tquality.PART_DATA, statsNbt);
        return partItemStack;
    }

    public static ItemStack injectStatsNbtToPart(ItemStack original, ItemStack materialStack) {
        if (original.isEmpty() || materialStack.isEmpty() || !(original.getItem() instanceof ToolPartItem part))
            return original;

        float quality;
        if (materialStack.hasTag() && materialStack.getTag().contains(Tquality.QUALITY)) {
            quality = materialStack.getTag().getFloat(Tquality.QUALITY);
        } else {
            quality = 0.5f;
            original.getOrCreateTag().putBoolean(Tquality.DEFAULT_QUALITY, true);
        }

        MaterialId material = part.getMaterial(original).getId();
        return injectStatsNbtToPart(original, material, quality, -1);
    }

    public static ItemStack updateStatsByQuality(ItemStack original, float deltaQuality) {
        if (original.isEmpty() || !(original.getItem() instanceof ToolPartItem part) || !original.getTag().contains(Tquality.PART_DATA)) {
            return original;
        }
        CompoundTag statsNbt = original.getTag().getCompound(Tquality.PART_DATA).getCompound(Tquality.STATS);
        float oldQuality = original.getTag().getCompound(Tquality.PART_DATA).getFloat(Tquality.QUALITY);

        if (oldQuality + deltaQuality > 0.95) {
            return original;
        }

        original.getTag().getCompound(Tquality.PART_DATA).putFloat(Tquality.QUALITY, oldQuality + deltaQuality);

        float factor = deltaQuality * 0.2f;
        for (String key : statsNbt.getAllKeys()) {
            switch (statsNbt.getTagType(key)) {
                case Tag.TAG_FLOAT -> {
                    statsNbt.putFloat(key, statsNbt.getFloat(key) * (1 + factor * 0.3f));
                }
                case Tag.TAG_INT -> {
                    statsNbt.putInt(key, (int) (statsNbt.getInt(key) * (1 + factor)));
                }
                case Tag.TAG_STRING -> {
                    Tier tier = TierSortingRegistry.byName(ResourceLocation.parse(statsNbt.getString(key).toLowerCase()));
                    if (tier != null) {
                        statsNbt.putString(key, tier.toString());
                    }
                }
            }
        }

        return original;
    }

    public static ItemStack injectStatsNbtToPart(ItemStack original, ItemStack materialStack, float overrideQuality) {
        if (original.isEmpty() || materialStack.isEmpty() || !(original.getItem() instanceof ToolPartItem part))
            return original;
        return injectStatsNbtToPart(original, part.getMaterial(original).getId(), overrideQuality, -1);
    }

    public static IMaterialStats getStatsFromNbt(CompoundTag statsData) {
        MaterialStatsId statsId = new MaterialStatsId(statsData.getString(Tquality.STATS_TYPE));
        IMaterialStats stats = MaterialRegistry.getInstance().getMaterialStats(MaterialIds.manyullyn, new MaterialStatsId(statsId)).orElse(null);
        if (stats != null) {
            return StatsUtils.deserializeStats(statsData.getCompound(Tquality.STATS), stats.getType());
        }
        return null;
    }

    public static ItemStack injectStatsNbtToTool(ItemStack original, List<ItemStack> inputs) {
        ToolStack toolStack = ToolStack.from(original);
        ToolDataNBT data = toolStack.getPersistentData();
        ListTag toolPartsData = new ListTag();
        for (ItemStack input : inputs) {
            CompoundTag statsNbt = input.getTag().getCompound(Tquality.PART_DATA);
            toolPartsData.add(statsNbt);
        }
        data.put(Tquality.modResource(Tquality.TOOL_DATA), toolPartsData);
        return original;
    }

    public static List<Optional<IMaterialStats>> getStatsFromTool(ItemStack stack) {
        ToolStack toolStack = ToolStack.from(stack);
        ToolDataNBT data = toolStack.getPersistentData();

        if (!data.contains(Tquality.modResource(Tquality.TOOL_DATA))) {
            return null;
        }

        List<Optional<IMaterialStats>> stats = new ArrayList<>();

        ListTag toolPartData = (ListTag) data.get(Tquality.modResource(Tquality.TOOL_DATA));
        for (int i = 0; i < toolPartData.size(); i++) {
            if (toolPartData != null && toolPartData.get(i) instanceof CompoundTag tag && tag.contains(Tquality.STATS)) {
                stats.add(Optional.ofNullable(StatsUtils.getStatsFromNbt(tag)));
            } else {
                stats.add(Optional.empty());
            }
        }
        return stats;
    }

    public static void addQualityTooltip(List<Component> tooltip, float quality) {
        tooltip.add(Component.translatable("tquality.tooltip.quality").append(": ").withStyle(ChatFormatting.WHITE).append(
                Component.literal(String.format("%.2f", quality)).withStyle(Style.EMPTY.withColor(ModUtils.getQualityColor(quality)))));
    }
}
