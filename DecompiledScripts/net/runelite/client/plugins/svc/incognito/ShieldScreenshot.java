/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.client.ui.DrawManager
 */
package net.runelite.client.plugins.svc.incognito;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.DrawManager;

final class ShieldScreenshot {
    private static final int MAX_WIDTH = 1280;
    private static final Color MARKER = new Color(255, 40, 40);

    private ShieldScreenshot() {
    }

    static void requestShot(Client client, DrawManager drawManager, WorldPoint worldPoint, String string, Consumer<byte[]> consumer) {
        drawManager.requestNextFrameListener(image -> {
            byte[] byArray = null;
            try {
                BufferedImage bufferedImage = ShieldScreenshot.copy(image);
                if (bufferedImage != null) {
                    ShieldScreenshot.drawMarker(client, bufferedImage, worldPoint, string);
                    byArray = ShieldScreenshot.encodePng(bufferedImage);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            consumer.accept(byArray);
        });
    }

    private static BufferedImage copy(Image image) {
        if (image == null) {
            return null;
        }
        int n = image.getWidth(null);
        int n2 = image.getHeight(null);
        if (n <= 0 || n2 <= 0) {
            return null;
        }
        BufferedImage bufferedImage = new BufferedImage(n, n2, 1);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    private static void drawMarker(Client client, BufferedImage bufferedImage, WorldPoint worldPoint, String string) {
        if (worldPoint == null) {
            return;
        }
        try {
            LocalPoint localPoint = LocalPoint.fromWorld((Client)client, (WorldPoint)worldPoint);
            if (localPoint == null) {
                return;
            }
            Polygon polygon = Perspective.getCanvasTilePoly((Client)client, (LocalPoint)localPoint);
            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (polygon != null) {
                graphics2D.setColor(MARKER);
                graphics2D.setStroke(new BasicStroke(3.0f));
                graphics2D.drawPolygon(polygon);
                Rectangle rectangle = polygon.getBounds();
                ShieldScreenshot.drawLabel(graphics2D, string, rectangle.x, rectangle.y - 6);
            } else {
                ShieldScreenshot.drawLabel(graphics2D, string + " (off-screen)", 12, 24);
            }
            graphics2D.dispose();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void drawLabel(Graphics2D graphics2D, String string, int n, int n2) {
        if (string == null || string.isEmpty()) {
            return;
        }
        graphics2D.setFont(new Font("SansSerif", 1, 14));
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString(string, n + 1, n2 + 1);
        graphics2D.setColor(MARKER);
        graphics2D.drawString(string, n, n2);
    }

    private static byte[] encodePng(BufferedImage bufferedImage) throws Exception {
        BufferedImage bufferedImage2 = bufferedImage;
        if (bufferedImage.getWidth() > 1280) {
            int n = 1280;
            int n2 = Math.max(1, bufferedImage.getHeight() * 1280 / bufferedImage.getWidth());
            BufferedImage bufferedImage3 = new BufferedImage(n, n2, 1);
            Graphics2D graphics2D = bufferedImage3.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(bufferedImage, 0, 0, n, n2, null);
            graphics2D.dispose();
            bufferedImage2 = bufferedImage3;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage)bufferedImage2, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}

