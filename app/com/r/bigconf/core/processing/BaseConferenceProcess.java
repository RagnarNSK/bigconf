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
        Map<Integer, ByteBuffer> incomingBackup = new HashMap<>(processData.getIncoming());
        processData.getIncoming().clear();
        buildBuffers(processData.getBuilding(), incomingBackup);
        ConferenceChannelsData activeBackup = processData.getActive();
        processData.setActive(processData.getBuilding());
        activeBackup.getAudioChannels().clear();
        activeBackup.setCommonChannel(null);
        processData.setBuilding(activeBackup);
        log.trace("Processing interval for conference finished");
    }

    private void buildBuffers(ConferenceChannelsData building, Map<Integer, ByteBuffer> incomingBackup) {
        if (incomingBackup.size() == 0) {
            return;
        }
        final Map<Integer, Integer> lengthsMap = new HashMap<>();
        int maxLength = 0;
        for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
            int dataLength = WavUtils.getDataLength(entry.getValue());
            if (dataLength > maxLength) {
                maxLength = dataLength;
            }
            lengthsMap.put(entry.getKey(), dataLength);
        }
        int dataStartIndex = 44;
        int buffersSize = maxLength + dataStartIndex;
        building.setCommonChannel(ByteBuffer.allocate(buffersSize));
        for (Integer id : incomingBackup.keySet()) {
            building.getAudioChannels().put(id, ByteBuffer.allocate(buffersSize));
        }
        //copy headers
        ByteBuffer value = incomingBackup.entrySet().iterator().next().getValue();
        value.position(0);
        for (int i = 0; i < dataStartIndex; i++) {
            byte headerByte = value.get(i);
            building.getCommonChannel().put(i, headerByte);
            for (ByteBuffer buffer : building.getAudioChannels().values()) {
                buffer.put(i, headerByte);
            }
        }
        for (int i = dataStartIndex; i < buffersSize; i = i + 2) {
            int total = 0;
            for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i + 1);
                    byte lo = entry.getValue().get(i);
                    total = total + WavUtils.getaShort(hi, lo);
                }
            }
            int totalMinimized = minimizeTotalChannel ? total / incomingBackup.size() : total;
            byte totalHi = WavUtils.getHiPart(totalMinimized);
            byte totalLo = WavUtils.getLoPart(totalMinimized);
            WavUtils.putBytes(building.getCommonChannel(), i, totalHi, totalLo);

            for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
                final byte hiPart;
                final byte loPart;
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i + 1);
                    byte lo = entry.getValue().get(i);
                    int specific = total - WavUtils.getaShort(hi, lo);
                    int specificMinimized = minimizeTotalChannel ? specific / incomingBackup.size() - 1 : specific;
                    hiPart = WavUtils.getHiPart(specificMinimized);
                    loPart = WavUtils.getLoPart(specificMinimized);
                } else {
                    hiPart = totalHi;
                    loPart = totalLo;
                }
                WavUtils.putBytes(building.getAudioChannels().get(entry.getKey()), i, hiPart, loPart);
            }
        }
        //building.compressedCommonChannel = codec.compress(building.commonChannel);
    }

    private boolean checkLength(Map<Integer, Integer> lengthsMap, int i, Map.Entry<Integer, ByteBuffer> entry) {
        return i + 1 < lengthsMap.get(entry.getKey()) + 44;
    }

}
