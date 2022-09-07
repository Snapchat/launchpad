package com.snapchat.launchpad.conversion.schemas;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ConversionResponse {
    @JsonProperty("status")
    private final Status status;

    @JsonProperty("reason")
    private final String reason;

    @JsonProperty("errorRecords")
    private final List<ResponseErrorRecords> errorRecords;

    public ConversionResponse(
            Status status, String reason, List<ResponseErrorRecords> errorRecords) {
        this.status = status;
        this.reason = reason;
        this.errorRecords = errorRecords;
    }

    public static class ConversionResponseBuilder {
        private ConversionResponse.Status status = Status.UNSET;
        private String reason = "";
        private List<ResponseErrorRecords> errorRecords = new ArrayList<>();

        public ConversionResponseBuilder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public ConversionResponseBuilder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public ConversionResponseBuilder setErrorRecords(List<ResponseErrorRecords> errorRecords) {
            this.errorRecords = errorRecords;
            return this;
        }

        public ConversionResponseBuilder addErrorRecords(ResponseErrorRecords errorRecords) {
            this.errorRecords.add(errorRecords);
            return this;
        }

        public ConversionResponseBuilder removeErrorRecords(ResponseErrorRecords errorRecords) {
            this.errorRecords.remove(errorRecords);
            return this;
        }

        public ConversionResponse createConversionResponse() {
            return new ConversionResponse(status, reason, errorRecords);
        }
    }

    public enum Status {
        UNSET,
        SUCCESS,
        FAILED
    }

    public static class ResponseErrorRecords {
        @JsonProperty("reason")
        private final String reason;

        @JsonProperty("recordIndexes")
        private final List<Integer> recordIndexes;

        public ResponseErrorRecords(String reason, List<Integer> recordIndexes) {
            this.reason = reason;
            this.recordIndexes = recordIndexes;
        }

        public static class ResponseErrorRecordsBuilder {
            private String reason = "";
            private List<Integer> recordIndexes = new ArrayList<>();

            public ResponseErrorRecordsBuilder setReason(String reason) {
                this.reason = reason;
                return this;
            }

            public ResponseErrorRecordsBuilder setRecordIndexes(List<Integer> recordIndexes) {
                this.recordIndexes = recordIndexes;
                return this;
            }

            public ResponseErrorRecordsBuilder addRecordIndexes(Integer recordIndex) {
                this.recordIndexes.add(recordIndex);
                return this;
            }

            public ResponseErrorRecordsBuilder setRecordIndexes(Integer recordIndex) {
                this.recordIndexes.remove(recordIndex);
                return this;
            }

            public ResponseErrorRecords createResponseErrorRecords() {
                return new ResponseErrorRecords(reason, recordIndexes);
            }
        }
    }
}
