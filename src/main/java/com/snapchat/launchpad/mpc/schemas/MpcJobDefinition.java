package com.snapchat.launchpad.mpc.schemas;


import com.fasterxml.jackson.annotation.JsonProperty;

public class MpcJobDefinition {
    @JsonProperty("company_ip")
    private String companyIp;

    @JsonProperty("image")
    private String image;

    @JsonProperty("command")
    private String command;

    public String getCompanyIp() {
        return companyIp;
    }

    public void setCompanyIp(String companyIp) {
        this.companyIp = companyIp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return String.format("company_ip: %s\nimage: %s\ncommand: %s", companyIp, image, command);
    }
}
