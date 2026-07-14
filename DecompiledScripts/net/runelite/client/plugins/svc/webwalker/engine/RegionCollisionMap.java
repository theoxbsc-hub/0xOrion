/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.runelite.client.plugins.svc.webwalker.engine.CollisionMap;

public final class RegionCollisionMap
implements CollisionMap {
    private static final int MAGIC = 1331118897;
    private static final int LONGS_PER_REGION = 512;
    private final Map<Integer, long[]> regions = new HashMap<Integer, long[]>();

    public static int regionId(int n, int n2) {
        return n >> 6 << 8 | n2 >> 6;
    }

    private static int bitIndex(int n, int n2, int n3, int n4) {
        int n5 = n & 0x3F;
        int n6 = n2 & 0x3F;
        return ((n3 * 64 + n6) * 64 + n5) * 2 + n4;
    }

    private boolean get(int n, int n2, int n3, int n4) {
        long[] lArray = this.regions.get(RegionCollisionMap.regionId(n, n2));
        if (lArray == null) {
            return true;
        }
        int n5 = RegionCollisionMap.bitIndex(n, n2, n3, n4);
        return (lArray[n5 >> 6] & 1L << (n5 & 0x3F)) != 0L;
    }

    public void set(int n2, int n3, int n4, int n5, boolean bl) {
        long[] lArray = this.regions.computeIfAbsent(RegionCollisionMap.regionId(n2, n3), n -> new long[512]);
        int n6 = RegionCollisionMap.bitIndex(n2, n3, n4, n5);
        long l = 1L << (n6 & 0x3F);
        if (bl) {
            int n7 = n6 >> 6;
            lArray[n7] = lArray[n7] | l;
        } else {
            int n8 = n6 >> 6;
            lArray[n8] = lArray[n8] & (l ^ 0xFFFFFFFFFFFFFFFFL);
        }
    }

    public void createRegion(int n2, int n3) {
        this.regions.computeIfAbsent(RegionCollisionMap.regionId(n2, n3), n -> new long[512]);
    }

    public void loadRegion(int n, long[] lArray) {
        long[] lArray2 = new long[512];
        int n2 = Math.min(lArray.length, 512);
        System.arraycopy(lArray, 0, lArray2, 0, n2);
        this.regions.put(n, lArray2);
    }

    public int regionCount() {
        return this.regions.size();
    }

    @Override
    public boolean n(int n, int n2, int n3) {
        return this.get(n, n2, n3, 0);
    }

    @Override
    public boolean e(int n, int n2, int n3) {
        return this.get(n, n2, n3, 1);
    }

    @Override
    public boolean hasData(int n, int n2, int n3) {
        return this.regions.containsKey(RegionCollisionMap.regionId(n, n2));
    }

    public byte[] save() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(byteArrayOutputStream));){
            dataOutputStream.writeInt(1331118897);
            dataOutputStream.writeInt(this.regions.size());
            for (Map.Entry<Integer, long[]> entry : this.regions.entrySet()) {
                dataOutputStream.writeInt(entry.getKey());
                long[] lArray = entry.getValue();
                for (int i = 0; i < 512; ++i) {
                    dataOutputStream.writeLong(lArray[i]);
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static RegionCollisionMap load(InputStream inputStream) throws IOException {
        RegionCollisionMap regionCollisionMap = new RegionCollisionMap();
        try (DataInputStream dataInputStream = new DataInputStream(new GZIPInputStream(inputStream));){
            int n = dataInputStream.readInt();
            if (n != 1331118897) {
                throw new IOException("Bad collision map magic: 0x" + Integer.toHexString(n));
            }
            int n2 = dataInputStream.readInt();
            for (int i = 0; i < n2; ++i) {
                int n3 = dataInputStream.readInt();
                long[] lArray = new long[512];
                for (int j = 0; j < 512; ++j) {
                    lArray[j] = dataInputStream.readLong();
                }
                regionCollisionMap.regions.put(n3, lArray);
            }
        }
        return regionCollisionMap;
    }
}

