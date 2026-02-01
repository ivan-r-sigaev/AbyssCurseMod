package com.gmail.ivanrsigaev.abysscursemod;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.http.ParseException;

import com.gmail.ivanrsigaev.abysscursemod.Config.EffectData;
import com.gmail.ivanrsigaev.abysscursemod.Config.LayerData;
import com.gmail.ivanrsigaev.abysscursemod.Config.LevelData;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Stores the config as a .json file in the mod's directory.
 */
public class ConfigSerializer {
    public static final String CONFIG_FILENAME = "abyss_curse_config.json";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILENAME);

    public static Config getConfig() {
        return config;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = new String(Files.readAllBytes(CONFIG_PATH));
                config = configFromJson(json);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, configToJson(config));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MobEffectSerializer implements JsonSerializer<MobEffect> {
        public JsonElement serialize(MobEffect src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(ForgeRegistries.MOB_EFFECTS.getKey(src).toString());
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

    private static class ResourceLocationDeserializer implements JsonDeserializer<ResourceLocation> {
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                var levelName = json.getAsJsonPrimitive().getAsString();
                return parseLevelName(levelName);
            } catch (Exception e) {}
            throw new JsonParseException("Failed to parse ResourceLocation from a string.");
        }
    }
    
    // Do the MobEffect and ResourceKey<Level> need an InstanceCreator 
    // if they already have a serializer and a deserializer?
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
            .registerTypeAdapter(MobEffect.class, new MobEffectSerializer())
            .registerTypeAdapter(MobEffect.class, new MobEffectDeserializer())
            .create();
    
    private static Config config = sampleConfig();

    private static Config sampleConfig() {
        var config = new Config();
        var overworld = new LevelData(-64, true, new ArrayList<>());
        {
            var lower_layer_effect = new ArrayList<EffectData>();
            lower_layer_effect.add(new EffectData(parseMobEffectName("instant_damage"), 0, 0));
            lower_layer_effect.add(new EffectData(parseMobEffectName("blindness"), 100, 0));
            var lower_layer = new LayerData("example lower abyss layer", 64, 8, lower_layer_effect);
            overworld.layers.add(lower_layer);
        }
        {
            var upper_layer_effect = new ArrayList<EffectData>();
            upper_layer_effect.add(new EffectData(parseMobEffectName("nausea"), 200, 1));
            var upper_layer = new LayerData("example upper abyss layer", 64, 16, upper_layer_effect);
            overworld.layers.add(upper_layer);
        }
        config.levels.put(parseLevelName("overworld"), overworld);
        return config;
    }

    private static Config configFromJson(String json) {
        return GSON.fromJson(json, Config.class);
    }

    private static String configToJson(Config config) {
        return GSON.toJson(config);
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

    private static ResourceLocation parseLevelName(String name) {
        try {
            return ResourceLocation.tryParse(name);
        } catch (Exception e) {}
        throw new ParseException("Failed to parse '" + name + "' as ResourceKey<Level>.");
    }
}
