package com.r.bigconf.ignite;

import com.r.bigconf.manager.BaseConferenceManager;
import com.r.bigconf.model.Conference;
import com.r.bigconf.model.User;
import org.apache.ignite.Ignite;

public class IgniteConferenceManager extends BaseConferenceManager  {

    private final Ignite ignite;

    public IgniteConferenceManager(Ignite ignite) {
        this.ignite = ignite;
    }

    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        IgniteConfProcess confProcess = new IgniteConfProcess(conference);
        getActiveProcesses().add(confProcess);
        ignite.compute().run(confProcess);
        return conference;
    }
}
