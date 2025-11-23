package com.gmail.ivanrsigaev.abysscursemod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

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

        public int nextHardLimit(LayerData layerData, int y) {
            var botY = getLayerBottomY(layerData);
            if (isAscensionCurse) {
                var delta = (botY + layerData.height - 1 - y) % layerData.curseActivationHeight;
                return y + delta;
            } else {
                var delta = (y - botY) % layerData.curseActivationHeight;
                return y - delta;
            }
        }

        public LayerData findLayer(int y) {
            LayerData result = null;
            var currentY = bottomY;
            if (y < currentY) {
                return result;
            }
            for (LayerData value : layers) {
                if (y < currentY + value.height) {
                    result = value;
                    break;
                }
                currentY += value.height;
            }
            return result;
        }

        public int getLayerBottomY(LayerData layerData) {
            var index = layers.indexOf(layerData);
            if (index == -1) {
                throw new IllegalArgumentException();
            }
            var y = bottomY;
            for (var layer : layers.subList(0, index)) {
                y += layer.height;
            }
            return y;
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
            this("Sample Layer", 1, 1, new ArrayList<>());
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
            this(MobEffect.byId(1), 1,1);
        }
    }

    public boolean disableCurseLayers;
    public boolean disableCurseInCreativeMode;
    public Map<ResourceLocation, LevelData> levels;

    public Config(boolean disableCurseLayers,
            boolean disableCurseInCreativeMode,
            Map<ResourceLocation, LevelData> levels) {
        this.disableCurseLayers = disableCurseLayers;
        this.disableCurseInCreativeMode = disableCurseInCreativeMode;
        this.levels = levels;
    }

    public Config() {
        this(false, false, new HashMap<>());
    }
}
