package com.aeternum.client;

import com.aeternum.data.PlayerData;
import com.aeternum.registry.ModAttachments;
import com.aeternum.systems.titles.TitleSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side HUD overlay.
 * Draws HP / Energy / Stamina bars, class info, karma, and temperature
 * in the bottom-left corner. Rendered AFTER vanilla GUI so it overlays
 * on top without needing to cancel vanilla layers.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    private static final int BAR_W   = 120;
    private static final int BAR_H   = 6;
    private static final int BAR_X   = 8;
    private static final int PAD     = 3;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.player.isSpectator()) return;

        PlayerData data;
        try {
            data = mc.player.getData(ModAttachments.PLAYER_DATA.get());
        } catch (Exception ignored) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int screenW = mc.getWindow().getGuiScaledWidth();

        // Starting Y — leave room for hotbar (bottom 22px) + XP bar
        int y = screenH - 80;

        // ── HP BAR ────────────────────────────────────────────────────────────
        double hpPct   = data.getHealthPercent();
        int hpColor    = hpPct > 0.5 ? 0xFF22CC44 : (hpPct > 0.25 ? 0xFFDDAA00 : 0xFFCC2222);
        drawBar(g, BAR_X, y, BAR_W, BAR_H, hpPct, hpColor);
        g.drawString(mc.font,
            "§cHP §f" + (int)data.getCurrentHealth() + "§7/§f" + (int)data.getMaxHealth(),
            BAR_X + BAR_W + 4, y - 1, 0xFFFFFFFF, true);
        y += BAR_H + PAD;

        // ── ENERGY BAR ───────────────────────────────────────────────────────
        double enPct = data.getMaxEnergy() > 0 ? data.getCurrentEnergy() / data.getMaxEnergy() : 0;
        drawBar(g, BAR_X, y, BAR_W, BAR_H, enPct, 0xFF2255EE);
        g.drawString(mc.font,
            "§9EN §f" + (int)data.getCurrentEnergy() + "§7/§f" + (int)data.getMaxEnergy(),
            BAR_X + BAR_W + 4, y - 1, 0xFFAABBFF, true);
        y += BAR_H + PAD;

        // ── STAMINA BAR ───────────────────────────────────────────────────────
        double stPct = data.getMaxStamina() > 0 ? data.getCurrentStamina() / data.getMaxStamina() : 0;
        drawBar(g, BAR_X, y, BAR_W, BAR_H, stPct, 0xFFDDCC00);
        g.drawString(mc.font,
            "§eST §f" + (int)data.getCurrentStamina() + "§7/§f" + (int)data.getMaxStamina(),
            BAR_X + BAR_W + 4, y - 1, 0xFFFFFFAA, true);
        y += BAR_H + PAD + 5;

        // ── CLASS / LEVEL LINE ───────────────────────────────────────────────
        g.drawString(mc.font,
            "§eLv." + data.getLevel() + " §7[§f" + data.getPlayerClass() + "§7]",
            BAR_X, y, 0xFFFFFFFF, true);
        y += 10;

        // ── ACTIVE TITLE ─────────────────────────────────────────────────────
        if (!data.getActiveTitle().isEmpty()) {
            TitleSystem.AeternumTitle t = TitleSystem.ALL_TITLES.get(data.getActiveTitle());
            if (t != null) {
                g.drawString(mc.font, t.displayName(), BAR_X, y, 0xFFFFD700, true);
                y += 10;
            }
        }

        // ── KARMA ─────────────────────────────────────────────────────────────
        int karmaCol = switch (data.getKarmaLevel()) {
            case DIVINE, HOLY, VIRTUOUS, GOOD -> 0xFF55FF55;
            case NEUTRAL -> 0xFFAAAAAA;
            case SHADY, WICKED -> 0xFFFF5555;
            case CORRUPT, ABYSSAL -> 0xFFAA0000;
        };
        g.drawString(mc.font,
            data.getKarmaLevel().name() + " (" + data.getKarma() + ")",
            BAR_X, y, karmaCol, true);
        y += 10;

        // ── TEMPERATURE ───────────────────────────────────────────────────────
        int tempCol = switch (data.getTemperatureStatus()) {
            case NORMAL -> 0xFF55FF55;
            case HOT    -> 0xFFFFFF55;
            case HEAT_EXHAUSTION, HEAT_STROKE -> 0xFFFF5555;
            case COLD   -> 0xFF55FFFF;
            case HYPOTHERMIA, FROSTBITE -> 0xFF5555FF;
        };
        g.drawString(mc.font,
            "Temp: " + String.format("%.1f", data.getBodyTemperature()) + "°C",
            BAR_X, y, tempCol, true);

        // ── XP BAR (replaces vanilla look, centered above hotbar) ────────────
        int xpW = 182;
        int xpX = (screenW - xpW) / 2;
        int xpY = screenH - 29;
        double xpPct = data.getXpForNextLevel() > 0
            ? (double) data.getExperience() / data.getXpForNextLevel() : 0;
        // Background line
        g.fill(xpX, xpY, xpX + xpW, xpY + 5, 0xFF222222);
        // Fill
        int fill = (int)(xpW * Math.min(1.0, xpPct));
        if (fill > 0) g.fill(xpX, xpY, xpX + fill, xpY + 5, 0xFF22AA22);
        // Label
        g.drawCenteredString(mc.font,
            "Lv." + data.getLevel() + "  " + data.getExperience() + "/" + data.getXpForNextLevel() + " XP",
            screenW / 2, xpY - 9, 0xFF88FF88);
    }

    // ── DRAW HELPER ───────────────────────────────────────────────────────────

    private void drawBar(GuiGraphics g, int x, int y, int w, int h, double pct, int fillColor) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000); // border
        g.fill(x, y, x + w, y + h, 0xFF2A2A2A);                  // background
        int fw = (int)(w * Math.max(0, Math.min(1, pct)));
        if (fw > 0) g.fill(x, y, x + fw, y + h, fillColor);      // fill
        // Shine effect (1px lighter top strip)
        if (fw > 0) g.fill(x, y, x + fw, y + 1, blendColor(fillColor, 0xFFFFFFFF, 0.25f));
    }

    private int blendColor(int base, int overlay, float alpha) {
        int br = (base >> 16) & 0xFF, bg = (base >> 8) & 0xFF, bb = base & 0xFF;
        int or = (overlay >> 16) & 0xFF, og = (overlay >> 8) & 0xFF, ob = overlay & 0xFF;
        int r = (int)(br + (or - br) * alpha);
        int g = (int)(bg + (og - bg) * alpha);
        int b = (int)(bb + (ob - bb) * alpha);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
