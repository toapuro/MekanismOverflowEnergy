package dev.toapuro.mekanismoverflowenergy.energy;

import dev.toapuro.mekanismoverflowenergy.Config;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class OverflowableForgeStrictEnergyHandler implements IStrictEnergyHandler {
    private static final FloatingLong INT_MAX_J = FloatingLong.createConst(Integer.MAX_VALUE);
    private static final IntSupplier maxOperationCount = () -> Config.maxOperationCount.get();

    @Nullable
    private final IEnergyStorage storage;
    private final IStrictEnergyHandler handler;

    OverflowableForgeStrictEnergyHandler(@Nullable IEnergyStorage storage, IStrictEnergyHandler handler) {
        this.storage = storage;
        this.handler = handler;
    }

    public static OverflowableForgeStrictEnergyHandler wrap(@Nullable IEnergyStorage storage, IStrictEnergyHandler handler) {
        return new OverflowableForgeStrictEnergyHandler(storage, handler);
    }

    @Override
    public int getEnergyContainerCount() {
        return this.handler.getEnergyContainerCount();
    }

    @Override
    public @NotNull FloatingLong getEnergy(int container) {
        return this.handler.getEnergy(container);
    }

    @Override
    public void setEnergy(int container, @NotNull FloatingLong energy) {
        this.handler.setEnergy(container, energy);
    }

    @Override
    public @NotNull FloatingLong getMaxEnergy(int container) {
        if (container != 0) {
            return FloatingLong.ZERO;
        }

        FloatingLong maxEnergy = this.handler.getMaxEnergy(container);

        // When the energy storage capacity is INT_MAX, it appears to be the maximum capacity for Long
        if (this.storage != null && this.storage.getMaxEnergyStored() == Integer.MAX_VALUE) {
            return FloatingLong.MAX_VALUE;
        }

        return maxEnergy;
    }

    @Override
    public @NotNull FloatingLong getNeededEnergy(int container) {
        return container == 0 ? getMaxEnergy(container).subtract(getEnergy(container)) : FloatingLong.ZERO;
    }

    @Override
    public @NotNull FloatingLong insertEnergy(int container, @NotNull FloatingLong amount, @NotNull Action action) {
        if (!(container == 0 && (this.storage != null && this.storage.canReceive()))) {
            return amount;
        }

        if (amount.greaterOrEqual(INT_MAX_J)) {
            // In the simulation, it pretends to have inserted everything
            if(action.simulate()) return FloatingLong.ZERO;

            FloatingLong toInsert = amount.copy();
            for (int i = 0; i < maxOperationCount.getAsInt(); i++) {
                if (toInsert.isZero()) {
                    break;
                }

                FloatingLong lastEnergy = toInsert.copy();
                toInsert = this.handler.insertEnergy(toInsert, action);

                if(lastEnergy.equals(toInsert)) {
                    break;
                }
            }

            return toInsert;
        }

        return this.handler.insertEnergy(amount, action);
    }

    @Override
    public @NotNull FloatingLong extractEnergy(int container, @NotNull FloatingLong amount, @NotNull Action action) {
        if (!(container == 0 && (this.storage != null && this.storage.canExtract()))) {
            return FloatingLong.ZERO;
        }

        if (amount.greaterOrEqual(INT_MAX_J)) {
            // In the simulation, it pretends to have extracted everything
            if(action.simulate()) return amount;

            FloatingLong toExtract = amount.copy();

            for (int i = 0; i < maxOperationCount.getAsInt(); i++) {
                if (toExtract.smallerOrEqual(FloatingLong.ZERO)) {
                    return amount;
                }

                FloatingLong extracted = this.handler.extractEnergy(toExtract, action);
                toExtract.minusEqual(extracted);

                if(extracted.isZero()) {
                    break;
                }
            }

            return amount.subtract(toExtract);
        }
        return this.handler.extractEnergy(container, amount, action);
    }
}
