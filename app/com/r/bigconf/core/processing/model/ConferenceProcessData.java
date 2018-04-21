package com.r.bigconf.core.processing.model;

import java.nio.ByteBuffer;
import java.util.Map;

public interface ConferenceProcessData {

    Map<String, ByteBuffer> getUsersIncomingData();

    ConferenceChannelsData getChannelsDataObjectToFill();

    void replaceWithNewChannelsData(ConferenceChannelsData builtData);

}
