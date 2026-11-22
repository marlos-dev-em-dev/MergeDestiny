package com.marlos.selfupdate.model;

public class UpdateResult {
    public enum Status { UPDATE_AVAILABLE, UP_TO_DATE, ERROR }
    private final Status status;
    private final UpdateInfo updateInfo;
    private final String message;

    private UpdateResult(Status status, UpdateInfo info, String message) {
        this.status = status; this.updateInfo = info; this.message = message;
    }
    public static UpdateResult available(UpdateInfo info) { return new UpdateResult(Status.UPDATE_AVAILABLE, info, null); }
    public static UpdateResult upToDate() { return new UpdateResult(Status.UP_TO_DATE, null, null); }
    public static UpdateResult error(String message) { return new UpdateResult(Status.ERROR, null, message); }
    public Status getStatus() { return status; }
    public UpdateInfo getUpdateInfo() { return updateInfo; }
    public String getMessage() { return message; }
}
