package com.r.bigconf.ignite;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.concurrent.TimeUnit;

@Slf4j
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
            log.trace("Calculated delay time: {}", timeToSleep);
            if(timeToSleep > 0) {
                ignite.scheduler().runLocal(this, timeToSleep, TimeUnit.MILLISECONDS);
            } else {
                ignite.scheduler().runLocal(this);
            }
            log.trace("next execution scheduled");
        }
    }
}
