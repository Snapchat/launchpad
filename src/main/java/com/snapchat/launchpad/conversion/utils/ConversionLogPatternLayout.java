package com.snapchat.launchpad.conversion.utils;


import ch.qos.logback.classic.PatternLayout;
import com.snapchat.launchpad.conversion.schemas.ConversionLog;

public class ConversionLogPatternLayout extends PatternLayout {
    @Override
    public String getFileHeader() {
        return String.join(",", ConversionLog.getHeader());
    }
}
