package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Hides ordinary blocks, shows ores/storage (BlockMixin#shouldDrawSide). Not compatible with Sodium. */
public final class XRayModule extends Module {
    public static volatile boolean ACTIVE;
    private static final Map<Block, Boolean> CACHE = new ConcurrentHashMap<>();
    private final BooleanSetting fullbright = add(new BooleanSetting("Fullbright", true));
    private boolean addedNv;

    public XRayModule() { super("Xray", "See ores through walls", Category.RENDER); }

    public static boolean isTarget(BlockState s) {
        return CACHE.computeIfAbsent(s.getBlock(), b -> {
            String p = Registries.BLOCK.getId(b).getPath();
            return p.endsWith("_ore") || p.equals("ancient_debris") || p.endsWith("chest") || p.equals("barrel")
                    || p.endsWith("shulker_box") || p.endsWith("spawner") || p.equals("end_portal_frame")
                    || p.equals("nether_portal") || p.equals("lava") || p.equals("diamond_block") || p.equals("emerald_block");
        });
    }

    private void reload() { if (mc.worldRenderer != null && mc.world != null) mc.worldRenderer.reload(); }

    @Override public void onEnable()  { ACTIVE = true;  reload(); }
    @Override public void onDisable() {
        ACTIVE = false; reload();
        if (addedNv && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        addedNv = false;
    }

    @Override public void onTick() {
        if (!fullbright.get() || mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) return;
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, false, false));
        addedNv = true;
    }
}
