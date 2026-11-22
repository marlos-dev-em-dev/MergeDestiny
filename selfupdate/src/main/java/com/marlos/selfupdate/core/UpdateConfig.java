package com.marlos.selfupdate.core;

public class UpdateConfig {
    private final String manifestUrl;
    private final String applicationId;

    public UpdateConfig(String manifestUrl, String applicationId) {
        if (manifestUrl == null || !manifestUrl.startsWith("https://"))
            throw new IllegalArgumentException("O manifesto precisa usar HTTPS.");
        this.manifestUrl = manifestUrl;
        this.applicationId = applicationId;
    }
    public String getManifestUrl() { return manifestUrl; }
    public String getApplicationId() { return applicationId; }
}
