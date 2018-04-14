package com.r.bigconf.ignite;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import org.apache.ignite.lang.IgniteRunnable;

public class IgniteConferenceProcess extends BaseConferenceProcess implements IgniteRunnable {

    public IgniteConferenceProcess(Conference conference) {
        super(conference);
    }

    @Override
    public void run() {

    }
}
