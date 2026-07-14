/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.ui.DrawManager
 */
package net.runelite.client.plugins.svc.chatresponder;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import net.runelite.client.ui.DrawManager;

final class ChatScreenshot {
    private static final int MAX_WIDTH = 1280;

    private ChatScreenshot() {
    }

    static void requestShot(DrawManager drawManager, Consumer<byte[]> consumer) {
        drawManager.requestNextFrameListener(image -> {
            byte[] byArray = null;
            try {
                BufferedImage bufferedImage = ChatScreenshot.copy(image);
                if (bufferedImage != null) {
                    byArray = ChatScreenshot.encodePng(bufferedImage);
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

