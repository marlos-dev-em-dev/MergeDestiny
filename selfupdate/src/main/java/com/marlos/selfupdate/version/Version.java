package com.marlos.selfupdate.version;

public final class Version implements Comparable<Version> {
    public final int major, minor, patch;
    private Version(int major, int minor, int patch) {
        this.major = major; this.minor = minor; this.patch = patch;
    }
    public static Version parse(String value) {
        if (value == null || !value.matches("\\d+\\.\\d+\\.\\d+"))
            throw new IllegalArgumentException("Versão inválida: " + value);
        String[] p = value.split("\\.");
        return new Version(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
    }
    @Override public int compareTo(Version other) {
        if (major != other.major) return Integer.compare(major, other.major);
        if (minor != other.minor) return Integer.compare(minor, other.minor);
        return Integer.compare(patch, other.patch);
    }
}
