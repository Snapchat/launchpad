package com.snapchat.launchpad.conversion.utils;


import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcLogger {
    private final Logger logger = LoggerFactory.getLogger(MpcLogger.class);

    public void logMpc(MpcLoggingRow mpcLoggingRow) {
        logger.info(
                "{},{},{},{},{},{},{},{},{},{}",
                mpcLoggingRow.getClientDedupId(),
                mpcLoggingRow.getPixelId(),
                mpcLoggingRow.getAppId(),
                mpcLoggingRow.getHashedEmail(),
                mpcLoggingRow.getHashedPhone(),
                mpcLoggingRow.getEventType(),
                mpcLoggingRow.getTimestamp(),
                mpcLoggingRow.getCurrency(),
                mpcLoggingRow.getPrice(),
                mpcLoggingRow.getEventConversionType());
    }

    public static class MpcLoggingRow {
        private final String clientDedupId;
        private final String pixelId;
        private final String appId;
        private final String hashedEmail;
        private final String hashedPhone;
        private final String eventType;
        private final String timestamp;
        private final String currency;
        private final String price;

        private final String eventConversionType;

        private MpcLoggingRow(
                String clientDedupId,
                String pixelId,
                String appId,
                String hashedEmail,
                String hashedPhone,
                String eventType,
                String timestamp,
                String currency,
                String price,
                String eventConversionType) {
            this.clientDedupId = clientDedupId;
            this.pixelId = pixelId;
            this.appId = appId;
            this.hashedEmail = hashedEmail;
            this.hashedPhone = hashedPhone;
            this.eventType = eventType;
            this.timestamp = timestamp;
            this.currency = currency;
            this.price = price;
            this.eventConversionType = eventConversionType;
        }

        public String getClientDedupId() {
            return clientDedupId;
        }

        public String getPixelId() {
            return pixelId;
        }

        public String getAppId() {
            return appId;
        }

        public String getHashedEmail() {
            return hashedEmail;
        }

        public String getHashedPhone() {
            return hashedPhone;
        }

        public String getEventType() {
            return eventType;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getCurrency() {
            return currency;
        }

        public String getPrice() {
            return price;
        }

        public String getEventConversionType() {
            return eventConversionType;
        }

        public static class MpcLoggingRowBuilder {
            private String clientDedupId = "";
            private String pixelId = "";
            private String appId = "";
            private String hashedEmail = "";
            private String hashedPhone = "";
            private String eventType = "";
            private String timestamp = "";
            private String currency = "";
            private String price = "";

            private String eventConversionType = "";

            public MpcLoggingRowBuilder setClientDedupId(String clientDedupId) {
                this.clientDedupId = Optional.ofNullable(clientDedupId).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setPixelId(String pixelId) {
                this.pixelId = Optional.ofNullable(pixelId).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setAppId(String appId) {
                this.appId = Optional.ofNullable(appId).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setHashedEmail(String hashedEmail) {
                this.hashedEmail = Optional.ofNullable(hashedEmail).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setHashedPhone(String hashedPhone) {
                this.hashedPhone = Optional.ofNullable(hashedPhone).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setEventType(String eventType) {
                this.eventType = Optional.ofNullable(eventType).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setTimestamp(String timestamp) {
                this.timestamp = Optional.ofNullable(timestamp).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setCurrency(String currency) {
                this.currency = Optional.ofNullable(currency).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setPrice(String price) {
                this.price = Optional.ofNullable(price).orElse("");
                return this;
            }

            public MpcLoggingRowBuilder setEventConversionType(String eventConversionType) {
                this.eventConversionType = Optional.ofNullable(eventConversionType).orElse("");
                return this;
            }

            public MpcLogger.MpcLoggingRow createMpcLoggingRow() {
                return new MpcLogger.MpcLoggingRow(
                        clientDedupId,
                        pixelId,
                        appId,
                        hashedEmail,
                        hashedPhone,
                        eventType,
                        timestamp,
                        currency,
                        price,
                        eventConversionType);
            }
        }
    }
}
