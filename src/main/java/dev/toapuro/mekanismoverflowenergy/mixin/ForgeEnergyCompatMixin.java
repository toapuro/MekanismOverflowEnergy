package dev.toapuro.mekanismoverflowenergy.mixin;

import dev.toapuro.mekanismoverflowenergy.energy.OverflowableForgeStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeEnergyCompat.class, remap = false)
public abstract class ForgeEnergyCompatMixin {
    @Shadow
    public abstract Capability<IEnergyStorage> getCapability();

    @Inject(method = "getLazyStrictEnergyHandler", at = @At("HEAD"), cancellable = true)
    public void getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<IStrictEnergyHandler>> cir) {
        cir.setReturnValue(
                CapabilityUtils.getCapability(provider, this.getCapability(), side).lazyMap(OverflowableForgeStrictEnergyHandler::wrap)
        );
    }
}
