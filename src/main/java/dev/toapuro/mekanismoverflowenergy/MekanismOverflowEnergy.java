package dev.toapuro.mekanismoverflowenergy;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekanismOverflowEnergy.MODID)
public class MekanismOverflowEnergy {

    public static final String MODID = "mekanismoverflowenergy";

    @SuppressWarnings("removal")
    public MekanismOverflowEnergy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Config.initializeConfig();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
