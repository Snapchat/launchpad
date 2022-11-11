package com.snapchat.launchpad.conversion.utils;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.cloud.storage.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    private static AmazonS3 S3 = null;
    private static Storage GCS = null;

    static {
        try {
            S3 = AmazonS3ClientBuilder.defaultClient();
        } catch (Exception ex) {
            logger.error("Failed to initialize S3: ", ex);
        }

        try {
            GCS = StorageOptions.getDefaultInstance().getService();
        } catch (Exception ex) {
            logger.error("Failed to initialize GCS: ", ex);
        }
    }

    private FileStorage() {}

    public static void upload(String destination, File file)
            throws URISyntaxException, IOException {
        URI uri = new URI(destination);
        if (Objects.equals(uri.getScheme(), "s3") && S3 != null) {
            moveToS3(uri.getHost(), uri.getPath().substring(1), file);
        } else if (Objects.equals(uri.getScheme(), "gs") && GCS != null) {
            moveToGCS(uri.getHost(), uri.getPath().substring(1), file);
        } else if (Objects.equals(uri.getScheme(), "file")) {
            moveToFS(uri.getPath(), file);
        } else {
            throw new RuntimeException("Unsupported protocol!");
        }
    }

    private static void moveToS3(String bucket, String path, File file) {
        S3.putObject(bucket, path, file);
    }

    private static void moveToGCS(String bucket, String path, File file) throws IOException {
        BlobId blobId = BlobId.of(bucket, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        try (FileInputStream is = new FileInputStream(file)) {
            GCS.create(blobInfo, is.readAllBytes());
        }
    }

    private static void moveToFS(String path, File file) throws IOException {
        Path target = Path.of(path);
        Files.createDirectories(target.getParent());
        Files.copy(Path.of(file.getPath()), target);
    }
}
