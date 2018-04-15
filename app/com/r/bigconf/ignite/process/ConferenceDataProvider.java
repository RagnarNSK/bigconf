package com.r.bigconf.ignite.process;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import org.apache.ignite.Ignite;

import java.util.UUID;

public interface ConferenceDataProvider {

    Conference getConference(Ignite ignite, UUID conferenceId);

    ConferenceProcessData getConferenceProcessData(Ignite ignite, UUID conferenceId);

}
