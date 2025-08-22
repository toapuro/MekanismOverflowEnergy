package dev.toapuro.mekanismoverflowenergy;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Integer> maxOperationCount;

    public static void initializeConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("[Mekanism Overflow Energy]");
        maxOperationCount = builder.define("maxOperationCount", 256);

        SPEC = builder.build();
    }
}
