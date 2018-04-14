package com.r.bigconf.ignite;

import com.r.bigconf.core.manager.BaseConferenceManager;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import org.apache.ignite.Ignite;

import java.util.UUID;

public class IgniteConferenceManager extends BaseConferenceManager  {

    private final Ignite ignite;

    public IgniteConferenceManager(Ignite ignite) {
        this.ignite = ignite;
    }

    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        IgniteConferenceProcess confProcess = new IgniteConferenceProcess(conference);
        ignite.compute().run(confProcess);
        return conference;
    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public Conference getConference(UUID conferenceId) {
        //TODO
        return null;
    }
}
