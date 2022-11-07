package com.snapchat.launchpad.conversion.utils;


import com.snapchat.launchpad.conversion.schemas.MpcLoggingRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcLogger {
    private final Logger logger = LoggerFactory.getLogger(MpcLogger.class);

    public void logMpc(MpcLoggingRow mpcLoggingRow) {
        logger.info(mpcLoggingRow.toString());
    }
}
