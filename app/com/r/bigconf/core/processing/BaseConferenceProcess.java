package com.r.bigconf.core.processing;

import com.r.bigconf.core.processing.model.ConferenceChannelsData;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.core.sound.wav.WavUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BaseConferenceProcess {

    public boolean minimizeTotalChannel = false;

    public void processInterval(ConferenceProcessData processData) {
        log.trace("Processing interval for conference started");
        Map<String, ByteBuffer> incoming = processData.getUsersIncomingData();
        ConferenceChannelsData building = processData.getChannelsDataObjectToFill();
        buildBuffers(building, incoming);
        processData.replaceWithNewChannelsData(building);
        log.trace("Processing interval for conference finished");
    }

    private void buildBuffers(ConferenceChannelsData toBuild, Map<String, ByteBuffer> incomingDataMap) {
        if (incomingDataMap.size() == 0) {
            return;
        }
        final Map<String, Integer> lengthsMap = new HashMap<>();
        int maxLength = 0;
        for (Map.Entry<String, ByteBuffer> entry : incomingDataMap.entrySet()) {
            int dataLength = WavUtils.getDataLength(entry.getValue());
            if (dataLength > maxLength) {
                maxLength = dataLength;
            }
            lengthsMap.put(entry.getKey(), dataLength);
        }
        int dataStartIndex = 44;
        int buffersSize = maxLength + dataStartIndex;
        toBuild.setCommonChannel(ByteBuffer.allocate(buffersSize));
        for (String id : incomingDataMap.keySet()) {
            toBuild.getAudioChannels().put(id, ByteBuffer.allocate(buffersSize));
        }
        //copy headers
        ByteBuffer value = incomingDataMap.entrySet().iterator().next().getValue();
        value.position(0);
        for (int i = 0; i < dataStartIndex; i++) {
            byte headerByte = value.get(i);
            toBuild.getCommonChannel().put(i, headerByte);
            for (ByteBuffer buffer : toBuild.getAudioChannels().values()) {
                buffer.put(i, headerByte);
            }
        }
        for (int i = dataStartIndex; i < buffersSize; i = i + 2) {
            int total = 0;
            for (Map.Entry<String, ByteBuffer> entry : incomingDataMap.entrySet()) {
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i + 1);
                    byte lo = entry.getValue().get(i);
                    total = total + WavUtils.getaShort(hi, lo);
                }
            }
            int totalMinimized = minimizeTotalChannel ? total / incomingDataMap.size() : total;
            byte totalHi = WavUtils.getHiPart(totalMinimized);
            byte totalLo = WavUtils.getLoPart(totalMinimized);
            WavUtils.putBytes(toBuild.getCommonChannel(), i, totalHi, totalLo);

            for (Map.Entry<String, ByteBuffer> entry : incomingDataMap.entrySet()) {
                final byte hiPart;
                final byte loPart;
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i + 1);
                    byte lo = entry.getValue().get(i);
                    int specific = total - WavUtils.getaShort(hi, lo);
                    int specificMinimized = minimizeTotalChannel ? specific / incomingDataMap.size() - 1 : specific;
                    hiPart = WavUtils.getHiPart(specificMinimized);
                    loPart = WavUtils.getLoPart(specificMinimized);
                } else {
                    hiPart = totalHi;
                    loPart = totalLo;
                }
                WavUtils.putBytes(toBuild.getAudioChannels().get(entry.getKey()), i, hiPart, loPart);
            }
        }
        //building.compressedCommonChannel = codec.compress(building.commonChannel);
    }

    private boolean checkLength(Map<String, Integer> lengthsMap, int i, Map.Entry<String, ByteBuffer> entry) {
        return i + 1 < lengthsMap.get(entry.getKey()) + 44;
    }

}
