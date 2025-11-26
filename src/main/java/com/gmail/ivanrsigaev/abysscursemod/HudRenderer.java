package com.gmail.ivanrsigaev.abysscursemod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AbyssCurseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HudRenderer {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (minecraft.options.renderDebug) return;
        
        PoseStack poseStack = event.getPoseStack();
        Config config = ConfigSerializer.getConfig();
        var levelKey = minecraft.player.level.dimension().location();
        var levelData = config.levels.get(levelKey);
        if (levelData == null) return;
        var y = (int)Math.floor(minecraft.player.getY());
        var layerData = levelData.findLayer(y);
        if (layerData == null) return;

        var yLimit = levelData.nextHardLimit(layerData, y);
        var dy = Math.abs(y - yLimit);
        String limitText = Integer.toString(dy + 1) + "/" + Integer.toString(layerData.curseActivationHeight);
        String text = "[" + limitText + "] " + layerData.name;
        var w = minecraft.getWindow().getGuiScaledWidth();
        var h = minecraft.getWindow().getGuiScaledHeight();

        // Could add text position to mod config.
        minecraft.font.drawShadow(poseStack, text, w / 20, h / 20, 0xFFFFFF);
    }
}
