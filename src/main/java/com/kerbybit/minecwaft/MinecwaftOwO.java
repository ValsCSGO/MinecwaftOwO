package com.kerbybit.minecwaft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.StringUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = MinecwaftOwO.MODID, version = MinecwaftOwO.VERSION)
public class MinecwaftOwO {
    static final String MODID = "MinecwaftOwO";
    static final String VERSION = "1.6";
    static boolean toggled = true;

    private static final String[] punctuationFaces = new String[]{"(*^w^)", "(*^.^*)", "(OuO)", "(OwO)", "(UwU)", "(>w<)", "(^w^)", "(^u^)", "(/ =w=)/"};
    private static final List<Matcher> punctuations = Arrays.asList(
            Pattern.compile("!( |$)").matcher(""),
            Pattern.compile("\\.( |$)").matcher(""),
            Pattern.compile(",( |$)").matcher("")
    );

    private static final HashMap<Matcher, String> matchers = new HashMap<Matcher, String>(){{
        put(Pattern.compile("(?<!\u00A7)[lr]").matcher(""), "w");
        put(Pattern.compile("[LR]").matcher(""), "W");
        put(Pattern.compile("(?<![§\\\\oO])n([aeiou])").matcher(""), "ny$1");
        put(Pattern.compile("N([aeiou])").matcher(""), "Ny$1");
        put(Pattern.compile("N([AEIOU])").matcher(""), "NY$1");
    }};

    private static Cache<String, String> cache = new Cache2kBuilder<String, String>() {}
            .name("MinecwaftOwO")
            .eternal(true)
            .entryCapacity(25000)
            .loader((string) -> {
                string = StringUtils.replace(string, "th","d");
                string = StringUtils.replace(string, "Th","D");
                string = StringUtils.replace(string, " is"," ish");
                string = StringUtils.replace(string, " Is"," Ish");
                string = StringUtils.replace(string, "ove", "uv");

                for (Map.Entry<Matcher, String> match : matchers.entrySet()) {
                    string = match.getKey().reset(string).replaceAll(match.getValue());
                }

                for (Matcher punctuation : punctuations) {
                    string = punctuation.reset(string).replaceAll(" " + punctuationFaces[string.length() % punctuationFaces.length] + " ");
                }
                return string;
            })
            .build();

    private double ticks = 0;
    private Face[] faces = new Face[] {
            new Face("OwO", "-w-", ">w<", "xwx"),
            new Face("owo", "-w-", ">w<", "xwx"),
            new Face("UwU", "-w-", ">m<", "xmx"),
            new Face("OuO", "^u^", ">n<", "vnv"),
            new Face("ono", "-n-", ">n<", "xnx"),
            new Face("uwu", "-u-", ">m<", "xmx")
    };
    private HashMap<EntityPlayer, Face> faceList = new HashMap<>();

    @Mod.EventHandler
    private void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandOwO());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tick(TickEvent event) {
        ticks+=0.0005;
        if (ticks > 25) ticks = 0;
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event) {
        faceList.clear();
    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent.Post event) {
        if (!toggled) {
            faceList.clear();
            return;
        }

        EntityPlayer entity = event.entityPlayer;
        if (entity.isInvisible()) return;

        if (!faceList.containsKey(entity)) {
            faceList.put(entity, faces[faceList.size() % faces.length]);
        }

        Face face = faceList.get(entity);
        String toRender = face.open;
        Color color = Color.WHITE;

        if (entity.getHealth() == 0) {
            toRender = face.dead;
            color = Color.RED;
        } else if (entity.hurtResistantTime > 0) {
            toRender = face.hurt;
            color = Color.RED;
        } else if (ticks % entity.getName().length() < 0.1) {
            toRender = face.closed;
        }

        FontRenderer fontrenderer = event.renderer.getFontRendererFromRenderManager();
        float f1 = 0.016666668F * 1.6F;
        GlStateManager.pushMatrix();

        Vec3 look = event.entity.getLook(event.partialRenderTick);
        look = new Vec3(look.xCoord / 3.5, look.yCoord / 3.5, look.zCoord / 3.5);
        look = look.addVector(event.x, event.y + event.entity.height - .05, event.z);
        if (entity.isSneaking()) look = look.addVector(0, -0.3, 0);


        GlStateManager.translate((float) look.xCoord, (float) look.yCoord, (float) look.zCoord);

        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * event.partialRenderTick;
        float yaw = MathHelper.wrapAngleTo180_float(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * event.partialRenderTick);

        if (yaw > 0) {
            GlStateManager.rotate(Math.abs(yaw - 180), 0.0F, 1.0F, 0.0F);
        } else {
            GlStateManager.rotate(-(yaw + 180), 0.0F, 1.0F, 0.0F);
        }

        GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);

        if (pitch < 0) {
            float percent = (-pitch) / 90;
            GlStateManager.translate(0, (0.3 * percent), (0.35 * percent));
        } else if (pitch < 65) {
            float percent = (pitch) / 90;
            GlStateManager.translate(0, (0.3 * percent), -(0.4 * percent));
        } else if (pitch < 85) {
            float percent = (pitch) / 90;
            GlStateManager.translate(0, (0.3 * percent), -(0.35 * percent));
        } else {
            float percent = (pitch) / 90;
            double offset = 0.3 * percent;
            GlStateManager.translate(0, offset, -offset);
        }

        if (pitch > 0 && entity.isSneaking()) {
            GlStateManager.translate(0, 0, -.1);
        }

        if (entity.inventory.armorItemInSlot(3) != null) {
            if (pitch < 0) {
                GlStateManager.translate(0, 0, -0.1);
            } else {
                GlStateManager.translate(0, 0, -0.05);
            }
        }

        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        renderString(fontrenderer, toRender, color);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void renderString(FontRenderer renderer, String face, Color color) {
        int x = -renderer.getStringWidth(face) / 2;

        render(renderer, x, face, color);
    }

    private void render(FontRenderer renderer, int x, String face, Color color) {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.color(255, 255, 255, .5F);

        renderer.drawStringWithShadow(face, x, 0, color.getRGB());
    }

    public static String makeOwO(String string) {
        if (!toggled) return string;
        return cache.get(string);
    }

    private class Face {
        String open;
        String closed;
        String hurt;
        String dead;

        private Face(String open, String closed, String hurt, String dead) {
            this.open = open;
            this.closed = closed;
            this.hurt = hurt;
            this.dead = dead;
        }
    }
}
