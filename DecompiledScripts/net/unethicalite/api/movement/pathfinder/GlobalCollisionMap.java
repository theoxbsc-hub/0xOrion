/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.Direction
 */
package net.unethicalite.api.movement.pathfinder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.runelite.api.coords.Direction;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.BitSet4D;
import net.unethicalite.api.movement.pathfinder.CollisionMap;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.client.Static;

public class GlobalCollisionMap
implements CollisionMap {
    public final BitSet4D[] regions = new BitSet4D[65536];

    public GlobalCollisionMap() {
    }

    public GlobalCollisionMap(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            int region = buffer.getShort() & 0xFFFF;
            this.regions[region] = new BitSet4D(buffer, 64, 64, 4, 2);
        }
    }

    public File writeToFile() {
        byte[] bytes = this.toBytes();
        File fileLoc = new File("collision-map");
        if (!fileLoc.isFile()) {
            try {
                fileLoc.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
             GZIPOutputStream gos = new GZIPOutputStream(bos);
             FileOutputStream fos = new FileOutputStream(fileLoc);){
            gos.write(bytes);
            gos.finish();
            fos.write(bos.toByteArray());
            gos.flush();
            fos.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return fileLoc;
    }

    public byte[] toBytes() {
        int regionCount = (int)Arrays.stream(this.regions).filter(Objects::nonNull).count();
        ByteBuffer buffer = ByteBuffer.allocate(regionCount * 4098);
        for (int i = 0; i < this.regions.length; ++i) {
            if (this.regions[i] == null) continue;
            buffer.putShort((short)i);
            this.regions[i].write(buffer);
        }
        return buffer.array();
    }

    public void set(int x, int y, int z, int w, boolean value) {
        BitSet4D region = this.regions[x / 64 * 256 + y / 64];
        if (region == null) {
            return;
        }
        region.set(x % 64, y % 64, z, w, value);
    }

    public BitSet4D getRegion(int x, int y) {
        int regionId = x / 64 * 256 + y / 64;
        return this.regions[regionId];
    }

    public void createRegion(int region) {
        this.regions[region] = new BitSet4D(64, 64, 4, 2);
        this.regions[region].setAll(true);
    }

    public boolean get(int x, int y, int z, int w) {
        BitSet4D region = this.getRegion(x, y);
        if (region == null) {
            return false;
        }
        int regionX = x % 64;
        int regionY = y % 64;
        return region.get(regionX, regionY, z, w);
    }

    public GlobalCollisionMap copy() {
        return new GlobalCollisionMap(this.toBytes());
    }

    public GlobalCollisionMap withLocalCollisions() {
        GlobalCollisionMap newMap = this.copy();
        Static.getRegionManager().getTileFlags().forEach(tileFlag -> {
            int region = tileFlag.getRegion();
            int x = tileFlag.getX();
            int y = tileFlag.getY();
            int z = tileFlag.getZ();
            int flag = tileFlag.getFlag();
            if (newMap.regions[region] == null) {
                newMap.createRegion(region);
            }
            if (Reachable.isObstacle(flag)) {
                newMap.set(x, y, z, 0, false);
                newMap.set(x, y, z, 1, false);
            } else {
                newMap.set(x, y, z, 0, true);
                newMap.set(x, y, z, 1, true);
                if (Reachable.isWalled(Direction.NORTH, flag)) {
                    newMap.set(x, y, z, 0, false);
                }
                if (Reachable.isWalled(Direction.EAST, flag)) {
                    newMap.set(x, y, z, 1, false);
                }
            }
        });
        return newMap;
    }

    public void overwrite(GlobalCollisionMap globalCollisionMap) {
        System.arraycopy(globalCollisionMap.regions, 0, this.regions, 0, this.regions.length);
    }

    @Override
    public boolean n(int x, int y, int z) {
        return this.get(x, y, z, 0);
    }

    @Override
    public boolean e(int x, int y, int z) {
        return this.get(x, y, z, 1);
    }

    public static GlobalCollisionMap fetchFromUrl(String url) throws IOException {
        try (InputStream is = Walker.class.getResourceAsStream("/regions");){
            if (is == null) {
                GlobalCollisionMap globalCollisionMap = new GlobalCollisionMap();
                return globalCollisionMap;
            }
            GlobalCollisionMap globalCollisionMap = new GlobalCollisionMap(new GZIPInputStream(new ByteArrayInputStream(is.readAllBytes())).readAllBytes());
            return globalCollisionMap;
        }
    }
}

