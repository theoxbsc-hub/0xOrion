package ox.incognito;

import java.lang.instrument.Instrumentation;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM agent: spoofs NIC MAC + reported CPU/RAM + java identity for per-account isolation.
 * Driven by -Dox.* system properties written by the 0x launcher identity script.
 */
public final class OxIncognitoAgent {
    public static byte[] FAKE_MAC = null;
    public static volatile boolean MAC_PATCH_OK = false;
    public static volatile long FAKE_MAX_MEM_BYTES = 0L;
    public static volatile int FAKE_CPU = 0;

    private static final Map<String, byte[]> PER_IFACE = new ConcurrentHashMap<>();
    private static volatile String primaryName = null;
    private static volatile boolean primaryResolved = false;
    private static final Set<String> REAL_NICS = ConcurrentHashMap.newKeySet();
    private static volatile boolean inputArgsFilterLogged = false;

    private OxIncognitoAgent() {
    }

    public static void premain(String args, Instrumentation instrumentation) {
        spoofJavaIdentity();
        parseMachineInfoSpoof();

        String macProp = firstNonEmpty(
            System.getProperty("ox.mac", ""),
            System.getProperty("incognito.mac", "")
        );
        FAKE_MAC = macProp.isEmpty() ? null : parseMac(macProp);
        if (!macProp.isEmpty() && FAKE_MAC == null) {
            AgentLog.info("Failed to parse MAC: " + macProp);
        } else if (FAKE_MAC != null) {
            AgentLog.info("Fake MAC loaded: " + macProp);
            snapshotRealNics();
        }

        MacTransformer transformer = new MacTransformer();
        instrumentation.addTransformer(transformer, true);
        armInputArgsFilter(instrumentation);

        if (FAKE_MAC != null) {
            try {
                instrumentation.retransformClasses(NetworkInterface.class);
                AgentLog.info("NetworkInterface.getHardwareAddress() PATCHED");
            } catch (Exception e) {
                AgentLog.error("retransform FAILED: " + e.getMessage());
            }
            MAC_PATCH_OK = verifyMacPatched();
            if (MAC_PATCH_OK) {
                AgentLog.important("MAC OK");
            } else {
                AgentLog.error("MAC PATCH FAILED - real MAC may leak");
            }
        } else {
            AgentLog.error("No ox.mac set - MAC inactive");
        }

        if (FAKE_MAX_MEM_BYTES > 0L || FAKE_CPU > 0) {
            AgentLog.info("Machine-info spoof armed maxMem="
                + (FAKE_MAX_MEM_BYTES > 0L ? (FAKE_MAX_MEM_BYTES / 0x100000L) + "MB" : "real")
                + " cpu=" + (FAKE_CPU > 0 ? FAKE_CPU : "real"));
        }
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b == null ? "" : b.trim();
    }

