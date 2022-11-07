package com.snapchat.launchpad.conversion.utils;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;
import java.util.UUID;

public class UniqueIdDiscriminator implements Discriminator<ILoggingEvent> {
    private static final String uuid = UUID.randomUUID().toString();
    private static final String KEY = "INSTANCE_ID";
    private boolean started;

    public String getDiscriminatingValue(ILoggingEvent iLoggingEvent) {
        return uuid;
    }

    public String getKey() {
        return KEY;
    }

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
