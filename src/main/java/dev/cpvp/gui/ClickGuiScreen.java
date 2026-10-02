package dev.cpvp.gui;

import dev.cpvp.ProfileManager;
import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.module.ModuleManager;
import dev.cpvp.module.impl.InterfaceModule;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.IntConsumer;

import static dev.cpvp.gui.RenderUtil.*;

/**
 * Fish v1 ClickGUI, laid out like Vape V4: nav panel on the left (click an entry to show/hide that window),
 * one window per category, plus Profiles and Settings windows on the right.
 */
public final class ClickGuiScreen extends Screen {
    private static final int HEADER_H = 26, ROW_H = 22, GAP = 8, TOP = 52, NAV_W = 150, RAD = 3;
    private static final List<Category> COLS = List.of(Category.COMBAT, Category.RENDER, Category.UTILITY,
            Category.WORLD, Category.INVENTORY, Category.NETWORK);

    private static final Set<Category> HIDDEN = new HashSet<>();
    private static final Set<Category> COLLAPSED = new HashSet<>();
    private static final Map<Category, Float> COL_ANIM = new HashMap<>();
    private static final Map<Category, float[]> SCROLL = new HashMap<>();
    private static final Map<Module, Float> EXPAND = new HashMap<>();
    private static final Set<Module> OPEN = new HashSet<>();
    private static boolean showProfiles = true, showSettings = false;

    private record Hit(int x, int y, int w, int h, int clipTop, int clipBottom, IntConsumer click) {
        boolean contains(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }
    private record Bounds(int x, int y, int w, int h, Category cat) {}
    private static final int NO_MIN = Integer.MIN_VALUE, NO_MAX = Integer.MAX_VALUE;

    private final List<Hit> hits = new ArrayList<>();
    private final List<Bounds> bounds = new ArrayList<>();
    private Module listening;
    private NumberSetting dragging;
    private int dragX, dragW;
    private double clickX;
    private float kAnim;
    private long lastFrame = System.nanoTime();

    public ClickGuiScreen() { super(Text.literal("Fish v1")); }

    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { ProfileManager.saveCurrent(); }

    private InterfaceModule ui() { return ModuleManager.INSTANCE.get(InterfaceModule.class); }

    private static void window(DrawContext c, int x, int y, int w, int h) {
        roundRect(c, x, y, x + w, y + h, RAD, PANEL);
        outline(c, x, y, x + w, y + h, BORDER);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float tickDelta) {
        ACCENT = ui().accent();
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        kAnim = 1f - (float) Math.exp(-dt * ui().animSpeed.get());
        float kScroll = 1f - (float) Math.exp(-dt * 16f);
        hits.clear();
        bounds.clear();

        if (ui().blur.get() && client != null && client.world != null) applyBlur(tickDelta);
        c.fill(0, 0, width, height, 0x38000000);

        logo(c, 14, 14, 1.0f);
        drawNav(c, 14, TOP, mx, my);

        int rightX = width - 10;
        if (showProfiles) { drawProfiles(c, rightX - 150, TOP); rightX -= 158; }
        if (showSettings) { drawSettingsWindow(c, rightX - 190, TOP, 190); rightX -= 198; }

        List<Category> vis = new ArrayList<>();
        for (Category k : COLS) if (!HIDDEN.contains(k)) vis.add(k);
        int startX = 14 + NAV_W + GAP, n = vis.size();
        if (n == 0) return;
        int colW = MathHelper.clamp((rightX - startX - (n - 1) * GAP) / n, 100, 150);
        int maxBody = height - TOP - 12 - HEADER_H;
        for (int i = 0; i < n; i++) drawColumn(c, vis.get(i), startX + i * (colW + GAP), TOP, colW, maxBody, mx, my, kScroll);
    }

