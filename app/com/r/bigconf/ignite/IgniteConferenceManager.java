package com.r.bigconf.ignite;

import com.r.bigconf.core.manager.BaseConferenceManager;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import org.apache.ignite.Ignite;

public class IgniteConferenceManager extends BaseConferenceManager  {

    private final Ignite ignite;

    public IgniteConferenceManager(Ignite ignite) {
        this.ignite = ignite;
    }

    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        IgniteConferenceProcess confProcess = new IgniteConferenceProcess(conference);
        getActiveProcesses().add(confProcess);
        ignite.compute().run(confProcess);
        return conference;
    }
}
