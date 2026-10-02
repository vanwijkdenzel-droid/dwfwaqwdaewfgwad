package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import net.minecraft.entity.effect.StatusEffects;

/** Strips blindness / nausea / darkness visuals client-side. */
public final class AntiDebuffModule extends Module {
    public AntiDebuffModule() { super("AntiDebuff", "Removes screen-obscuring effects", Category.WORLD); }

    @Override public void onTick() {
        mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        mc.player.removeStatusEffect(StatusEffects.DARKNESS);
    }
}
