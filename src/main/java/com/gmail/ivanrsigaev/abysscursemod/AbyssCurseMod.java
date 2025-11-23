package com.gmail.ivanrsigaev.abysscursemod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AbyssCurseMod.MODID)
public class AbyssCurseMod
{
    public static final String MODID = "abysscursemod";

    public static MinecraftServer server = null; 
    public static CommandSourceStack commandsSource = null; 

    public AbyssCurseMod(FMLJavaModLoadingContext context)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        server = event.getServer();
        commandsSource = server.createCommandSourceStack();

        ConfigSerializer.load();
    }
}
