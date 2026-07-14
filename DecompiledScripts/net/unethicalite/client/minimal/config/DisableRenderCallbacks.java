/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Model
 *  net.runelite.api.Renderable
 *  net.runelite.api.SceneTileModel
 *  net.runelite.api.SceneTilePaint
 *  net.runelite.api.Texture
 *  net.runelite.api.hooks.DrawCallbacks
 */
package net.unethicalite.client.minimal.config;

import net.runelite.api.Model;
import net.runelite.api.Renderable;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Texture;
import net.runelite.api.hooks.DrawCallbacks;

public class DisableRenderCallbacks
implements DrawCallbacks {
    public void draw(Renderable renderable, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) {
    }

    public void drawScenePaint(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, SceneTilePaint paint, int tileZ, int tileX, int tileY, int zoom, int centerX, int centerY) {
    }

    public void drawSceneModel(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, SceneTileModel model, int tileZ, int tileX, int tileY, int zoom, int centerX, int centerY) {
    }

    public void draw(int overlayColor) {
    }

    public boolean drawFace(Model model, int face) {
        return false;
    }

    public void drawScene(int cameraX, int cameraY, int cameraZ, int cameraPitch, int cameraYaw, int plane) {
    }

    public void postDrawScene() {
    }

    public void animate(Texture texture, int diff) {
    }
}

