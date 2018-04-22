package com.r.bigconf.ignite.service.affinity;

import com.google.common.collect.Sets;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.ignite.process.IgniteConferenceProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import java.util.UUID;

@Slf4j
public class ConferenceAffinityService {
    public static final String CACHE_NAME = "conferencesCache";

    private final IgniteAffinityServiceHelper<UUID,Conference> ish;

    public ConferenceAffinityService(Ignite ignite) {
        this.ish = new IgniteAffinityServiceHelper<>(CACHE_NAME, ignite, UUID.class, Conference.class);
    }

    public Conference getConference(UUID conferenceId) {
        return ish.getCache().get(conferenceId);
    }

    public void saveConference(Conference conference) {
        ish.getCache().put(conference.getId(), conference);
    }

    public Conference start(Conference conference) {
        ConferenceProcessDataAffinityService processDataService = new ConferenceProcessDataAffinityService(ish.getIgnite());
        saveConference(conference);
        processDataService.saveUserForConference(conference.getId(),
                new ConferenceUsers(Sets.newHashSet(conference.getCreatedBy())));
        Ignition.localIgnite().compute().runAsync(new IgniteConferenceProcess(conference.getId()));
        log.info("Conference {} started",conference.getId());
        return conference;
    }
}
