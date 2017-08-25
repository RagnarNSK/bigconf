package com.r.bigconf.processing;

import com.r.bigconf.model.Conference;
import com.r.bigconf.sound.wav.WavUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ConfProcess implements Runnable {

    private final Conference conference;
    public boolean isActive;

    private ConferenceChannelsData active = new ConferenceChannelsData();
    private ConferenceChannelsData building = new ConferenceChannelsData();

    private Map<Integer, ByteBuffer> incoming = new HashMap<>();

    public ConfProcess(Conference conference) {
        this.conference = conference;
    }

    @Override
    public void run() {
        while (isActive) {
            if (System.currentTimeMillis() % conference.getRecordInterval() == 0) {
                Map<Integer, ByteBuffer> incomingBackup = new HashMap<>(incoming);
                incoming.clear();
                buildBuffers(incomingBackup);
                ConferenceChannelsData activeBackup = active;
                active = building;
                building = activeBackup;
                building.audioChannels.clear();
                building.commonChannel = null;
            }
        }

    }

    private void buildBuffers(Map<Integer, ByteBuffer> incomingBackup) {
        final Map<Integer, Integer> lengthsMap = new HashMap<>();
        int maxLength = 0;
        building.commonChannel = ByteBuffer.allocate(maxLength);
        for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
            int dataLength = WavUtils.getDataLength(entry.getValue());
            if (dataLength > maxLength) {
                maxLength = dataLength;
            }
            lengthsMap.put(entry.getKey(), dataLength);
            building.audioChannels.put(entry.getKey(), ByteBuffer.allocate(maxLength));
        }
        int dataStartIndex = 44;
        for (int i = dataStartIndex; i < maxLength + dataStartIndex; i = i + 2) {
            int total = 0;
            for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i);
                    byte lo = entry.getValue().get(i + 1);
                    total = total + getaShort(hi, lo);
                }
            }
            int totalMinimized = total / incomingBackup.size();
            byte totalHi = getHiPart(totalMinimized);
            byte totalLo = getLoPart(totalMinimized);
            building.commonChannel.put(i, totalHi);
            building.commonChannel.put(i + 1, totalLo);

            for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
                final byte hiPart;
                final byte loPart;
                if(checkLength(lengthsMap,i,entry)){
                    byte hi = entry.getValue().get(i);
                    byte lo = entry.getValue().get(i + 1);
                    int specific = total - getaShort(hi, lo);
                    int specificMinimized = specific/ incomingBackup.size() - 1;
                    hiPart = getHiPart(specificMinimized);
                    loPart = getLoPart(specificMinimized);
                } else {
                    hiPart = totalHi;
                    loPart= totalLo;
                }
                building.audioChannels.get(entry.getKey()).put(i, hiPart);
                building.audioChannels.get(entry.getKey()).put(i + 1, loPart);
            }
        }
    }

    private boolean checkLength(Map<Integer, Integer> lengthsMap, int i, Map.Entry<Integer, ByteBuffer> entry) {
        return i + 1 < lengthsMap.get(entry.getKey()) + 44;
    }

    private short getaShort(byte hi, byte lo) {
        return (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
    }

    private byte getLoPart(int total) {
        return (byte) (total & 0xFF00);
    }

    private byte getHiPart(int total) {
        return (byte) ((total & 0xFF00) >> 8);
    }

    public ByteBuffer getForUser(int userId) {
        if (active != null) {
            ByteBuffer special = active.audioChannels.get(userId);
            return special != null ? special : active.commonChannel;
        } else {
            return null;
        }
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
