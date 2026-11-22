package com.marlos.selfupdate.version;

import com.marlos.selfupdate.model.UpdateType;

public final class UpdateClassifier {
    private UpdateClassifier() {}
    public static UpdateType classify(Version installed, Version remote) {
        if (remote.major > installed.major) return UpdateType.MAJOR;
        if (remote.minor > installed.minor) return UpdateType.MINOR;
        return UpdateType.PATCH;
    }
}
