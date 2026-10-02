package dev.cpvp.module.impl;

import dev.cpvp.gui.RenderUtil;
import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.module.ModuleManager;
import dev.cpvp.setting.BooleanSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public final class HudModule extends Module {
    private final BooleanSetting watermark = add(new BooleanSetting("Watermark", true));
    private final BooleanSetting arrayList = add(new BooleanSetting("Module List", true));

    public HudModule() {
        super("HUD", "Watermark and enabled-module list", Category.RENDER);
        setEnabled(true);
    }

    @Override public void onHudRender(DrawContext c) {
        if (mc.options.hudHidden || mc.currentScreen instanceof dev.cpvp.gui.ClickGuiScreen) return;
        if (watermark.get()) {
            int fw = RenderUtil.width("FISH", 1.5f);
            int w = fw + RenderUtil.width("v1", 0.9f) + 20;
            RenderUtil.roundRect(c, 5, 5, 5 + w, 27, 6, 0xB0000000);
            c.fill(5, 12, 7, 20, RenderUtil.ACCENT);
            RenderUtil.text(c, "FISH", 12, 9, RenderUtil.TEXT, 1.5f);
            RenderUtil.text(c, "v1", 12 + fw + 4, 14, RenderUtil.ACCENT, 0.9f);
        }
        if (!arrayList.get()) return;

        List<Module> on = ModuleManager.INSTANCE.all().stream()
                .filter(m -> m.isEnabled() && m != this && m.getCategory() != Category.SETTINGS)
                .sorted(Comparator.comparingInt((Module m) -> RenderUtil.width(m.getName(), 1f)).reversed())
                .toList();
        int y = 6, right = c.getScaledWindowWidth() - 6;
        for (Module m : on) {
            int w = RenderUtil.width(m.getName(), 1f);
            c.fill(right - w - 6, y - 1, right + 2, y + 10, 0x90101010);
            c.fill(right + 1, y - 1, right + 2, y + 10, RenderUtil.ACCENT);
            RenderUtil.text(c, m.getName(), right - w - 2, y, RenderUtil.ACCENT, 1f);
            y += 11;
        }
    }
}
