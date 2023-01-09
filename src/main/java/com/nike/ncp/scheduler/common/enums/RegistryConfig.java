package com.nike.ncp.scheduler.common.enums;


public final class RegistryConfig {

    private RegistryConfig() {

    }

    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;
    public enum RegistType { EXECUTOR, ADMIN }

}
