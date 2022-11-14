package com.snapchat.launchpad.common.utils;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class FileStorageTest {

    @Test
    public void Upload_a_file_S3() throws IOException {
        try (MockedStatic<FileStorage> fileStorage =
                Mockito.mockStatic(FileStorage.class, Mockito.CALLS_REAL_METHODS)) {
            String bucket = "bucket";
            String path = "test.csv";
            String dest = String.format("s3://%s/%s", bucket, path);
            String content = "test";

            AmazonS3 mockedAmazonS3 = Mockito.mock(AmazonS3.class);
            fileStorage.when(FileStorage::getAmazonS3).thenReturn(mockedAmazonS3);

            Path tempFilePath = Files.createTempFile("", "");
            Files.write(tempFilePath, content.getBytes());
            FileStorage.upload(dest, new File(tempFilePath.toUri()));

            ArgumentCaptor<String> bucketArgumentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> pathArgumentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<InputStream> contentIsArgumentCaptor =
                    ArgumentCaptor.forClass(InputStream.class);
            Mockito.verify(mockedAmazonS3, Mockito.times(1))
                    .putObject(
                            bucketArgumentCaptor.capture(),
                            pathArgumentCaptor.capture(),
                            contentIsArgumentCaptor.capture(),
                            Mockito.any(ObjectMetadata.class));
            Assertions.assertEquals(bucket, bucketArgumentCaptor.getValue());
            Assertions.assertEquals(path, pathArgumentCaptor.getValue());
            Assertions.assertArrayEquals(
                    contentIsArgumentCaptor.getValue().readAllBytes(), content.getBytes());
        }
    }

    @Test
    public void Upload_a_file_GCS() throws IOException {
        try (MockedStatic<FileStorage> fileStorage =
                Mockito.mockStatic(FileStorage.class, Mockito.CALLS_REAL_METHODS)) {
            String bucket = "bucket";
            String path = "test.csv";
            String dest = String.format("gs://%s/%s", bucket, path);
            String content = "test";

            Storage mockedStorage = Mockito.mock(Storage.class);
            fileStorage.when(FileStorage::getStorage).thenReturn(mockedStorage);

            Path tempFilePath = Files.createTempFile("", "");
            Files.write(tempFilePath, content.getBytes());
            FileStorage.upload(dest, new File(tempFilePath.toUri()));

            ArgumentCaptor<BlobInfo> blobInfoArgumentCaptor =
                    ArgumentCaptor.forClass(BlobInfo.class);
            ArgumentCaptor<byte[]> bytesArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
            Mockito.verify(mockedStorage, Mockito.times(1))
                    .create(blobInfoArgumentCaptor.capture(), bytesArgumentCaptor.capture());
            Assertions.assertEquals(
                    BlobInfo.newBuilder(BlobId.of(bucket, path))
                            .setContentType("text/plain")
                            .build(),
                    blobInfoArgumentCaptor.getValue());
            Assertions.assertArrayEquals(content.getBytes(), bytesArgumentCaptor.getValue());
        }
    }
}