    private static void parseMachineInfoSpoof() {
        try {
            String mem = firstNonEmpty(
                System.getProperty("ox.maxMem", ""),
                System.getProperty("incognito.maxMem", "")
            );
            if (!mem.isEmpty()) {
                long mb = Long.parseLong(mem);
                if (mb > 0L) {
                    FAKE_MAX_MEM_BYTES = mb * 1024L * 1024L;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            String cpu = firstNonEmpty(
                System.getProperty("ox.cpu", ""),
                System.getProperty("incognito.cpu", "")
            );
            if (!cpu.isEmpty()) {
                int n = Integer.parseInt(cpu);
                if (n > 0) {
                    FAKE_CPU = n;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static long getReportedMaxMemory() {
        return FAKE_MAX_MEM_BYTES > 0L ? FAKE_MAX_MEM_BYTES : Runtime.getRuntime().maxMemory();
    }

    public static int getReportedCpuCount() {
        return FAKE_CPU > 0 ? FAKE_CPU : Runtime.getRuntime().availableProcessors();
    }

    private static void armInputArgsFilter(Instrumentation instrumentation) {
        try {
            Class<?> clazz = Class.forName("sun.management.RuntimeImpl", false, ClassLoader.getPlatformClassLoader());
            if (instrumentation.isModifiableClass(clazz)) {
                instrumentation.retransformClasses(clazz);
            }
            AgentLog.info("Input-args filter armed");
        } catch (Throwable t) {
            AgentLog.info("Input-args filter deferred (" + t + ")");
        }
    }

    public static List<String> filterInputArgs(List<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<String> out = new ArrayList<>(list.size());
        for (String arg : list) {
            if (arg == null) {
                continue;
            }
            String t = arg.trim().toLowerCase();
            if (t.startsWith("-javaagent")
                || t.contains("incognito")
                || t.contains("ox.incognito")
                || t.startsWith("-dox.")
                || t.startsWith("-dincognito")
                || t.startsWith("-xx:+disableattachmechanism")
                || t.startsWith("-dunethicalite.cache-dir")
                || t.startsWith("-xx:activeprocessorcount")
                || t.startsWith("-dos.name=")
                || t.startsWith("-dos.version=")
                || t.startsWith("-dos.arch=")
                || t.startsWith("-duser.name=")
                || t.startsWith("-duser.timezone=")
                || t.startsWith("-djava.version=")
                || t.startsWith("-djava.vendor=")) {
                continue;
            }
            out.add(arg);
        }
        if (!inputArgsFilterLogged && out.size() != list.size()) {
            inputArgsFilterLogged = true;
            AgentLog.info("Hid " + (list.size() - out.size()) + " identity args from getInputArguments()");
        }
        return out;
    }

    private static boolean verifyMacPatched() {
        if (FAKE_MAC == null) {
            return false;
        }
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en != null && en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                try {
                    if (ni.isLoopback() || !ni.isUp()) {
                        continue;
                    }
                    byte[] hw = ni.getHardwareAddress();
                    if (hw == null || hw.length != 6) {
                        continue;
                    }
                    return hw[0] == FAKE_MAC[0] && hw[1] == FAKE_MAC[1] && hw[2] == FAKE_MAC[2];
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void spoofJavaIdentity() {
        String ver = firstNonEmpty(
            System.getProperty("ox.javaVersion", ""),
            System.getProperty("incognito.javaVersion", "")
        );
        String vendor = firstNonEmpty(
            System.getProperty("ox.javaVendor", ""),
            System.getProperty("incognito.javaVendor", "")
        );
        if (!ver.isEmpty()) {
            System.setProperty("java.version", ver);
            System.setProperty("java.vm.version", ver);
            System.setProperty("java.runtime.version", ver);
        }
        if (!vendor.isEmpty()) {
            System.setProperty("java.vendor", vendor);
            System.setProperty("java.vm.vendor", vendor);
        }
        if (!ver.isEmpty() || !vendor.isEmpty()) {
            AgentLog.info("Java identity -> "
                + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        }
    }

    public static byte[] getFakeMacFor(NetworkInterface networkInterface) {
        if (FAKE_MAC == null) {
            return null;
        }
        if (networkInterface == null) {
            return FAKE_MAC.clone();
        }
        try {
            if (networkInterface.isLoopback()) {
                return null;
            }
        } catch (Exception ignored) {
        }
        String name = "";
        try {
            name = networkInterface.getName();
        } catch (Exception ignored) {
        }
        if (name == null) {
            name = "";
        }
        if (!REAL_NICS.isEmpty() && !REAL_NICS.contains(name)) {
            return null;
        }
        byte[] cached = PER_IFACE.get(name);
        if (cached != null) {
            return cached.clone();
        }
        resolvePrimary();
        byte[] mac = primaryName == null || primaryName.equals(name)
            ? FAKE_MAC.clone()
            : deriveMac(name);
        PER_IFACE.put(name, mac);
        return mac.clone();
    }

    private static void resolvePrimary() {
        if (primaryResolved) {
            return;
        }
        synchronized (OxIncognitoAgent.class) {
            if (primaryResolved) {
                return;
            }
            try {
                Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                while (en != null && en.hasMoreElements()) {
                    NetworkInterface ni = en.nextElement();
                    try {
                        if (ni.isLoopback()) {
                            continue;
                        }
                        String name = ni.getName();
                        if (REAL_NICS.isEmpty() ? !ni.isUp() : !REAL_NICS.contains(name)) {
                            continue;
                        }
                        primaryName = name;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
            primaryResolved = true;
        }
    }

    private static void snapshotRealNics() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en != null && en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                try {
                    if (ni.isLoopback()) {
                        continue;
                    }
                    byte[] hw = ni.getHardwareAddress();
                    if (hw == null || hw.length != 6) {
                        continue;
                    }
                    REAL_NICS.add(ni.getName());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static byte[] deriveMac(String ifaceName) {
        int hash = -2128831035;
        for (int i = 0; i < ifaceName.length(); ++i) {
            hash ^= ifaceName.charAt(i);
            hash *= 16777619;
        }
        return new byte[]{
            FAKE_MAC[0], FAKE_MAC[1], FAKE_MAC[2],
            (byte) hash, (byte) (hash >>> 8), (byte) (hash >>> 16)
        };
    }

    private static byte[] parseMac(String value) {
        try {
            String[] parts = value.split(":");
            if (parts.length != 6) {
                return null;
            }
            byte[] out = new byte[6];
            for (int i = 0; i < 6; ++i) {
                out[i] = (byte) Integer.parseInt(parts[i], 16);
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }
}
