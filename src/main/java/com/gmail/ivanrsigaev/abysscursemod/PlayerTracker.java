package com.gmail.ivanrsigaev.abysscursemod;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

import com.gmail.ivanrsigaev.abysscursemod.Config.LayerData;
import com.gmail.ivanrsigaev.abysscursemod.Config.LevelData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "abysscursemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTracker {
    private static final Map<UUID, PlayerData> PLAYERS = new HashMap<>();

    private static class PlayerData {
        public ResourceKey<Level> level;
        public int limitY;
        public int prevY;

        public PlayerData(ResourceKey<Level> level, int limitY, int prevY) {
            this.level = level;
            this.limitY = limitY;
            this.prevY = prevY;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        var level = player.level.dimension();
        var y = (int)Math.floor(player.getY());
        var levelData = ConfigStorage.getConfig().levels.get(level);
        PlayerData data;
        if (levelData != null) {
            data = new PlayerData(level, hardLayerLimit(levelData, y).orElse(0), y);
        } else {
            data = new PlayerData(level, 0, 0);
        }
        var uuid = player.getUUID();
        PLAYERS.put(uuid, data);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        var uuid = event.getEntity().getUUID();
        PLAYERS.remove(uuid);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // TODO: implement this...
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        // TODO: implement this...
    }

    private static LayerData findLayer(LevelData levelData, int y) {
        LayerData layerData = null;
        int bottomY = levelData.bottomY;
        if (y < bottomY) {
            return null;
        }
        for (LayerData value : levelData.layers) {
            if (y < bottomY + value.height) {
                layerData = value;
                break;
            }
            bottomY += value.height;
        }
        return layerData;
    }

    private static OptionalInt hardLayerLimit(LevelData levelData, int y) {
        LayerData layerData = null;
        int bottomY = levelData.bottomY;
        if (y < bottomY) {
            return OptionalInt.empty();
        }
        for (LayerData value : levelData.layers) {
            if (y < bottomY + value.height) {
                layerData = value;
                break;
            }
            bottomY += value.height;
        }
        if (layerData == null) {
            return OptionalInt.empty();
        }

        if (levelData.isAscensionCurse) {
            var delta = (bottomY + layerData.height - 1 - y) % layerData.curseActivationHeight;
            return OptionalInt.of(y + delta);
        } else {
            var delta = (y - bottomY) % layerData.curseActivationHeight;
            return OptionalInt.of(y - delta);
        }
    }
}
