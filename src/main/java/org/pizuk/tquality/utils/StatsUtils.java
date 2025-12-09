package org.pizuk.tquality.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
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

import java.util.List;

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

    public static float modifyStatsEntry(float value, MaterialId materialId, float quality) {
        return value * ModUtils.normalDistribution(1.0f, 0.5f * (1.05f - quality));
    }

    public static int modifyStatsEntry(int value, MaterialId materialId, float quality) {
        return (int) (value * ModUtils.normalDistribution(1.0f, 0.5f * (1.05f - quality)));
    }

    public static String modifyStatsEntry(Tier tier, MaterialId material, float quality) {
        return tier.toString();
    }

    public static ItemStack getModifiedPartItem(ItemStack original, ItemStack materialStack, float quality, float seed, boolean isCrafted) {
        if (original.isEmpty()
                || !materialStack.hasTag()
                || !materialStack.getTag().contains(Tquality.QUALITY)
                || !(original.getItem() instanceof ToolPartItem part))
            return original;

        ItemStack stack = original.copy();
        MaterialId material = part.getMaterial(stack).getId();
        float finalQuality;
        if (quality == -1) {
            finalQuality = materialStack.getTag().getFloat(Tquality.QUALITY);
        } else {
            finalQuality = quality;
        }

        MaterialRegistry.getInstance().getMaterialStats(material, part.materialStatId).ifPresent(stat -> {
            JsonObject json = new JsonObject();
            serializeStats(stat).entrySet().forEach(entry -> {
                JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    Number num = primitive.getAsNumber();
                    if (num instanceof Float || num instanceof Double)
                        json.addProperty(entry.getKey(), modifyStatsEntry(num.floatValue(), material, finalQuality));
                    if (num instanceof Integer || num instanceof Long)
                        json.addProperty(entry.getKey(), modifyStatsEntry(num.intValue(), material, finalQuality));
                } else if (primitive.isString()) {
                    Tier tier = TierSortingRegistry.byName(ResourceLocation.parse(primitive.getAsString()));
                    if (tier != null) {
                        json.addProperty(entry.getKey(), modifyStatsEntry(tier, material, finalQuality));
                    }
                }
            });
            CompoundTag statsNbt = new CompoundTag();
            statsNbt.put(Tquality.TQUALITY_STATS, jsonToStatsNbt(json));
            statsNbt.putString(Tquality.TQUALITY_STATS_TYPE, stat.getType().getId().toString());
            stack.getTag().put(Tquality.TQUALITY_DATA, statsNbt);
        });
        return stack;
    }

    public static ItemStack getModifiedPartItem(ItemStack original, ItemStack materialStack, boolean isCrafted) {
        return getModifiedPartItem(original, materialStack, -1, -1, isCrafted);
    }

    public static IMaterialStats getStatsFromNbt(CompoundTag tag) {
        MaterialStatsId statsId = new MaterialStatsId(tag.getString(Tquality.TQUALITY_STATS_TYPE));
        IMaterialStats stats = MaterialRegistry.getInstance().getMaterialStats(MaterialIds.manyullyn, new MaterialStatsId(statsId)).orElse(null);
        if (stats != null) {
            return StatsUtils.deserializeStats(tag.getCompound(Tquality.TQUALITY_STATS), stats.getType());
        }
        return null;
    }

    public static ItemStack getModifiedToolItem(ItemStack original, List<ItemStack> inputs, boolean isCrafted) {
        ToolStack toolStack = ToolStack.from(original);
        ToolDataNBT data = toolStack.getPersistentData();
        ListTag toolPartsData = new ListTag();
        for (ItemStack input : inputs) {
            CompoundTag statsNbt = input.getTag().getCompound(Tquality.TQUALITY_DATA);
            toolPartsData.add(statsNbt);
        }
        data.put(Tquality.modResource(Tquality.TQUALITY_TOOL_PART_DATA), toolPartsData);
        return original;
    }
}
