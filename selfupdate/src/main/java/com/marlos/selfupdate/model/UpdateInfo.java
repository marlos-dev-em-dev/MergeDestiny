package com.marlos.selfupdate.model;

import java.util.ArrayList;
import java.util.List;

public class UpdateInfo {
    private final String version;
    private final int versionCode;
    private final String downloadUrl;
    private final String sha256;
    private final List<String> changelog;
    private final UpdateType type;

    public UpdateInfo(String version, int versionCode, String downloadUrl, String sha256,
                      List<String> changelog, UpdateType type) {
        this.version = version;
        this.versionCode = versionCode;
        this.downloadUrl = downloadUrl;
        this.sha256 = sha256;
        this.changelog = changelog == null ? new ArrayList<>() : changelog;
        this.type = type;
    }
    public String getVersion() { return version; }
    public int getVersionCode() { return versionCode; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getSha256() { return sha256; }
    public List<String> getChangelog() { return changelog; }
    public UpdateType getType() { return type; }
    public boolean isMandatory() {return type != null && type.isMandatory();}
    public String getChangelogText() { return String.join("\n", changelog); }
}
