package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import net.minecraft.entity.player.PlayerEntity;

/** Glow outline on other players (client-side glowing flag). */
public final class ChamsModule extends Module {
    public ChamsModule() { super("Chams", "Glow outline on players", Category.RENDER); }

    @Override public void onTick() {
        for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player) p.setGlowing(true);
    }

    @Override public void onDisable() {
        if (mc.world != null) for (PlayerEntity p : mc.world.getPlayers()) p.setGlowing(false);
    }
}
