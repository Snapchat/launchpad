package com.snapchat.launchpad.conversion.components;


import com.snapchat.launchpad.conversion.schemas.ConversionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("conversion-log")
@Component
public class ConversionLogger {
    private final Logger logger = LoggerFactory.getLogger(ConversionLogger.class);

    public void logConversion(ConversionLog conversionLog) {
        logger.info(conversionLog.toString());
    }
}
