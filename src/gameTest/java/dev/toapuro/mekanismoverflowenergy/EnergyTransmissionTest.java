package dev.toapuro.mekanismoverflowenergy;

import com.jerry.mekanism_extras.common.registry.ExtraBlock;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PrefixGameTestTemplate(false)
@GameTestHolder(MekanismOverflowEnergy.MODID)
public class EnergyTransmissionTest {
    private static final int SETUP_TICKS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(EnergyTransmissionTest.class);

    public static IStrictEnergyHandler getEnergyCapability(BlockEntity blockEntity) {
        LazyOptional<IStrictEnergyHandler> capability = CapabilityUtils.getCapability(blockEntity, Capabilities.STRICT_ENERGY, null);
        if(!capability.isPresent()) {
            RuntimeException exception = new RuntimeException("Could not get energy capability");
            LOGGER.error("{}", blockEntity.getClass(), exception);
            throw exception;
        }

        return capability.orElseThrow(IllegalStateException::new);
    }

    @GameTest(template = "transmission", setupTicks = SETUP_TICKS, batch = "1")
    public static void transmissionTest(GameTestHelper helper) {
        IStrictEnergyHandler sourceCube = getEnergyCapability(helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FloatingLong sourceEnergy = sourceCube.getMaxEnergy(0);
        sourceCube.setEnergy(0, sourceEnergy);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(0, 1, 2, ExtraBlock.INFINITE_UNIVERSAL_CABLE.getBlock());
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    IStrictEnergyHandler capability = getEnergyCapability(helper.getBlockEntity(new BlockPos(0, 1, 3)));
                    FloatingLong energy = capability.getEnergy(0);

                    if(!sourceEnergy.equals(energy)) {
                        helper.fail("Energy amount mismatch");
                    }
                })
                .thenSucceed();
    }
}
