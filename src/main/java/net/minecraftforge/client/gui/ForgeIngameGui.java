/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.client.gui;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.*;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.food.FoodData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

@SuppressWarnings("deprecation")
public class ForgeIngameGui extends Gui
{
    //private static final ResourceLocation VIGNETTE     = new ResourceLocation("textures/misc/vignette.png");
    //private static final ResourceLocation WIDGITS      = new ResourceLocation("textures/gui/widgets.png");
    //private static final ResourceLocation PUMPKIN_BLUR = new ResourceLocation("textures/misc/pumpkinblur.png");

    private static final int WHITE = 0xFFFFFF;

    //Flags to toggle the rendering of certain aspects of the HUD, valid conditions
    //must be met for them to render normally. If those conditions are met, but this flag
    //is false, they will not be rendered.
    public static boolean renderVignette = true;
    public static boolean renderHelmet = true;
    public static boolean renderPortal = true;
    public static boolean renderSpectatorTooltip = true;
    public static boolean renderHotbar = true;
    public static boolean renderCrosshairs = true;
    public static boolean renderBossHealth = true;
    public static boolean renderHealth = true;
    public static boolean renderArmor = true;
    public static boolean renderFood = true;
    public static boolean renderHealthMount = true;
    public static boolean renderAir = true;
    public static boolean renderExperiance = true;
    public static boolean renderJumpBar = true;
    public static boolean renderObjective = true;

    public static int left_height = 39;
    public static int right_height = 39;
    /*
     * If the Euclidian distance to the moused-over block in meters is less than this value, the "Looking at" text will appear on the debug overlay.
     */
    public static double rayTraceDistance = 20.0D;

    private Font fontrenderer = null;
    private RenderGameOverlayEvent eventParent;
    //private static final String MC_VERSION = MinecraftForge.MC_VERSION;
    private GuiOverlayDebugForge debugOverlay;

    public ForgeIngameGui(Minecraft mc)
    {
        super(mc);
        debugOverlay = new GuiOverlayDebugForge(mc);
    }

