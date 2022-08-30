package com.snapchat.launchpad.mpc.schemas;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MpcJobDefinitionTest {
    @Test
    public void Converts_to_string() {
        String result = "imageTag: test-tag\ncommand: test-command";

        MpcJobDefinition mpcJobDefinition = new MpcJobDefinition();
        mpcJobDefinition.setImageTag("test-tag");
        mpcJobDefinition.setCommand("test-command");

        Assertions.assertEquals(result, mpcJobDefinition.toString());
    }

    @Test
    public void Sets_company_ip() {
        String testIp = "test-ip";

        MpcJobDefinition mpcJobDefinition = new MpcJobDefinition();
        mpcJobDefinition.getDynamicValues().put("COMPANY_IP", testIp);

        Assertions.assertEquals(
                testIp, mpcJobDefinition.getDynamicValues().get("COMPANY_IP").toString());
    }
}
