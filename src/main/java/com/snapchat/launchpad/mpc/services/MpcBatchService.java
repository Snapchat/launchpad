package com.snapchat.launchpad.mpc.services;


import com.snapchat.launchpad.mpc.schemas.MpcJobDefinition;
import java.io.IOException;

public abstract class MpcBatchService {
    static final String STORAGE_PATH = "/data";

    public abstract String submitBatchJob(MpcJobDefinition jobDef) throws IOException;
}
