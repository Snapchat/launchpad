package com.snapchat.launchpad.conversion.utils;


import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import com.snapchat.launchpad.common.utils.FileStorage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStorageSizeAndTimeBasedRollingPolicy<E> extends SizeAndTimeBasedRollingPolicy<E> {

    private static final Logger logger =
            LoggerFactory.getLogger(CloudStorageSizeAndTimeBasedRollingPolicy.class);

    ExecutorService executor = Executors.newFixedThreadPool(4);

    private String remotePathPrefix;

    public String getRemotePrefix() {
        return remotePathPrefix;
    }

    public void setRemotePathPrefix(String remotePathPrefix) {
        this.remotePathPrefix = remotePathPrefix;
    }

    @Override
    public void start() {
        super.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));
    }

    @Override
    public void rollover() throws RolloverFailure {
        super.rollover();
        uploadFileToCloudAsync();
    }

    private void uploadFileToCloudAsync() {
        File file =
                new File(getTimeBasedFileNamingAndTriggeringPolicy().getElapsedPeriodsFileName());
        if (file.exists() && file.length() > 0) {
            executor.execute(
                    () -> {
                        String destinationPath = remotePathPrefix + "/" + file.getPath();
                        try {
                            logger.info(
                                    String.format(
                                            "Copying file %s to %s...",
                                            file.getPath(), destinationPath));
                            FileStorage.upload(destinationPath, file);
                        } catch (Exception ex) {
                            logger.error("Failed to move file...", ex);
                        }
                    });
        }
    }

    class ShutdownHookRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (getTimeBasedFileNamingAndTriggeringPolicy().getElapsedPeriodsFileName()
                        != null) {
                    rollover();
                }
            } catch (Exception ex) {
                logger.error("Failed to cleanup last batch...", ex);
            } finally {
                executor.shutdown();
            }
        }
    }
}
