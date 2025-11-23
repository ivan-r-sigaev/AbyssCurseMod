package com.gmail.ivanrsigaev.abysscursemod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class Config {
    public static class LevelData {
        public int bottomY;
        public boolean isAscensionCurse;
        public List<LayerData> layers;

        public LevelData(int bottomY, boolean isAscensionCurse, List<LayerData> layers) {
            this.bottomY = bottomY;
            this.isAscensionCurse = isAscensionCurse;
            this.layers = layers;
        }

        public LevelData() {
            this(0, true, new ArrayList<>());
        }
    }

    public static class LayerData {
        public String name;
        public int height;
        public int curseActivationHeight;
        public List<EffectData> curseEffects;
        
        public LayerData(String name, int height, int curseActivationHeight, List<EffectData> curseEffects) {
            this.name = name;
            this.height = height;
            this.curseActivationHeight = curseActivationHeight;
            this.curseEffects = curseEffects;
        }

        public LayerData() {
            this("", 0, 0, new ArrayList<>());
        }
    }

    public static class EffectData {
        public MobEffect effect;
        public int duration;
        public int level;

        public EffectData(MobEffect effect, int duration, int level) {
            this.effect = effect;
            this.duration = duration;
            this.level = level;
        }

        public EffectData() {
            this(null, 0,0);
        }
    }

    public boolean fixedCurseLeyers;
    public boolean disableCurseLayers;
    public boolean disableCurseInCreativeMode;
    public Map<ResourceKey<Level>, LevelData> levels;

    public Config(boolean fixedCurseLeyers,
            boolean disableCurseLayers,
            boolean disableCurseInCreativeMode,
            Map<ResourceKey<Level>, LevelData> levels) {
        this.fixedCurseLeyers = fixedCurseLeyers;
        this.disableCurseLayers = disableCurseLayers;
        this.disableCurseInCreativeMode = disableCurseInCreativeMode;
        this.levels = levels;
    }

    public Config() {
        this(false, false, false, new HashMap<>());
    }

    public static Config defaultValues() {
        var config = new Config();
        var overworld = new LevelData(0, true, new ArrayList<>());
        {
            var lower_layer_effect = new ArrayList<EffectData>();
            lower_layer_effect.add(new EffectData(parseMobEffectName("instant_damage"), 1, 1));
            lower_layer_effect.add(new EffectData(parseMobEffectName("blindness"), 10, 1));
            var lower_layer = new LayerData("Lower Abyss Layer", 64, 10, lower_layer_effect);
            overworld.layers.add(lower_layer);
        }
        {
            var top_layer_effect = new ArrayList<EffectData>();
            top_layer_effect.add(new EffectData(parseMobEffectName("nausea"), 5, 1));
            var top_layer = new LayerData("Top Abyss Layer", 64, 20, top_layer_effect);
            overworld.layers.add(top_layer);
        }
        config.levels.put(parseLevelName("overworld"), overworld);
        return config;
    }

    public static Config fromJson(String json) {
        return GSON.fromJson(json, Config.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    private static class MobEffectSerializer implements JsonSerializer<MobEffect> {
        public JsonElement serialize(MobEffect src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class MobEffectDeserializer implements JsonDeserializer<MobEffect> {
        public MobEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                var effectName = json.getAsJsonPrimitive().getAsString();
                return parseMobEffectName(effectName);
            } catch (IllegalStateException e) {}
            throw new JsonParseException("Failed to parse MobEffect from a string.");
        }
    }

    private static class LevelResourceKeySerializer implements JsonSerializer<ResourceKey<Level>> {
        public JsonElement serialize(ResourceKey<Level> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class LevelResourceKeyDeserializer implements JsonDeserializer<ResourceKey<Level>> {
        public ResourceKey<Level> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                var levelName = json.getAsJsonPrimitive().getAsString();
                return parseLevelName(levelName);
            } catch (Exception e) {}
            throw new JsonParseException("Failed to parse ResourceKey<Level> from a string.");
        }
    }

    private static MobEffect parseMobEffectName(String name) 
            throws ParseException {
        try {
            ResourceLocation effectId = ResourceLocation.tryParse(name);
            
            // Try parse `effect` as `minecraft:effect` alias. 
            if (effectId == null && !name.contains(":")) {
                effectId = ResourceLocation.tryParse("minecraft:" + name);
            }
            
            if (effectId != null) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                if (effect != null) {
                    return effect;
                }
            }
        } catch (Exception e) {}
        throw new ParseException("Failed to parse '" + name + "' as a MobEffect.");
    }

    private static ResourceKey<Level> parseLevelName(String name) {
        try {
            ResourceLocation levelId = ResourceLocation.tryParse(name);

            if (levelId != null) {
                return ResourceKey.create(Registry.DIMENSION_REGISTRY, levelId);
            }
        } catch (Exception e) {}
        throw new ParseException("Failed to parse '" + name + "' as ResourceKey<Level>.");
    }

    private static final Type LEVEL_RESOURSE_KEY_TYPE = new TypeToken<ResourceKey<Level>>() {}.getType();
    
    // Do the MobEffect and ResourceKey<Level> need an InstanceCreator 
    // if they already have a serializer and a deserializer?
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LEVEL_RESOURSE_KEY_TYPE, new LevelResourceKeySerializer())
            .registerTypeAdapter(LEVEL_RESOURSE_KEY_TYPE, new LevelResourceKeyDeserializer())
            .registerTypeAdapter(MobEffect.class, new MobEffectSerializer())
            .registerTypeAdapter(MobEffect.class, new MobEffectDeserializer())
            .create();
}
