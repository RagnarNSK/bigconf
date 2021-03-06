package module;

import com.google.inject.AbstractModule;
import com.r.bigconf.core.service.ConferenceService;
import com.r.bigconf.core.service.UserService;
import com.r.bigconf.ignite.service.IgniteConferenceService;
import com.r.bigconf.ignite.IgniteHolder;
import com.r.bigconf.ignite.IgniteHolderImpl;
import com.r.bigconf.ignite.service.IgniteUserService;

public class IgniteModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConferenceService.class).to(IgniteConferenceService.class);
        bind(UserService.class).to(IgniteUserService.class);
        bind(IgniteHolder.class).to(IgniteHolderImpl.class);
    }
}
