package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class FullbrightModule extends Module {
    private boolean added;
    public FullbrightModule() { super("Fullbright", "See in the dark", Category.RENDER); }

    @Override public void onTick() {
        if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) return;
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, false, false));
        added = true;
    }

    @Override public void onDisable() {
        if (added && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        added = false;
    }
}
