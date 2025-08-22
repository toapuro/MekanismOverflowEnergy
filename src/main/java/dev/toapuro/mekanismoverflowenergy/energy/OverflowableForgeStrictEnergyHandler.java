package dev.toapuro.mekanismoverflowenergy.energy;

import dev.toapuro.mekanismoverflowenergy.Config;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class OverflowableForgeStrictEnergyHandler implements IStrictEnergyHandler {
    private static final FloatingLong INT_MAX = FloatingLong.createConst(Integer.MAX_VALUE);
    private static final IntSupplier maxOperationCountSup = () -> Config.maxOperationCount.get();

    private final IEnergyStorage storage;

    OverflowableForgeStrictEnergyHandler(IEnergyStorage storage) {
        this.storage = storage;
    }

    public static OverflowableForgeStrictEnergyHandler wrap(IEnergyStorage storage) {
        return new OverflowableForgeStrictEnergyHandler(storage);
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public @NotNull FloatingLong getEnergy(int container) {
        return container == 0 ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(this.storage.getEnergyStored()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, @NotNull FloatingLong energy) {
    }

    @Override
    public @NotNull FloatingLong getMaxEnergy(int container) {
        if (container != 0) {
            return FloatingLong.ZERO;
        }

        int maxEnergyInt = this.storage.getMaxEnergyStored();
        if (maxEnergyInt == Integer.MAX_VALUE) {
            return FloatingLong.MAX_VALUE;
        }

        return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(maxEnergyInt);
    }

    @Override
    public @NotNull FloatingLong getNeededEnergy(int container) {
        return container == 0 ? getMaxEnergy(container).subtract(getEnergy(container)) : FloatingLong.ZERO;
    }

    @Override
    public @NotNull FloatingLong insertEnergy(int container, @NotNull FloatingLong amount, @NotNull Action action) {
        if (!(container == 0 && this.storage.canReceive())) {
            return amount;
        }

        FloatingLong feAmount = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertTo(amount);
        if (feAmount.smallerOrEqual(FloatingLong.ZERO)) {
            return amount;
        }

        if (feAmount.greaterOrEqual(INT_MAX) && action.execute()) {
            FloatingLong remainEnergy = feAmount.copy();
            for (int i = 0; i < maxOperationCountSup.getAsInt(); i++) {
                int toInsert = remainEnergy.intValue();
                if (toInsert == 0) {
                    break;
                }

                int received = this.storage.receiveEnergy(toInsert, action.simulate());
                remainEnergy.minusEqual(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(received));
            }

            return remainEnergy;
        }
        int received = this.storage.receiveEnergy(feAmount.intValue(), action.simulate());
        return feAmount.subtract(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(received));
    }

    @Override
    public @NotNull FloatingLong extractEnergy(int container, @NotNull FloatingLong amount, @NotNull Action action) {
        if (!(container == 0 && this.storage.canExtract())) {
            return FloatingLong.ZERO;
        }

        FloatingLong feAmount = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertTo(amount);
        if (feAmount.smallerOrEqual(FloatingLong.ZERO)) {
            return FloatingLong.ZERO;
        }

        if (feAmount.greaterOrEqual(INT_MAX) && action.execute()) {
            FloatingLong totalExtracted = FloatingLong.create(0);
            for (int i = 0; i < maxOperationCountSup.getAsInt(); i++) {
                int extractedInt = this.storage.extractEnergy(feAmount.subtract(totalExtracted).intValue(), action.simulate());
                totalExtracted.plusEqual(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(extractedInt));

                if (extractedInt != Integer.MAX_VALUE) {
                    break;
                }
            }

            return totalExtracted;
        }
        int extracted = this.storage.extractEnergy(feAmount.intValue(), action.simulate());
        return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
    }
}
