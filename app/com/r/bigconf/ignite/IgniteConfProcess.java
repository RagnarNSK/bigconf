package com.r.bigconf.ignite;

import com.r.bigconf.model.Conference;
import com.r.bigconf.processing.ConfProcess;
import org.apache.ignite.lang.IgniteRunnable;

public class IgniteConfProcess extends ConfProcess implements IgniteRunnable {
    public IgniteConfProcess(Conference conference) {
        super(conference);
    }
}
