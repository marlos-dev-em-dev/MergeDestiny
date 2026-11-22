package com.marlos.selfupdate.model;

public enum UpdateType {
    PATCH,
    MINOR,
    MAJOR;

    public boolean isMandatory() {
        return this == MAJOR;
    }
}
