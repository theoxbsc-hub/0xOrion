/*
 * Decompiled with CFR 0.152.
 */
package incognito;

import incognito.MacTransformer;
import java.lang.instrument.Instrumentation;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IncognitoAgent {
    public static byte[] FAKE_MAC = null;
    public static volatile boolean MAC_PATCH_OK = false;
    public static volatile long FAKE_MAX_MEM_BYTES = 0L;
    public static volatile int FAKE_CPU = 0;
    public static volatile boolean PLATFORM_PATCH_APPLIED = false;
    private static final Map<String, byte[]> PER_IFACE = new ConcurrentHashMap<String, byte[]>();
    private static volatile String primaryName = null;
    private static volatile boolean primaryResolved = false;
    private static final Set<String> REAL_NICS = ConcurrentHashMap.newKeySet();
    public static volatile boolean REFLECTION_GUARD_ACTIVE = false;
    private static volatile boolean reflectionHideLogged = false;
    private static final Set<String> seenReflectionProbes = ConcurrentHashMap.newKeySet();
    private static volatile boolean inputArgsFilterLogged = false;

    public static void premain(String string, Instrumentation instrumentation) {
        IncognitoAgent.spoofJavaIdentity();
        IncognitoAgent.parseMachineInfoSpoof();
        String string2 = System.getProperty("incognito.mac", "");
        byte[] byArray = FAKE_MAC = string2.isEmpty() ? null : IncognitoAgent.parseMac(string2);
        if (!string2.isEmpty() && FAKE_MAC == null) {
            System.out.println("[Incognito Agent] Failed to parse MAC: " + string2);
        } else if (FAKE_MAC != null) {
            System.out.println("[Incognito Agent] Fake MAC loaded: " + string2);
            IncognitoAgent.snapshotRealNics();
        }
        MacTransformer macTransformer = new MacTransformer();
        instrumentation.addTransformer(macTransformer, true);
        IncognitoAgent.armInputArgsFilter(instrumentation);
        if (FAKE_MAC != null) {
            try {
                instrumentation.retransformClasses(NetworkInterface.class);
                System.out.println("[Incognito Agent] NetworkInterface.getHardwareAddress() PATCHED");
            }
            catch (Exception exception) {
                System.err.println("[Incognito Agent] retransform FAILED: " + exception.getMessage());
            }
            MAC_PATCH_OK = IncognitoAgent.verifyMacPatched();
            if (MAC_PATCH_OK) {
                System.out.println("[Incognito Agent] MAC patch VERIFIED \u2014 login block will send the fake MAC");
            } else {
                System.err.println("[Incognito Agent] *** MAC PATCH NOT VERIFIED *** the REAL hardware MAC may leak in the login block \u2014 do NOT run multiple accounts until this is fixed");
            }
        } else {
            System.out.println("[Incognito Agent] No incognito.mac property set - MAC patch inactive");
        }
        if (FAKE_MAX_MEM_BYTES > 0L || FAKE_CPU > 0) {
            System.out.println("[Incognito Agent] Machine-info spoof armed -> maxMem=" + (String)(FAKE_MAX_MEM_BYTES > 0L ? FAKE_MAX_MEM_BYTES / 0x100000L + "MB" : "real") + ", cpu=" + String.valueOf(FAKE_CPU > 0 ? Integer.valueOf(FAKE_CPU) : "real") + " (applied when DesktopPlatformInfoProvider loads)");
        }
    }

    private static void parseMachineInfoSpoof() {
        String string;
        try {
            long l;
            string = System.getProperty("incognito.maxMem", "").trim();
            if (!string.isEmpty() && (l = Long.parseLong(string)) > 0L) {
                FAKE_MAX_MEM_BYTES = l * 1024L * 1024L;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            int n;
            string = System.getProperty("incognito.cpu", "").trim();
            if (!string.isEmpty() && (n = Integer.parseInt(string)) > 0) {
                FAKE_CPU = n;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static long getReportedMaxMemory() {
        long l = FAKE_MAX_MEM_BYTES;
        return l > 0L ? l : Runtime.getRuntime().maxMemory();
    }

    public static int getReportedCpuCount() {
        int n = FAKE_CPU;
        return n > 0 ? n : Runtime.getRuntime().availableProcessors();
    }

    public static void markPlatformPatched() {
        PLATFORM_PATCH_APPLIED = true;
    }

    private static void armInputArgsFilter(Instrumentation instrumentation) {
        try {
            Class<?> clazz = Class.forName("sun.management.RuntimeImpl", false, ClassLoader.getPlatformClassLoader());
            if (instrumentation.isModifiableClass(clazz)) {
                instrumentation.retransformClasses(clazz);
            }
            System.out.println("[Incognito Agent] Input-args filter armed on sun.management.RuntimeImpl");
        }
        catch (Throwable throwable) {
            System.out.println("[Incognito Agent] Input-args filter will apply on RuntimeImpl load (" + String.valueOf(throwable) + ")");
        }
    }

    public static boolean hideReflectionClass(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        String string2 = string.replace('/', '.');
        return string2.startsWith("net.runelite.client.plugins.svc") || string2.equals("incognito") || string2.startsWith("incognito.");
    }

    public static void guardReflectionName(String string) throws ClassNotFoundException {
        boolean bl = IncognitoAgent.hideReflectionClass(string);
        if (string != null && seenReflectionProbes.size() < 300 && seenReflectionProbes.add(string)) {
            System.out.println("[Incognito Agent] Server reflection probe: " + string + (bl ? "   <<< x64 class \u2014 HIDDEN (reported not-found, like stock Orion)" : ""));
        }
        if (bl) {
            if (!reflectionHideLogged) {
                reflectionHideLogged = true;
                REFLECTION_GUARD_ACTIVE = true;
                System.out.println("[Incognito Agent] *** Reflection-check oracle defense ACTIVE \u2014 the RSPS IS probing x64 class names; they now report NOT FOUND, like stock Orion ***");
            }
            throw new ClassNotFoundException(string);
        }
    }

    public static List<String> filterInputArgs(List<String> list) {
        if (list == null) {
            return list;
        }
        ArrayList<String> arrayList = new ArrayList<String>(list.size());
        for (String string : list) {
            String string2;
            boolean bl;
            if (string == null || (bl = (string2 = string.trim().toLowerCase()).startsWith("-javaagent") || string2.contains("incognito") || string2.startsWith("-dorion.") || string2.startsWith("-xx:+disableattachmechanism") || string2.startsWith("-dunethicalite.cache-dir") || string2.startsWith("-xx:activeprocessorcount") || string2.startsWith("-xx:errorfile=") || string2.startsWith("-dos.name=") || string2.startsWith("-dos.version=") || string2.startsWith("-dos.arch=") || string2.startsWith("-duser.name=") || string2.startsWith("-duser.timezone=") || string2.startsWith("-duser.language=") || string2.startsWith("-duser.country=") || string2.startsWith("-djava.version=") || string2.startsWith("-djava.vendor="))) continue;
            arrayList.add(string);
        }
        if (!inputArgsFilterLogged && arrayList.size() != list.size()) {
            inputArgsFilterLogged = true;
            System.out.println("[Incognito Agent] Input-args filter ACTIVE \u2014 hid " + (list.size() - arrayList.size()) + " spoofer/instrumentation args from getInputArguments() (stock-Orion-like list returned)");
        }
        return arrayList;
    }

    private static boolean verifyMacPatched() {
        if (FAKE_MAC == null) {
            return false;
        }
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration != null && enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                try {
                    byte[] byArray;
                    if (networkInterface.isLoopback() || !networkInterface.isUp() || (byArray = networkInterface.getHardwareAddress()) == null || byArray.length != 6) continue;
                    return byArray[0] == FAKE_MAC[0] && byArray[1] == FAKE_MAC[1] && byArray[2] == FAKE_MAC[2];
                }
                catch (Throwable throwable) {
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public static byte[] getFakeMac() {
        return FAKE_MAC != null ? (byte[])FAKE_MAC.clone() : null;
    }

    private static void spoofJavaIdentity() {
        String string = System.getProperty("incognito.javaVersion", "");
        String string2 = System.getProperty("incognito.javaVendor", "");
        if (!string.isEmpty()) {
            System.setProperty("java.version", string);
            System.setProperty("java.vm.version", string);
            System.setProperty("java.runtime.version", string);
        }
        if (!string2.isEmpty()) {
            System.setProperty("java.vendor", string2);
            System.setProperty("java.vm.vendor", string2);
        }
        if (!string.isEmpty() || !string2.isEmpty()) {
            System.out.println("[Incognito Agent] Java identity spoofed -> " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        }
    }

    public static byte[] getFakeMacFor(NetworkInterface networkInterface) {
        if (FAKE_MAC == null) {
            return null;
        }
        if (networkInterface == null) {
            return (byte[])FAKE_MAC.clone();
        }
        try {
            if (networkInterface.isLoopback()) {
                return null;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        String string = null;
        try {
            string = networkInterface.getName();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (string == null) {
            string = "";
        }
        if (!REAL_NICS.isEmpty() && !REAL_NICS.contains(string)) {
            return null;
        }
        byte[] byArray = PER_IFACE.get(string);
        if (byArray != null) {
            return (byte[])byArray.clone();
        }
        IncognitoAgent.resolvePrimary();
        byte[] byArray2 = primaryName == null || primaryName.equals(string) ? (byte[])FAKE_MAC.clone() : IncognitoAgent.deriveMac(string);
        PER_IFACE.put(string, byArray2);
        return (byte[])byArray2.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void resolvePrimary() {
        if (primaryResolved) {
            return;
        }
        Class<IncognitoAgent> clazz = IncognitoAgent.class;
        synchronized (IncognitoAgent.class) {
            if (primaryResolved) {
                // ** MonitorExit[var0] (shouldn't be in output)
                return;
            }
            try {
                Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                while (enumeration != null && enumeration.hasMoreElements()) {
                    NetworkInterface networkInterface = enumeration.nextElement();
                    try {
                        if (networkInterface.isLoopback()) continue;
                        String string = networkInterface.getName();
                        if (REAL_NICS.isEmpty() ? !networkInterface.isUp() : !REAL_NICS.contains(string)) continue;
                        primaryName = string;
                        break;
                    }
                    catch (Exception exception) {
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            primaryResolved = true;
            // ** MonitorExit[var0] (shouldn't be in output)
            return;
        }
    }

    private static void snapshotRealNics() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration != null && enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                try {
                    byte[] byArray;
                    if (networkInterface.isLoopback() || (byArray = networkInterface.getHardwareAddress()) == null || byArray.length != 6) continue;
                    REAL_NICS.add(networkInterface.getName());
                }
                catch (Exception exception) {}
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static byte[] deriveMac(String string) {
        int n = -2128831035;
        for (int i = 0; i < string.length(); ++i) {
            n ^= string.charAt(i);
            n *= 16777619;
        }
        byte[] byArray = new byte[]{FAKE_MAC[0], FAKE_MAC[1], FAKE_MAC[2], (byte)n, (byte)(n >>> 8), (byte)(n >>> 16)};
        return byArray;
    }

    private static byte[] parseMac(String string) {
        try {
            String[] stringArray = string.split(":");
            if (stringArray.length != 6) {
                return null;
            }
            byte[] byArray = new byte[6];
            for (int i = 0; i < 6; ++i) {
                byArray[i] = (byte)Integer.parseInt(stringArray[i], 16);
            }
            return byArray;
        }
        catch (Exception exception) {
            return null;
        }
    }
}