    // ---------- nav ----------
    private void drawNav(DrawContext c, int x, int y, int mx, int my) {
        int h = 6 + COLS.size() * 26 + 22 + 2 * 26 + 6;
        window(c, x, y, NAV_W, h);
        int ry = y + 6;
        for (Category cat : COLS) {
            boolean on = !HIDDEN.contains(cat);
            ry = navRow(c, x, ry, mx, my, cat.ordinal(), cat.label, on, b -> { if (!HIDDEN.remove(cat)) HIDDEN.add(cat); });
        }
        c.fill(x + 12, ry + 10, x + 26, ry + 11, BORDER);
        text(c, "MISC", x + 32, ry + 6, DIM, 0.7f);
        c.fill(x + 56, ry + 10, x + NAV_W - 12, ry + 11, BORDER);
        ry += 22;
        ry = navRow(c, x, ry, mx, my, 7, "Profiles", showProfiles, b -> showProfiles = !showProfiles);
        navRow(c, x, ry, mx, my, 6, "Settings", showSettings, b -> showSettings = !showSettings);
    }

    private int navRow(DrawContext c, int x, int y, int mx, int my, int icon, String label, boolean on, IntConsumer click) {
        boolean hov = mx >= x && mx < x + NAV_W && my >= y && my < y + 26;
        if (hov) roundRect(c, x + 4, y, x + NAV_W - 4, y + 25, 2, ROW_HOV);
        icon(c, icon, x + 14, y + 5, on ? ACCENT : DIM);
        text(c, label, x + 38, y + 7, on ? TEXT : DIM, 0.95f);
        chevronRight(c, x + NAV_W - 18, y + 10, on ? ACCENT : DIM);
        hits.add(new Hit(x, y, NAV_W, 26, NO_MIN, NO_MAX, click));
        return y + 26;
    }

    // ---------- category window ----------
    private void drawColumn(DrawContext c, Category cat, int x, int y0, int colW, int maxBody, int mx, int my, float kScroll) {
        List<Module> mods = ModuleManager.INSTANCE.byCategory(cat);

        float ca = COL_ANIM.getOrDefault(cat, 1f), goal = COLLAPSED.contains(cat) ? 0f : 1f;
        ca += (goal - ca) * kAnim;
        if (Math.abs(goal - ca) < 0.01f) ca = goal;
        COL_ANIM.put(cat, ca);

        float content = 2;
        for (Module m : mods) {
            float e = EXPAND.getOrDefault(m, 0f), g = OPEN.contains(m) ? 1f : 0f;
            e += (g - e) * kAnim;
            if (Math.abs(g - e) < 0.01f) e = g;
            EXPAND.put(m, e);
            content += ROW_H + Math.round(settingsHeight(m, true) * e) + 1;
        }
        content += 3;
        int bodyH = Math.round(Math.min(content, maxBody) * ca);

        float[] sc = SCROLL.computeIfAbsent(cat, k -> new float[2]);
        sc[0] = MathHelper.clamp(sc[0], 0, Math.max(0, content - maxBody));
        sc[1] += (sc[0] - sc[1]) * kScroll;
        if (Math.abs(sc[0] - sc[1]) < 0.05f) sc[1] = sc[0];

        window(c, x, y0, colW, HEADER_H + bodyH);
        icon(c, cat.ordinal(), x + 9, y0 + 6, ACCENT);
        text(c, cat.label, x + 30, y0 + 8, TEXT, 0.95f);
        if (COLLAPSED.contains(cat)) chevronDown(c, x + colW - 17, y0 + 11, DIM);
        else chevronUp(c, x + colW - 17, y0 + 11, DIM);
        if (bodyH > 0) c.fill(x + 1, y0 + HEADER_H - 1, x + colW - 1, y0 + HEADER_H, BORDER);
        hits.add(new Hit(x, y0, colW, HEADER_H, NO_MIN, NO_MAX, b -> { if (!COLLAPSED.remove(cat)) COLLAPSED.add(cat); }));
        bounds.add(new Bounds(x, y0, colW, HEADER_H + bodyH, cat));
        if (bodyH <= 0) return;

        final int clipTop = y0 + HEADER_H, clipBottom = y0 + HEADER_H + bodyH;
        c.enableScissor(x + 1, clipTop, x + colW - 1, clipBottom);
        float y = clipTop + 2 - sc[1];
        for (Module m : mods) {
            float e = EXPAND.getOrDefault(m, 0f);
            final int rx = x + 3, rw = colW - 6, top = Math.round(y);
            int setH = Math.round(settingsHeight(m, true) * e);

            if (top + ROW_H + setH > clipTop && top < clipBottom) {
                boolean hv = mx >= rx && mx < rx + rw && my >= top && my < top + ROW_H && my >= clipTop && my <= clipBottom;
                int bg = m.isEnabled() ? alpha(ACCENT, 0.16f) : (hv ? ROW_HOV : 0);
                if (bg != 0) roundRect(c, rx, top, rx + rw, top + ROW_H, 2, bg);
                if (m.isEnabled()) c.fill(rx, top + 4, rx + 2, top + ROW_H - 4, ACCENT);
                text(c, m.getName(), rx + 9, top + 6, m.isEnabled() ? ACCENT : TEXT, 0.9f);
                if (m.getKey() >= 0) {
                    String kn = keyName(m.getKey());
                    text(c, kn, rx + rw - 18 - width(kn, 0.7f), top + 8, DIM, 0.7f);
                }
                dots(c, rx + rw - 11, top + 5, hv || OPEN.contains(m) ? TEXT : DIM);

                hits.add(new Hit(rx, top, rw - 18, ROW_H, clipTop, clipBottom, b -> {
                    if (b == 0) m.toggle(); else if (b == 1) toggleOpen(m);
                }));
                hits.add(new Hit(rx + rw - 18, top, 18, ROW_H, clipTop, clipBottom, b -> toggleOpen(m)));
                if (setH > 1) drawSettings(c, m, rx, rw, top + ROW_H, setH, e > 0.95f, clipTop, clipBottom, true);
            }
            y += ROW_H + setH + 1;
        }
        c.disableScissor();

        float max = Math.max(0, content - maxBody);
        if (max > 0) {
            int barH = Math.max(14, (int) (bodyH * (bodyH / content)));
            int barY = clipTop + (int) ((bodyH - barH) * (sc[1] / max));
            c.fill(x + colW - 3, barY, x + colW - 1, barY + barH, 0xFF3A3A3A);
        }
    }

