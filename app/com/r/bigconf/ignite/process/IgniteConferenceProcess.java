package com.r.bigconf.ignite.process;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
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
    private final ConferenceDataProvider conferenceDataProvider;

    public IgniteConferenceProcess(UUID conferenceId, ConferenceDataProvider conferenceDataProvider) {
        this.conferenceId = conferenceId;
        this.conferenceDataProvider = conferenceDataProvider;
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        Conference conference = conferenceDataProvider.getConference(ignite, conferenceId);
        if (conference.isActive()) {
            ConferenceProcessData processData = conferenceDataProvider.getConferenceProcessData(ignite, conferenceId);
            processInterval(processData);
            //TODO store updated data
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