    @Override
    public void render(PoseStack mStack, float partialTicks)
    {
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        eventParent = new RenderGameOverlayEvent(mStack, partialTicks, this.minecraft.getWindow());
        renderHealthMount = minecraft.player.getVehicle() instanceof LivingEntity;
        renderFood = !renderHealthMount;
        renderJumpBar = minecraft.player.isRidingJumpable();

        right_height = 39;
        left_height = 39;

        if (pre(ALL, mStack)) return;

        fontrenderer = minecraft.font;
        //mc.entityRenderer.setupOverlayRendering();
        RenderSystem.enableBlend();
        if (renderVignette && Minecraft.useFancyGraphics())
        {
            renderVignette(minecraft.getCameraEntity());
        }
        else
        {
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }

        if (renderHelmet) renderHelmet(partialTicks, mStack);

        if (renderPortal && !minecraft.player.hasEffect(MobEffects.CONFUSION))
        {
            renderPortalOverlay(partialTicks);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
        {
            if (renderSpectatorTooltip) spectatorGui.renderHotbar(mStack, partialTicks);
        }
        else if (!this.minecraft.options.hideGui)
        {
            if (renderHotbar) renderHotbar(partialTicks, mStack);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            setBlitOffset(-90);
            random.setSeed((long)(tickCount * 312871));

            if (renderCrosshairs) renderCrosshair(mStack);
            if (renderBossHealth) renderBossHealth(mStack);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.minecraft.gameMode.canHurtPlayer() && this.minecraft.getCameraEntity() instanceof Player)
            {
                if (renderHealth) renderHealth(this.screenWidth, this.screenHeight, mStack);
                if (renderArmor)  renderArmor(mStack, this.screenWidth, this.screenHeight);
                if (renderFood)   renderFood(this.screenWidth, this.screenHeight, mStack);
                if (renderHealthMount) renderHealthMount(this.screenWidth, this.screenHeight, mStack);
                if (renderAir)    renderAir(this.screenWidth, this.screenHeight, mStack);
            }

            if (renderJumpBar)
            {
                renderJumpMeter(mStack, this.screenWidth / 2 - 91);
            }
            else if (renderExperiance)
            {
                renderExperience(this.screenWidth / 2 - 91, mStack);
            }
            if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(mStack);
             } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(mStack);
             }
        }

        renderSleepFade(this.screenWidth, this.screenHeight, mStack);

        renderHUDText(this.screenWidth, this.screenHeight, mStack);
        renderFPSGraph(mStack);
        renderEffects(mStack);
        if (!minecraft.options.hideGui) {
            renderRecordOverlay(this.screenWidth, this.screenHeight, partialTicks, mStack);
            renderSubtitles(mStack);
            renderTitle(this.screenWidth, this.screenHeight, partialTicks, mStack);
        }


        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = null;
        PlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(minecraft.player.getScoreboardName());
        if (scoreplayerteam != null)
        {
            int slot = scoreplayerteam.getColor().getId();
            if (slot >= 0) objective = scoreboard.getDisplayObjective(3 + slot);
        }
        Objective scoreobjective1 = objective != null ? objective : scoreboard.getDisplayObjective(1);
        if (renderObjective && scoreobjective1 != null)
        {
            this.displayScoreboardSidebar(mStack, scoreobjective1);
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.disableAlphaTest();

        renderChat(this.screenWidth, this.screenHeight, mStack);

        renderPlayerList(this.screenWidth, this.screenHeight, mStack);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        post(ALL, mStack);
    }

    @Override
    protected void renderCrosshair(PoseStack mStack)
    {
        if (pre(CROSSHAIRS, mStack)) return;
        bind(GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        super.renderCrosshair(mStack);
        post(CROSSHAIRS, mStack);
    }

    @Override
    protected void renderEffects(PoseStack mStack)
    {
        if (pre(POTION_ICONS, mStack)) return;
        super.renderEffects(mStack);
        post(POTION_ICONS, mStack);
    }

    protected void renderSubtitles(PoseStack mStack)
    {
        if (pre(SUBTITLES, mStack)) return;
        this.subtitleOverlay.render(mStack);
        post(SUBTITLES, mStack);
    }

    protected void renderBossHealth(PoseStack mStack)
    {
        if (pre(BOSSHEALTH, mStack)) return;
        bind(GuiComponent.GUI_ICONS_LOCATION);
        RenderSystem.defaultBlendFunc();
        minecraft.getProfiler().push("bossHealth");
        RenderSystem.enableBlend();
        this.bossOverlay.render(mStack);
        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(BOSSHEALTH, mStack);
    }

    @Override
    protected void renderVignette(Entity entity)
    {
        PoseStack mStack = new PoseStack();
        if (pre(VIGNETTE, mStack))
        {
            // Need to put this here, since Vanilla assumes this state after the vignette was rendered.
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            return;
        }
        super.renderVignette(entity);
        post(VIGNETTE, mStack);
    }


    private void renderHelmet(float partialTicks, PoseStack mStack)
    {
        if (pre(HELMET, mStack)) return;

        ItemStack itemstack = this.minecraft.player.inventory.getArmor(3);

        if (this.minecraft.options.getCameraType().isFirstPerson() && !itemstack.isEmpty())
        {
            Item item = itemstack.getItem();
            if (item == Blocks.CARVED_PUMPKIN.asItem())
            {
                renderPumpkin();
            }
            else
            {
                item.renderHelmetOverlay(itemstack, minecraft.player, this.screenWidth, this.screenHeight, partialTicks);
            }
        }

        post(HELMET, mStack);
    }

    protected void renderArmor(PoseStack mStack, int width, int height)
    {
        if (pre(ARMOR, mStack)) return;
        minecraft.getProfiler().push("armor");

        RenderSystem.enableBlend();
        int left = width / 2 - 91;
        int top = height - left_height;

        int level = minecraft.player.getArmorValue();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                blit(mStack, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                blit(mStack, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                blit(mStack, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        left_height += 10;

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(ARMOR, mStack);
    }

    @Override
    protected void renderPortalOverlay(float partialTicks)
    {
        PoseStack mStack = new PoseStack();
        if (pre(PORTAL, mStack)) return;

        float f1 = minecraft.player.oPortalTime + (minecraft.player.portalTime - minecraft.player.oPortalTime) * partialTicks;

        if (f1 > 0.0F)
        {
            super.renderPortalOverlay(f1);
        }

        post(PORTAL, mStack);
    }

    @Override
    protected void renderHotbar(float partialTicks, PoseStack mStack)
    {
        if (pre(HOTBAR, mStack)) return;

        if (minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
        {
            this.spectatorGui.renderHotbar(mStack, partialTicks);
        }
        else
        {
            super.renderHotbar(partialTicks, mStack);
        }

        post(HOTBAR, mStack);
    }

    protected void renderAir(int width, int height, PoseStack mStack)
    {
        if (pre(AIR, mStack)) return;
        minecraft.getProfiler().push("air");
        Player player = (Player)this.minecraft.getCameraEntity();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;

        int air = player.getAirSupply();
        if (player.isEyeInFluid(FluidTags.WATER) || air < 300)
        {
            int full = Mth.ceil((double)(air - 2) * 10.0D / 300.0D);
            int partial = Mth.ceil((double)air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i)
            {
                blit(mStack, left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            right_height += 10;
        }

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(AIR, mStack);
    }

    public void renderHealth(int width, int height, PoseStack mStack)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(HEALTH, mStack)) return;
        minecraft.getProfiler().push("health");
        RenderSystem.enableBlend();

        Player player = (Player)this.minecraft.getCameraEntity();
        int health = Mth.ceil(player.getHealth());
        boolean highlight = healthBlinkTime > (long)tickCount && (healthBlinkTime - (long)tickCount) / 3L %2L == 1L;

        if (health < this.lastHealth && player.invulnerableTime > 0)
        {
            this.lastHealthTime = Util.getMillis();
            this.healthBlinkTime = (long)(this.tickCount + 20);
        }
        else if (health > this.lastHealth && player.invulnerableTime > 0)
        {
            this.lastHealthTime = Util.getMillis();
            this.healthBlinkTime = (long)(this.tickCount + 10);
        }

        if (Util.getMillis() - this.lastHealthTime > 1000L)
        {
            this.lastHealth = health;
            this.displayHealth = health;
            this.lastHealthTime = Util.getMillis();
        }

        this.lastHealth = health;
        int healthLast = this.displayHealth;

        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = Mth.ceil(player.getAbsorptionAmount());

        int healthRows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.random.setSeed((long)(tickCount * 312871));

        int left = width / 2 - 91;
        int top = height - left_height;
        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;

        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION))
        {
            regen = tickCount % 25;
        }

        final int TOP =  9 * (minecraft.level.getLevelData().isHardcore() ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.hasEffect(MobEffects.POISON))      MARGIN += 36;
        else if (player.hasEffect(MobEffects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = Mth.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
        {
            //int b0 = (highlight ? 1 : 0);
            int row = Mth.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += random.nextInt(2);
            if (i == regen) y -= 2;

            blit(mStack, x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (i * 2 + 1 < healthLast)
                    blit(mStack, x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    blit(mStack, x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    blit(mStack, x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    blit(mStack, x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (i * 2 + 1 < health)
                    blit(mStack, x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    blit(mStack, x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(HEALTH, mStack);
    }

    public void renderFood(int width, int height, PoseStack mStack)
    {
        if (pre(FOOD, mStack)) return;
        minecraft.getProfiler().push("food");

        Player player = (Player)this.minecraft.getCameraEntity();
        RenderSystem.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;
        right_height += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodData stats = minecraft.player.getFoodData();
        int level = stats.getFoodLevel();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte background = 0;

            if (minecraft.player.hasEffect(MobEffects.HUNGER))
            {
                icon += 36;
                background = 13;
            }
            if (unused) background = 1; //Probably should be a += 1 but vanilla never uses this

            if (player.getFoodData().getSaturationLevel() <= 0.0F && tickCount % (level * 3 + 1) == 0)
            {
                y = top + (random.nextInt(3) - 1);
            }

            blit(mStack, x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                blit(mStack, x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                blit(mStack, x, y, icon + 45, 27, 9, 9);
        }
        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        post(FOOD, mStack);
    }

    protected void renderSleepFade(int width, int height, PoseStack mStack)
    {
        if (minecraft.player.getSleepTimer() > 0)
        {
            minecraft.getProfiler().push("sleep");
            RenderSystem.disableDepthTest();
            RenderSystem.disableAlphaTest();
            int sleepTime = minecraft.player.getSleepTimer();
            float opacity = (float)sleepTime / 100.0F;

            if (opacity > 1.0F)
            {
                opacity = 1.0F - (float)(sleepTime - 100) / 10.0F;
            }

            int color = (int)(220.0F * opacity) << 24 | 1052704;
            fill(mStack, 0, 0, width, height, color);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            minecraft.getProfiler().pop();
        }
    }

    protected void renderExperience(int x, PoseStack mStack)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(EXPERIENCE, mStack)) return;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        if (minecraft.gameMode.hasExperience())
        {
            super.renderExperienceBar(mStack, x);
        }
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(EXPERIENCE, mStack);
    }

    @Override
    public void renderJumpMeter(PoseStack mStack, int x)
    {
        bind(GUI_ICONS_LOCATION);
        if (pre(JUMPBAR, mStack)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        super.renderJumpMeter(mStack, x);

        RenderSystem.enableBlend();
        minecraft.getProfiler().pop();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(JUMPBAR, mStack);
    }

    protected void renderHUDText(int width, int height, PoseStack mStack)
    {
        minecraft.getProfiler().push("forgeHudText");
        RenderSystem.defaultBlendFunc();
        ArrayList<String> listL = new ArrayList<String>();
        ArrayList<String> listR = new ArrayList<String>();

        if (minecraft.isDemo())
        {
            long time = minecraft.level.getGameTime();
            if (time >= 120500L)
            {
                listR.add(I18n.get("demo.demoExpired"));
            }
            else
            {
                listR.add(I18n.get("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - time))));
            }
        }

        if (this.minecraft.options.renderDebug && !pre(DEBUG, mStack))
        {
            debugOverlay.update();
            listL.addAll(debugOverlay.getLeft());
            listR.addAll(debugOverlay.getRight());
            post(DEBUG, mStack);
        }

        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(mStack, eventParent, listL, listR);
        if (!MinecraftForge.EVENT_BUS.post(event))
        {
            int top = 2;
            for (String msg : listL)
            {
                if (msg == null) continue;
                fill(mStack, 1, top - 1, 2 + fontrenderer.width(msg) + 1, top + fontrenderer.lineHeight - 1, -1873784752);
                fontrenderer.draw(mStack, msg, 2, top, 14737632);
                top += fontrenderer.lineHeight;
            }

            top = 2;
            for (String msg : listR)
            {
                if (msg == null) continue;
                int w = fontrenderer.width(msg);
                int left = width - 2 - w;
                fill(mStack, left - 1, top - 1, left + w + 1, top + fontrenderer.lineHeight - 1, -1873784752);
                fontrenderer.draw(mStack, msg, left, top, 14737632);
                top += fontrenderer.lineHeight;
            }
        }

        minecraft.getProfiler().pop();
        post(TEXT, mStack);
    }

    protected void renderFPSGraph(PoseStack mStack)
    {
        if (this.minecraft.options.renderDebug && this.minecraft.options.renderFpsChart && !pre(FPS_GRAPH, mStack))
        {
            this.debugOverlay.render(mStack);
            post(FPS_GRAPH, mStack);
        }
    }

    protected void renderRecordOverlay(int width, int height, float partialTicks, PoseStack mStack)
    {
        if (overlayMessageTime > 0)
        {
            minecraft.getProfiler().push("overlayMessage");
            float hue = (float)overlayMessageTime - partialTicks;
            int opacity = (int)(hue * 255.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 8)
            {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(width / 2), (float)(height - 68), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int color = (animateOverlayMessageColor ? Mth.hsvToRgb(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                drawBackdrop(mStack, fontrenderer, -4, fontrenderer.width(overlayMessageString), 16777215 | (opacity << 24));
                fontrenderer.draw(mStack, overlayMessageString.getVisualOrderText(), -fontrenderer.width(overlayMessageString) / 2, -4, color | (opacity << 24));
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }

            minecraft.getProfiler().pop();
        }
    }

    protected void renderTitle(int width, int height, float partialTicks, PoseStack mStack)
    {
        if (title != null && titleTime > 0)
        {
            minecraft.getProfiler().push("titleAndSubtitle");
            float age = (float)this.titleTime - partialTicks;
            int opacity = 255;

            if (titleTime > titleFadeOutTime + titleStayTime)
            {
                float f3 = (float)(titleFadeInTime + titleStayTime + titleFadeOutTime) - age;
                opacity = (int)(f3 * 255.0F / (float)titleFadeInTime);
            }
            if (titleTime <= titleFadeOutTime) opacity = (int)(age * 255.0F / (float)this.titleFadeOutTime);

            opacity = Mth.clamp(opacity, 0, 255);

            if (opacity > 8)
            {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(width / 2), (float)(height / 2), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.pushMatrix();
                RenderSystem.scalef(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & -16777216;
                this.getFont().drawShadow(mStack, this.title.getVisualOrderText(), (float)(-this.getFont().width(this.title) / 2), -10.0F, 16777215 | l);
                RenderSystem.popMatrix();
                if (this.subtitle != null)
                {
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(2.0F, 2.0F, 2.0F);
                    this.getFont().drawShadow(mStack, this.subtitle.getVisualOrderText(), (float)(-this.getFont().width(this.subtitle) / 2), 5.0F, 16777215 | l);
                    RenderSystem.popMatrix();
                }
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }

            this.minecraft.getProfiler().pop();
        }
    }

    protected void renderChat(int width, int height, PoseStack mStack)
    {
        minecraft.getProfiler().push("chat");

        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(mStack, eventParent, 0, height - 48);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) event.getPosX(), (float) event.getPosY(), 0.0F);
        chat.render(mStack, tickCount);
        RenderSystem.popMatrix();

        post(CHAT, mStack);

        minecraft.getProfiler().pop();
    }

    protected void renderPlayerList(int width, int height, PoseStack mStack)
    {
        Objective scoreobjective = this.minecraft.level.getScoreboard().getDisplayObjective(0);
        ClientPacketListener handler = minecraft.player.connection;

        if (minecraft.options.keyPlayerList.isDown() && (!minecraft.isLocalServer() || handler.getOnlinePlayers().size() > 1 || scoreobjective != null))
        {
            this.tabList.setVisible(true);
            if (pre(PLAYER_LIST, mStack)) return;
            this.tabList.render(mStack, width, this.minecraft.level.getScoreboard(), scoreobjective);
            post(PLAYER_LIST, mStack);
        }
        else
        {
            this.tabList.setVisible(false);
        }
    }

    protected void renderHealthMount(int width, int height, PoseStack mStack)
    {
        Player player = (Player)minecraft.getCameraEntity();
        Entity tmp = player.getVehicle();
        if (!(tmp instanceof LivingEntity)) return;

        bind(GUI_ICONS_LOCATION);

        if (pre(HEALTHMOUNT, mStack)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        minecraft.getProfiler().popPush("mountHealth");
        RenderSystem.enableBlend();
        LivingEntity mount = (LivingEntity)tmp;
        int health = (int)Math.ceil((double)mount.getHealth());
        float healthMax = mount.getMaxHealth();
        int hearts = (int)(healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20)
        {
            int top = height - right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i)
            {
                int x = left_align - i * 8 - 9;
                blit(mStack, x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    blit(mStack, x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    blit(mStack, x, top, HALF, 9, 9, 9);
            }

            right_height += 10;
        }
        RenderSystem.disableBlend();
        post(HEALTHMOUNT, mStack);
    }

    //Helper macros
    private boolean pre(ElementType type, PoseStack mStack)
    {
        return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(mStack, eventParent, type));
    }
    private void post(ElementType type, PoseStack mStack)
    {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(mStack, eventParent, type));
    }
    private void bind(ResourceLocation res)
    {
        minecraft.getTextureManager().bind(res);
    }

    private class GuiOverlayDebugForge extends DebugScreenOverlay
    {
        private Minecraft mc;
        private GuiOverlayDebugForge(Minecraft mc)
        {
            super(mc);
            this.mc = mc;
        }
        public void update()
        {
            Entity entity = this.mc.getCameraEntity();
            this.block = entity.pick(rayTraceDistance, 0.0F, false);
            this.liquid = entity.pick(rayTraceDistance, 0.0F, true);
        }
        @Override protected void drawGameInformation(PoseStack mStack){}
        @Override protected void drawSystemInformation(PoseStack mStack){}
        private List<String> getLeft()
        {
            List<String> ret = this.getGameInformation();
            ret.add("");
            ret.add("Debug: Pie [shift]: " + (this.mc.options.renderDebugCharts ? "visible" : "hidden") + " FPS [alt]: " + (this.mc.options.renderFpsChart ? "visible" : "hidden"));
            ret.add("For help: press F3 + Q");
            return ret;
        }
        private List<String> getRight(){ return this.getSystemInformation(); }
    }
}
