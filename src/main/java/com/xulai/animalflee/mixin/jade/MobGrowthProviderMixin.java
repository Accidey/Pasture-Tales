package com.xulai.animalflee.mixin.jade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.addon.vanilla.MobGrowthProvider;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(MobGrowthProvider.class)
public class MobGrowthProviderMixin {

    @Inject(method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/EntityAccessor;Lsnownee/jade/api/config/IPluginConfig;)V", at = @At("HEAD"), cancellable = true)
    private void animalflee$hideGrowthCountdown(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config, CallbackInfo ci) {
        ci.cancel();
    }
}