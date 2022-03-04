package com.tencent.liteav.demo.services.room.bean.http;

import com.google.gson.annotations.SerializedName;

public class ShowLiveCosInfo {
    @SerializedName("bucket")
    public String     bucket;
    @SerializedName("region")
    public String     region;
    @SerializedName("filename")
    public String     fileName;
    @SerializedName("preview")
    public String     preview;
    @SerializedName("credential")
    public Credential credential;

    public ShowLiveCosInfo(String bucket, String region, String fileName, String preview) {
        this.bucket = bucket;
        this.region = region;
        this.fileName = fileName;
        this.preview = preview;
    }

    public String getKeyTime() {
        return "" + credential.startTime + ";" + credential.expiredTime + "";
    }


    @Override
    public String toString() {
        return "ShowLiveCosInfo{"
                + "bucket='" + bucket + '\''
                + ", region='" + region + '\''
                + ", fileName='" + fileName + '\''
                + ", preview='" + preview + '\''
                + ", credential=" + credential
                + '}';
    }

    public static class Credential {
        @SerializedName("startTime")
        public int         startTime   = 0;
        @SerializedName("expiredTime")
        public int         expiredTime = 0;
        @SerializedName("expiration")
        public String      expiration  = "";
        @SerializedName("credentials")
        public Credentials credentials;

        @Override
        public String toString() {
            return "Credential{"
                    + "startTime=" + startTime
                    + ", expiredTime=" + expiredTime
                    + ", expiration='" + expiration + '\''
                    + ", credentials=" + credentials
                    + '}';
        }

        public static class Credentials {
            @SerializedName("sessionToken")
            public String sessionToken = "";
            @SerializedName("tmpSecretId")
            public String tmpSecretId  = "";
            @SerializedName("tmpSecretKey")
            public String tmpSecretKey = "";

            @Override
            public String toString() {
                return "Credentials{"
                        + "sessionToken='" + sessionToken + '\''
                        + ", tmpSecretId='" + tmpSecretId + '\''
                        + ", tmpSecretKey='" + tmpSecretKey + '\''
                        + '}';
            }
        }
    }
}
