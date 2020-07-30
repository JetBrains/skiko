package org.jetbrains.awthrl.Common;

public enum OSType {
    MAC_OS, LINUX, WINDOWS, UNKNOWN;
    
    private static OSType current = null;
    
    public static OSType getCurrent() {
        if (current == null) {
            current = defineOsType();
        }
        return current;
    }

    private static OSType defineOsType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (isWindows(osName)) {
            return WINDOWS;
        }
        if (isMac(osName)) {
            return MAC_OS;
        }
        if (isUnix(osName)) {
            return LINUX;
        }
        return UNKNOWN;
    }

    private static boolean isWindows(String name) {
        return (name.indexOf("win") >= 0);
    }

    private static boolean isMac(String name) {
        return (name.indexOf("mac") >= 0);
    }

    private static boolean isUnix(String name) {
        return (name.indexOf("nix") >= 0 || name.indexOf("nux") >= 0 || name.indexOf("aix") >= 0);
    }
}