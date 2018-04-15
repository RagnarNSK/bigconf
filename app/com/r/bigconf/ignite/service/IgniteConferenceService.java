package com.r.bigconf.ignite.service;

import com.r.bigconf.core.service.BaseConferenceService;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import com.r.bigconf.ignite.process.ConferenceDataProvider;
import com.r.bigconf.ignite.process.IgniteConferenceProcess;
import com.r.bigconf.ignite.IgniteHolder;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.UUID;

public class IgniteConferenceService extends BaseConferenceService {

    private final IgniteHolder igniteHolder;

    private final ConferenceDataProvider provider = new IgniteConferenceDataProvider();

    @Inject
    public IgniteConferenceService(IgniteHolder igniteHolder) {
        this.igniteHolder = igniteHolder;
    }

    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        IgniteConferenceProcess confProcess = new IgniteConferenceProcess(conference.getId(), provider);
        igniteHolder.getIgnite().compute().run(confProcess);
        return conference;
    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public ByteBuffer getForUser(String userId) {
        //TODO
        return null;
    }

    @Override
    public void addIncoming(String userId, ByteBuffer byteBuffer) {
        //TODO
    }

    @Override
    public Conference getConference(UUID conferenceId) {
        //TODO
        return null;
    }

}
