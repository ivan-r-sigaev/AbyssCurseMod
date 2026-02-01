package com.gmail.ivanrsigaev.abysscursemod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gmail.ivanrsigaev.abysscursemod.Config.LayerData;
import com.gmail.ivanrsigaev.abysscursemod.Config.LevelData;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "abysscursemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTracker {
    private static final Map<UUID, PlayerData> PLAYERS = new HashMap<>();

    private static class PlayerData {
        public LevelData levelData;
        public LayerData layerData;
        public int yLimit;
        public int y;

        public PlayerData(LevelData levelData, LayerData layerData, int yLimit, int y) {
            this.levelData = levelData;
            this.layerData = layerData;
            this.yLimit = yLimit;
            this.y = y;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        PLAYERS.put(player.getUUID(), getNewPlayerData(player));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        var player = event.getEntity();
        PLAYERS.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        var config = ConfigSerializer.getConfig();
        if (!event.side.isServer()) {
            return;
        }
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }

        var player = event.player;
        var playerData = PLAYERS.get(player.getUUID());
        var yNew = (int)Math.floor(player.getY());
        if (yNew == playerData.y) {
            return;
        }

        if (playerData.layerData != null) {
            var canApplyCurse = 
                    !config.disableCurseLayers
                    && !(config.disableCurseInCreativeMode && player.noPhysics);
            var hasTriggeredCurse = 
                    playerData.levelData.isAscensionCurse && yNew > playerData.yLimit
                    || !playerData.levelData.isAscensionCurse && yNew < playerData.yLimit;
            if (canApplyCurse && hasTriggeredCurse) {
                for (var effectData : playerData.layerData.curseEffects) {
                    var mobEffectInstance = new MobEffectInstance(
                            effectData.effect,
                            effectData.duration,
                            effectData.level);
                    player.addEffect(mobEffectInstance);
                }
            }
        }

        var newPlayerData = getNewPlayerData(player);

        PLAYERS.put(player.getUUID(), newPlayerData);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        var playerData = getNewPlayerData(player);

        // Could add a feature to punish certain dimension transitions here...

        PLAYERS.put(player.getUUID(), playerData);
    }

    private static PlayerData getNewPlayerData(Player player) {
        var config = ConfigSerializer.getConfig();
        var levelKey = player.level.dimension().location();
        var levelData = config.levels.get(levelKey);
        var y = (int)Math.floor(player.getY());
        var data = new PlayerData(null, null, 0, y);
        if (levelData != null) {
            var layerData = levelData.findLayer(y);
            if (layerData != null) {
                var yLimit = levelData.nextHardLimit(layerData, y);
                data = new PlayerData(levelData, layerData, yLimit, y);
            }
        }
        return data;
    }
}
