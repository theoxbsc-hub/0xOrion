/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.core;

import java.lang.management.ManagementFactory;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccountProfile {
    private static final Logger log = LoggerFactory.getLogger(AccountProfile.class);
    private final Client client;
    private final Random noise = new Random(System.nanoTime() ^ (long)System.identityHashCode(this) * -7046029254386353131L);
    private volatile boolean resolved;
    private long seed;
    private Random rng;
    private double tempo;
    private int reactionMin;
    private int reactionMax;
    private String seedLabel = "";
    private double bowScale;
    private double speedScale;
    private double overshootBias;
    private int aimBiasX;
    private int aimBiasY;
    private double settleBase;
    private double settleSigma;
    private double shortEdge;
    private long startedAtMs;

    public AccountProfile(Client client) {
        this.client = client;
    }

    private synchronized void ensure() {
        if (this.resolved) {
            return;
        }
        this.seedLabel = this.computeSeedLabel();
        this.seed = AccountProfile.fnv1a64(this.seedLabel);
        this.rng = new Random(this.seed);
        this.tempo = 0.9 + this.rng.nextDouble() * 0.3;
        this.reactionMin = (int)Math.round(140.0 * this.tempo);
        this.reactionMax = (int)Math.round(380.0 * this.tempo);
        this.bowScale = 0.7 + this.rng.nextDouble() * 0.7;
        this.speedScale = 0.8 + this.rng.nextDouble() * 0.45;
        this.overshootBias = this.rng.nextDouble();
        this.aimBiasX = this.rng.nextInt(5) - 2;
        this.aimBiasY = this.rng.nextInt(5) - 2;
        this.settleBase = 55.0 + this.rng.nextDouble() * 40.0;
        this.settleSigma = 0.3 + this.rng.nextDouble() * 0.2;
        this.shortEdge = 0.02 + this.rng.nextDouble() * 0.04;
        this.startedAtMs = System.currentTimeMillis();
        this.resolved = true;
        log.info("[AIO Skiller] AccountProfile seed='{}' ({}), tempo={} bow={} spd={} aim=({},{})", new Object[]{this.seedLabel, this.seed, String.format("%.3f", this.tempo), String.format("%.2f", this.bowScale), String.format("%.2f", this.speedScale), this.aimBiasX, this.aimBiasY});
    }

    public Random rng() {
        this.ensure();
        return this.rng;
    }

    public double tempo() {
        this.ensure();
        return this.tempo;
    }

    public double bowScale() {
        this.ensure();
        return this.bowScale;
    }

    public double speedScale() {
        this.ensure();
        return this.speedScale;
    }

    public double overshootBias() {
        this.ensure();
        return this.overshootBias;
    }

    public int aimBiasX() {
        this.ensure();
        return this.aimBiasX;
    }

    public int aimBiasY() {
        this.ensure();
        return this.aimBiasY;
    }

    public double settleBase() {
        this.ensure();
        return this.settleBase;
    }

    public double settleSigma() {
        this.ensure();
        return this.settleSigma;
    }

    public String seedLabel() {
        this.ensure();
        return this.seedLabel;
    }

    public long seed() {
        this.ensure();
        return this.seed;
    }

    public double gaussian() {
        this.ensure();
        return this.rng.nextGaussian();
    }

    public int rngInt(int n, int n2) {
        this.ensure();
        if (n2 <= n) {
            return n;
        }
        return n + this.rng.nextInt(n2 - n + 1);
    }

    public int reactionDelay() {
        this.ensure();
        int n = this.reactionMax - this.reactionMin;
        double d = (this.noise.nextDouble() + this.noise.nextDouble()) / 2.0;
        int n2 = this.reactionMin + (int)Math.round(d * (double)n);
        if (this.noise.nextDouble() < 0.09) {
            n2 += (int)Math.round(-Math.log(1.0 - this.noise.nextDouble()) * (double)n * 0.85);
        }
        return Math.max(60, n2);
    }

    public int reactiveDelay() {
        this.ensure();
        int n = 200 + this.noise.nextInt(90);
        return Math.max(n, this.reactionDelay());
    }

    public long jitterDelay(long l) {
        double d;
        this.ensure();
        if (l <= 0L) {
            return l;
        }
        if (l < 1000L) {
            double d2 = 0.08 * this.tempo * this.fatigue();
            double d3 = 1.0 + this.shortEdge + d2 * Math.exp(this.noise.nextGaussian() * 0.6);
            d3 = Math.min(d3, 1.85);
            long l2 = Math.round((double)l * d3);
            if (this.noise.nextDouble() < 0.012) {
                l2 += 120L + Math.round(-Math.log(1.0 - this.noise.nextDouble()) * 350.0);
            }
            return Math.max(l, l2);
        }
        int n = 0;
        while (((d = this.tempo * this.fatigue() * Math.exp(this.noise.nextGaussian() * 0.22)) < 0.75 || d > 1.95) && ++n < 6) {
        }
        d = Math.max(0.62, Math.min(2.3, d));
        long l3 = Math.round((double)l * d);
        if (this.noise.nextDouble() < 0.02) {
            double d4 = 2.0 + -Math.log(1.0 - this.noise.nextDouble()) * 3.0;
            l3 = Math.round((double)l3 * Math.min(d4, 12.0));
            l3 = Math.min(l3, 32000L);
        }
        return Math.max(1L, l3);
    }

    private double fatigue() {
        long l = System.currentTimeMillis() - this.startedAtMs;
        if (l < 0L) {
            l = 0L;
        }
        double d = (double)l / 3600000.0;
        double d2 = 0.18 * (1.0 - Math.exp(-d / 2.5));
        double d3 = 0.03 * Math.sin(d * 1.7);
        return 1.0 + d2 + d3;
    }

    private String computeSeedLabel() {
        String string = AccountProfile.firstNonBlank(System.getProperty("orion.proxy.label"), System.getProperty("orion.proxy"), System.getProperty("incognito.machineId"), this.localName());
        if (string != null && !string.isEmpty()) {
            return string;
        }
        return "aioskiller-" + AccountProfile.jvmEntropyToken();
    }

    private static String jvmEntropyToken() {
        try {
            String string = ManagementFactory.getRuntimeMXBean().getName();
            if (string != null && !string.isEmpty()) {
                return string;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return Long.toHexString(System.nanoTime() ^ (long)System.identityHashCode(new Object()) * -7046029254386353131L);
    }

    private String localName() {
        try {
            String string;
            Player player = this.client.getLocalPlayer();
            if (player != null && player.getName() != null && !(string = player.getName().replaceAll("<[^>]*>", "").trim()).isEmpty()) {
                return string;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static String firstNonBlank(String ... stringArray) {
        if (stringArray == null) {
            return null;
        }
        for (String string : stringArray) {
            if (string == null || string.trim().isEmpty()) continue;
            return string.trim();
        }
        return null;
    }

    private static long fnv1a64(String string) {
        long l = -3750763034362895579L;
        if (string != null) {
            for (int i = 0; i < string.length(); ++i) {
                l ^= (long)string.charAt(i);
                l *= 1099511628211L;
            }
        }
        return l;
    }
}

