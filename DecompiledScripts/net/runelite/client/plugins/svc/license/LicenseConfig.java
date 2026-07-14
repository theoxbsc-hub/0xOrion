/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

final class LicenseConfig {
    static final String SERVER_URL = System.getProperty("orion.license.url", "https://orion-license-production.up.railway.app");
    static final String PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtsg/kcPcTFL4QtvWazM+cvJowcbrj6NO9eJRjjhqmQ7Zl4hqRXTtBBKFM6PBWoFuwTU64T3Jtg2I4/5mV5iRWP7EDQiXnEcJDcShhvhI0mcEtJpFbOI3A0Nbb2Pv4DfxkyC5ca7G8V9+kfR1qvL7Y2xWwcxK4NMH8lxin6rnLNhc2EACfRLB+L4EKmtGYfYQjWqsqOgOVPNZI1nfFMhD13haANduawDgzUCfwtXYrPh4vRcoArN6rxBID0qCj8YJ5LJTD8G20wiuzkGrkbwqOsdJriUmBFFi/XgWQkYOYRJxRux9VPgzqT+a4pWA66OwbCJYa64WM6DRiREnVhwX6QIDAQAB";
    static final int HTTP_TIMEOUT_MS = 8000;
    static final String PRODUCT_NAME = "Orion";

    private LicenseConfig() {
    }
}

