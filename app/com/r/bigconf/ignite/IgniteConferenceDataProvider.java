package com.r.bigconf.ignite;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import org.apache.ignite.Ignite;

import java.util.UUID;

class IgniteConferenceDataProvider implements ConferenceDataProvider {

    @Override
    public Conference getConference(Ignite ignite, UUID conferenceId) {
        //TODO
        return null;
    }

    @Override
    public ConferenceProcessData getConferenceProcessData(Ignite ignite, UUID conferenceId) {
        //TODO
        return null;
    }
}
