package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.UpdaterData;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

import static com.github.zly2006.reden.access.ServerData.data;
import static com.github.zly2006.reden.access.UpdaterData.updaterData;

@Mixin(value = ChainRestrictedNeighborUpdater.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class Mixin119Updater implements NeighborUpdater, UpdaterData.UpdaterDataAccess {
    @Shadow private int depth;

    @Shadow @Final private World world;

    @Shadow @Final private ArrayDeque<ChainRestrictedNeighborUpdater.Entry> queue;

    @Shadow @Final private List<ChainRestrictedNeighborUpdater.Entry> pending;

    @Unique private final UpdaterData updaterData = new UpdaterData(this);

    @Unique boolean shouldEntryStop = false;

    @Override
    public void yieldUpdater() {
        // already overridden
        runQueuedUpdates();
    }

    @NotNull
    @Override
    public UpdaterData getRedenUpdaterData() {
        return updaterData;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void runQueuedUpdates() {
        if (updaterData.thenTickUpdate) {
            // To keep injecting points, we need to call the original method
            // Call this method with `thenTickUpdate` = true to tick update
            shouldEntryStop = updaterData.tickingEntry.update(this.world);
            updaterData.thenTickUpdate = false;
        }
        else {
            beforeUpdate();
            try {
                while (!this.queue.isEmpty() || !this.pending.isEmpty()) {
                    for (int i = this.pending.size() - 1; i >= 0; --i) {
                        this.queue.push(this.pending.get(i));
                    }

                    this.pending.clear();

                    // Reden start
                    updaterData.tickingEntry = this.queue.peek();

                    while (this.pending.isEmpty()) {
                        // do tick by our method
                        updaterData.tickNextStage();

                        if (!shouldEntryStop) {
                            // Reden stop

                            // Note: call update multiple times is only used by six-way entries
                            this.queue.pop();
                            break;
                        }
                    }
                }
            } finally {
                this.queue.clear();
                this.pending.clear();
                this.depth = 0;
            }
        }
    }

    @Unique private void beforeUpdate() {
        if (!world.isClient) {
            UpdaterData updaterData = updaterData(this);
            ServerData serverData = data(Objects.requireNonNull(world.getServer(), "R-Debugger is not available on clients!"));
            updaterData.setCurrentParentTickStage(serverData.getTickStageTree().peekLeaf());
        }
    }
}
