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
        
        PoseStack poseStack = event.getPoseStack();
        Config config = ConfigSerializer.getConfig();
        var levelKey = minecraft.player.level.dimension().location();
        var levelData = config.levels.get(levelKey);
        var y = (int)Math.floor(minecraft.player.getY());
        var layerData = levelData.findLayer(y);
        if (layerData == null) return;

        var yLimit = levelData.nextHardLimit(layerData, y);
        var dy = Math.abs(y - yLimit);
        String text = Integer.toString(dy + 1) + "/" + Integer.toString(layerData.curseActivationHeight);
        
        minecraft.font.drawShadow(poseStack, text, 10, 10, 0xFFFFFF);
    }
}
