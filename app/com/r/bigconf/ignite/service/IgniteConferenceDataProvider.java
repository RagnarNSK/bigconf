package com.r.bigconf.ignite.service;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.ignite.process.ConferenceDataProvider;
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
