package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class MixinUpdater {
    @Shadow @Final private World world;
    @Shadow private int depth;
    @Inject(method = "runQueuedUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;update(Lnet/minecraft/world/World;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRunQueuedUpdates(CallbackInfo ci, ChainRestrictedNeighborUpdater.Entry entry) {
        UpdateMonitorHelper.onUpdate(world, entry);
    }
    @Inject(method = "runQueuedUpdates", at = @At("RETURN"))
    private void finishUpdates(CallbackInfo ci) {
        UpdateMonitorHelper.onChainFinish(world);
    }
}