    // ---------- profiles window ----------
    private void drawProfiles(DrawContext c, int x, int y) {
        final int w = 150;
        List<String> names = ProfileManager.names();
        int shown = Math.min(names.size(), 12);
        int h = HEADER_H + 6 + 22 + 6 + shown * 22 + 6;
        window(c, x, y, w, h);
        icon(c, 7, x + 9, y + 6, ACCENT);
        text(c, "Profiles", x + 30, y + 8, TEXT, 0.95f);
        c.fill(x + 1, y + HEADER_H - 1, x + w - 1, y + HEADER_H, BORDER);

        int by = y + HEADER_H + 6;
        roundRect(c, x + 6, by, x + w - 6, by + 22, 2, 0xFF0E0E0E);
        outline(c, x + 6, by, x + w - 6, by + 22, BORDER);
        centerText(c, "+  CREATE NEW", x + w / 2, by + 6, TEXT, 0.8f);
        hits.add(new Hit(x + 6, by, w - 12, 22, NO_MIN, NO_MAX, b -> ProfileManager.create()));

        int ry = by + 28;
        for (int i = 0; i < shown; i++) {
            final String name = names.get(i);
            boolean sel = name.equals(ProfileManager.current);
            if (sel) roundRect(c, x + 6, ry, x + w - 6, ry + 20, 2, 0xFFEDEDED);
            text(c, name, x + 14, ry + 5, sel ? 0xFF000000 : TEXT, 0.9f);
            hits.add(new Hit(x + 6, ry, w - 12, 20, NO_MIN, NO_MAX, b -> {
                if (b == 0) ProfileManager.load(name); else if (b == 1) ProfileManager.delete(name);
            }));
            ry += 22;
        }
        text(c, "click: load  |  right-click: delete", x + 8, y + h + 3, DIM, 0.6f);
    }

    // ---------- settings window ----------
    private void drawSettingsWindow(DrawContext c, int x, int y, int w) {
        Module ifc = ui();
        int bodyH = settingsHeight(ifc, false) + 4;
        window(c, x, y, w, HEADER_H + bodyH);
        icon(c, 6, x + 9, y + 6, ACCENT);
        text(c, "Settings", x + 30, y + 8, TEXT, 0.95f);
        c.fill(x + 1, y + HEADER_H - 1, x + w - 1, y + HEADER_H, BORDER);
        drawSettings(c, ifc, x + 3, w - 6, y + HEADER_H + 1, bodyH, true, NO_MIN, NO_MAX, false);
    }

