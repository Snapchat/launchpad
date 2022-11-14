package com.snapchat.launchpad.conversion.utils;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    private static AmazonS3 S3;
    private static Storage GCS;

    static AmazonS3 getAmazonS3() {
        if (S3 != null) {
            return S3;
        }
        try {
            S3 = AmazonS3ClientBuilder.defaultClient();
        } catch (Exception ex) {
            logger.error("Failed to initialize S3: ", ex);
        }
        return S3;
    }

    static Storage getStorage() {
        if (GCS != null) {
            return GCS;
        }
        try {
            GCS = StorageOptions.getDefaultInstance().getService();
        } catch (Exception ex) {
            logger.error("Failed to initialize GCS: ", ex);
        }
        return GCS;
    }

    private FileStorage() {}

    public static URL getPresignedUrl(String destination) {
        URI uri = URI.create(destination);
        StorageType storageType = StorageType.fromString(uri.getScheme());
        switch (storageType) {
            case S3:
                return getPresignedUrlS3(uri.getHost(), uri.getPath().substring(1));
            case GCS:
                return getPresignedUrlGcs(uri.getHost(), uri.getPath().substring(1));
            default:
                throw new IllegalArgumentException(
                        String.format(
                                "No supported url pre-signature for protocol: %s...",
                                uri.getScheme()));
        }
    }

    private static URL getPresignedUrlS3(String bucket, String path) {
        return getAmazonS3()
                .generatePresignedUrl(
                        bucket,
                        path,
                        new Date(System.currentTimeMillis() + 1800 * 1000),
                        com.amazonaws.HttpMethod.PUT);
    }

    private static URL getPresignedUrlGcs(String bucket, String path) {
        return getStorage()
                .signUrl(
                        BlobInfo.newBuilder(BlobId.of(bucket, path)).build(),
                        1800,
                        TimeUnit.SECONDS,
                        Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.PUT),
                        Storage.SignUrlOption.withV4Signature());
    }

    public static void upload(String destination, File file) throws IOException {
        upload(destination, new FileInputStream(file));
    }

    public static void upload(String destination, InputStream is) throws IOException {
        URI uri = URI.create(destination);
        StorageType storageType = StorageType.fromString(uri.getScheme());
        switch (storageType) {
            case S3:
                writeToS3(uri.getHost(), uri.getPath().substring(1), is);
                break;
            case GCS:
                writeToGCS(uri.getHost(), uri.getPath().substring(1), is);
                break;
            case FS:
                writeToFS(uri.getPath(), is);
        }
    }

    private static void writeToS3(String bucket, String path, InputStream is) throws IOException {
        getAmazonS3().putObject(bucket, path, is, new ObjectMetadata());
    }

    private static void writeToGCS(String bucket, String path, InputStream is) throws IOException {
        getStorage()
                .create(
                        BlobInfo.newBuilder(BlobId.of(bucket, path))
                                .setContentType("text/plain")
                                .build(),
                        is.readAllBytes());
    }

    private static void writeToFS(String path, InputStream is) throws IOException {
        Path target = Path.of(path);
        Files.createDirectories(target.getParent());
        Files.write(target, is.readAllBytes());
    }

    private enum StorageType {
        S3("s3"),
        GCS("gs"),
        FS("file");

        private final String text;

        StorageType(final String text) {
            this.text = text;
        }

        public static StorageType fromString(String text) {
            for (StorageType storageType : StorageType.values()) {
                if (storageType.text.equals(text)) {
                    return storageType;
                }
            }
            throw new IllegalArgumentException(String.format("No enum defined for: %s...", text));
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
