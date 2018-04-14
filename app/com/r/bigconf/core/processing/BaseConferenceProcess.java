package com.r.bigconf.core.processing;

import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.sound.wav.WavUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BaseConferenceProcess {
    protected final Conference conference;
    public boolean minimizeTotalChannel = false;
    private volatile ConferenceChannelsData active = new ConferenceChannelsData();
    private ConferenceChannelsData building = new ConferenceChannelsData();
    private Map<Integer, ByteBuffer> incoming = new HashMap<>();

    public BaseConferenceProcess(Conference conference) {
        this.conference = conference;
    }

    public Conference getConference() {
        return conference;
    }

    public void processInterval() {
        Map<Integer, ByteBuffer> incomingBackup = new HashMap<>(incoming);
        incoming.clear();
        buildBuffers(incomingBackup);
        ConferenceChannelsData activeBackup = active;
        active = building;
        building = activeBackup;
        building.audioChannels.clear();
        building.commonChannel = null;
    }

    public void addIncoming(Integer userId, ByteBuffer byteBuffer, Filter filter) {
        incoming.put(userId, filter != null ? filter.filter(byteBuffer) : byteBuffer);
    }

    public ByteBuffer getForUser(int userId) {
        if (active != null) {
            ByteBuffer special = active.audioChannels.get(userId);
            return special != null ? special : active.commonChannel;
        } else {
            return null;
        }
    }

    private void buildBuffers(Map<Integer, ByteBuffer> incomingBackup) {
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
        building.commonChannel = ByteBuffer.allocate(buffersSize);
        for (Integer id : incomingBackup.keySet()) {
            building.audioChannels.put(id, ByteBuffer.allocate(buffersSize));
        }
        //copy headers
        ByteBuffer value = incomingBackup.entrySet().iterator().next().getValue();
        value.position(0);
        for (int i = 0; i < dataStartIndex; i++) {
            byte headerByte = value.get(i);
            building.commonChannel.put(i, headerByte);
            for (ByteBuffer buffer : building.audioChannels.values()) {
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
            WavUtils.putBytes(building.commonChannel, i, totalHi, totalLo);

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
                WavUtils.putBytes(building.audioChannels.get(entry.getKey()), i, hiPart, loPart);
            }
        }
        //building.compressedCommonChannel = codec.compress(building.commonChannel);
    }

    private boolean checkLength(Map<Integer, Integer> lengthsMap, int i, Map.Entry<Integer, ByteBuffer> entry) {
        return i + 1 < lengthsMap.get(entry.getKey()) + 44;
    }

    private static class ConferenceChannelsData {
        /**
         * Audio channels map
         * key - integer - user id
         * value - wav content of conference WITHOUT source of user identified by key
         */
        private Map<Integer, ByteBuffer> audioChannels = new HashMap<>();
        private ByteBuffer commonChannel;
    }
}
