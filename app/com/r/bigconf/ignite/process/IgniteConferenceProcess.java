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
import org.apache.ignite.transactions.Transaction;

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
        ConferenceAffinityService service = new ConferenceAffinityService(ignite);
        Conference conference = service.getConference(conferenceId);
        if (conference != null) {
            if (conference.isActive()) {
                try (Transaction transaction = ignite.transactions().txStart()){
                    IgniteConferenceProcessData processData = new IgniteConferenceProcessData(ignite, conferenceId);
                    processInterval(processData);
                    processData.saveUsersData();
                    transaction.commit();
                }
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
                service.delete(conferenceId);
                log.info("Conference {} stopped and removed", conferenceId);
            }
        } else {
            log.error("null conference");
        }
    }
}
