package com.r.bigconf.local;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.processing.BaseConferenceProcess;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SingleThreadConferenceProcess extends BaseConferenceProcess implements Runnable {

    private long currentTime;

    public SingleThreadConferenceProcess(Conference conference) {
        super(conference);
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (conference.isActive()) {
            processInterval();
            currentTime += conference.getRecordInterval();
            try {
                long timeToSleep = currentTime - System.currentTimeMillis();
                if(timeToSleep > 0) {
                    Thread.sleep(timeToSleep);
                }
            } catch (InterruptedException e) {
                log.warn("Conf interrupted");
                conference.setActive(false);
            }
        }
    }


}
