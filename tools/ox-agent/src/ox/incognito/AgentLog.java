package ox.incognito;

final class AgentLog {
    private AgentLog() {
    }

    static boolean quiet() {
        return !"false".equalsIgnoreCase(System.getProperty("ox.quiet", "true"));
    }

    static void info(String msg) {
        if (!quiet()) {
            System.out.println("[0x Agent] " + msg);
        }
    }

    static void important(String msg) {
        // Quiet by default — banner/plugins handle the useful status
        if (!quiet()) {
            System.out.println("[0x Agent] " + msg);
        }
    }

    static void error(String msg) {
        System.err.println("[0x Agent] ERROR " + msg);
    }
}
