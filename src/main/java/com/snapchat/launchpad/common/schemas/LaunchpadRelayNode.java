package com.snapchat.launchpad.common.schemas;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snapchat.launchpad.common.utils.Hash;
import java.util.regex.Pattern;
import org.springframework.lang.NonNull;

public class LaunchpadRelayNode {
    private static final Pattern IPV4_REGEX =
            Pattern.compile(
                    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern IPV6_REGEX =
            Pattern.compile(
                    "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");

    private String ipv4Hashed;
    private String ipv6Hashed;

    @JsonProperty("i4h")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getIpv4Hashed() {
        return ipv4Hashed;
    }

    @JsonProperty("i6h")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getIpv6Hashed() {
        return ipv6Hashed;
    }

    public void setIpv4Hashed(String ipv4Hashed) {
        this.ipv4Hashed = ipv4Hashed;
    }

    public void setIpv6Hashed(String ipv6Hashed) {
        this.ipv6Hashed = ipv6Hashed;
    }

    public LaunchpadRelayNode(String ipRaw) {
        final String ipHashed = Hash.sha256(ipRaw);

        if (isIpv4(ipRaw)) {
            this.setIpv4Hashed(ipHashed);
        }

        if (isIpv6(ipRaw)) {
            this.setIpv6Hashed(ipHashed);
        }
    }

    public static boolean isIpv4(@NonNull final String ip) {
        return IPV4_REGEX.matcher(ip).matches();
    }

    public static boolean isIpv6(@NonNull final String ip) {
        return IPV6_REGEX.matcher(ip).matches();
    }
}
