package com.r.bigconf.ignite;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.concurrent.TimeUnit;

public class IgniteConferenceProcess extends BaseConferenceProcess implements IgniteRunnable {

    @IgniteInstanceResource
    private Ignite ignite;
    private long currentTime;

    public IgniteConferenceProcess(Conference conference) {
        super(conference);
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        if (conference.isActive()) {
            processInterval();
            currentTime += conference.getRecordInterval();
            long timeToSleep = Math.max(currentTime - System.currentTimeMillis(), 0);
            ignite.scheduler().runLocal(this, timeToSleep, TimeUnit.MILLISECONDS);
        }
    }
}