    private void toggleOpen(Module m) { if (!OPEN.remove(m)) OPEN.add(m); }

    private void drawSettings(DrawContext c, Module m, int rx, int rw, int startY, int setH, boolean interact,
                              int clipTop, int clipBottom, boolean keybind) {
        c.enableScissor(rx, startY, rx + rw, startY + setH);
        c.fill(rx + 1, startY, rx + rw - 1, startY + setH - 1, INNER);
        int sy = startY + 2;
        final int ix = rx + 9, iw = rw - 18;

        if (keybind) {
            text(c, "Keybind", ix, sy + 5, DIM, 0.8f);
            String kt = listening == m ? "press a key..." : (m.getKey() < 0 ? "NONE" : keyName(m.getKey()));
            int kw = width(kt, 0.75f) + 10, kx = ix + iw - kw;
            roundRect(c, kx, sy + 2, kx + kw, sy + 16, 2, TRACK);
            text(c, kt, kx + 5, sy + 5, listening == m ? ACCENT : TEXT, 0.75f);
            if (interact) hits.add(new Hit(kx, sy + 2, kw, 14, clipTop, clipBottom, b -> listening = (listening == m) ? null : m));
            sy += 20;
        }

        for (Setting s : m.getSettings()) {
            if (s instanceof BooleanSetting bs) {
                text(c, s.getName(), ix, sy + 5, TEXT, 0.8f);
                checkbox(c, ix + iw - 13, sy + 3, 13, bs.get());
                if (interact) hits.add(new Hit(ix, sy, iw, 20, clipTop, clipBottom, b -> bs.toggle()));
                sy += 20;
            } else if (s instanceof NumberSetting ns) {
                text(c, s.getName(), ix, sy + 3, TEXT, 0.8f);
                String v = ns.display();
                text(c, v, ix + iw - width(v, 0.8f), sy + 3, ACCENT, 0.8f);
                final int tx = ix, tw = iw, ty = sy + 17;
                double f = (ns.get() - ns.min()) / (ns.max() - ns.min());
                int fw = (int) (tw * f);
                roundRect(c, tx, ty, tx + tw, ty + 4, 1, TRACK);
                if (fw > 0) roundRect(c, tx, ty, tx + Math.max(fw, 3), ty + 4, 1, ACCENT);
                roundRect(c, tx + fw - 3, ty - 2, tx + fw + 3, ty + 6, 2, 0xFFFFFFFF);
                if (interact) hits.add(new Hit(tx - 3, ty - 6, tw + 6, 16, clipTop, clipBottom, b -> {
                    dragging = ns; dragX = tx; dragW = tw; updateSlider(clickX);
                }));
                sy += 27;
            }
        }
        c.disableScissor();
    }

    private int settingsHeight(Module m, boolean keybind) {
        int h = keybind ? 24 : 4;
        for (Setting s : m.getSettings()) h += (s instanceof NumberSetting) ? 27 : 20;
        return h + 4;
    }

    private void updateSlider(double mouseX) {
        if (dragging == null) return;
        double f = MathHelper.clamp((mouseX - dragX) / (double) dragW, 0, 1);
        dragging.set(dragging.min() + f * (dragging.max() - dragging.min()));
    }

    private static String keyName(int key) {
        return InputUtil.fromKeyCode(key, -1).getLocalizedText().getString().toUpperCase(Locale.ROOT);
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        clickX = mx;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (my < h.clipTop() || my > h.clipBottom()) continue;
            if (h.contains(mx, my)) { h.click().accept(button); return true; }
        }
        listening = null;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) { updateSlider(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        for (Bounds b : bounds) {
            if (mx < b.x() || mx >= b.x() + b.w() || my < b.y() || my >= b.y() + b.h()) continue;
            SCROLL.computeIfAbsent(b.cat(), k -> new float[2])[0] -= (float) (vertical * ui().scrollSpeed.get());
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (listening != null) {
            boolean clear = key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE;
            listening.setKey(clear ? -1 : key);
            listening = null;
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(key, scancode, mods);
    }
}
