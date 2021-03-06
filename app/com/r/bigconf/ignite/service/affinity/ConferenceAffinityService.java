package com.r.bigconf.ignite.service.affinity;

import com.google.common.collect.Sets;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.ConferenceUserInstantData;
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
    private final ConferenceProcessDataAffinityService conferenceProcessDataAffinityService;

    public ConferenceAffinityService(Ignite ignite) {
        this.ish = new IgniteAffinityServiceHelper<>(CACHE_NAME, ignite, UUID.class, Conference.class);
        this.conferenceProcessDataAffinityService = new ConferenceProcessDataAffinityService(ignite);
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
        ConferenceUserInstantData userInstantData = new ConferenceUserInstantData(conference.getCreatedBy(), true, false);
        processDataService.saveUserForConference(conference.getId(), new ConferenceUsers(Sets.newHashSet(userInstantData)));
        Ignition.localIgnite().compute().runAsync(new IgniteConferenceProcess(conference.getId()));
        log.info("Conference {} started",conference.getId());
        return conference;
    }

    public void delete(UUID conferenceId) {
        ish.getCache().remove(conferenceId);
        conferenceProcessDataAffinityService.onConferenceDelete(conferenceId);
    }
}
