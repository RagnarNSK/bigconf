package com.r.bigconf.ignite.process;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.ignite.service.affinity.ConferenceAffinityService;
import com.r.bigconf.ignite.service.affinity.ConferenceProcessDataAffinityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IgniteConferenceProcess extends BaseConferenceProcess implements IgniteRunnable {

    @IgniteInstanceResource
    private Ignite ignite;
    private long currentTime;
    private final UUID conferenceId;

    public IgniteConferenceProcess(UUID conferenceId) {
        this.conferenceId = conferenceId;
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        log.trace("conference process step started");
        Conference conference = new ConferenceAffinityService(ignite).getConference(conferenceId);
        if (conference != null) {
            if (conference.isActive()) {
                ConferenceProcessData processData = new IgniteConferenceProcessData(ignite, conferenceId);
                processInterval(processData);
                currentTime += conference.getRecordInterval();
                long timeToSleep = Math.max(currentTime - System.currentTimeMillis(), 0);
                log.trace("Calculated delay time: {}", timeToSleep);
                if (timeToSleep > 0) {
                    ignite.scheduler().runLocal(this, timeToSleep, TimeUnit.MILLISECONDS);
                } else {
                    ignite.scheduler().runLocal(this);
                }
                log.trace("next execution scheduled");
            } else {
                log.trace("conference stopped");
            }
        } else {
            log.error("null conference");
        }
    }
}
