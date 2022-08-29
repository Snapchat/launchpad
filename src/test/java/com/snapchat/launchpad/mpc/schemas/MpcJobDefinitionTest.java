package com.snapchat.launchpad.mpc.schemas;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MpcJobDefinitionTest {
    @Test
    public void Converts_to_string() {
        String result = "company_ip: test-ip\nimage: test-image\ncommand: test-command";

        MpcJobDefinition mpcJobDefinition = new MpcJobDefinition();
        mpcJobDefinition.setCompanyIp("test-ip");
        mpcJobDefinition.setImage("test-image");
        mpcJobDefinition.setCommand("test-command");

        Assertions.assertEquals(result, mpcJobDefinition.toString());
    }
}
