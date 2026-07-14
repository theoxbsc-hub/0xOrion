/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.runelite.client.plugins.svc.webwalker.engine.RegionCollisionMap;

public final class CollisionMapLoader {
    public static final String BUNDLED_RESOURCE = "/webwalker/collision-map.zip";

    private CollisionMapLoader() {
    }

    public static RegionCollisionMap loadBundled() {
        RegionCollisionMap regionCollisionMap;
        block9: {
            InputStream inputStream = CollisionMapLoader.class.getResourceAsStream(BUNDLED_RESOURCE);
            if (inputStream == null) {
                return new RegionCollisionMap();
            }
            InputStream inputStream2 = inputStream;
            try {
                regionCollisionMap = CollisionMapLoader.loadShortestPathZip(inputStream2);
                if (inputStream2 == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    return new RegionCollisionMap();
                }
            }
            inputStream2.close();
        }
        return regionCollisionMap;
    }

    public static RegionCollisionMap loadShortestPathZip(InputStream inputStream) throws IOException {
        RegionCollisionMap regionCollisionMap = new RegionCollisionMap();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream);){
            ZipEntry zipEntry;
            byte[] byArray = new byte[8192];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                int n;
                int n2;
                int n3;
                String string = zipEntry.getName();
                int n4 = string.indexOf(95);
                if (n4 <= 0) continue;
                try {
                    n3 = Integer.parseInt(string.substring(0, n4));
                    n2 = Integer.parseInt(string.substring(n4 + 1));
                }
                catch (NumberFormatException numberFormatException) {
                    continue;
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((n = zipInputStream.read(byArray)) != -1) {
                    byteArrayOutputStream.write(byArray, 0, n);
                }
                BitSet bitSet = BitSet.valueOf(byteArrayOutputStream.toByteArray());
                int n5 = n3 << 8 | n2;
                regionCollisionMap.loadRegion(n5, bitSet.toLongArray());
            }
        }
        return regionCollisionMap;
    }
}

