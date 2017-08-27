package com.r.bigconf.processing;

import com.r.bigconf.model.Conference;
import com.r.bigconf.sound.wav.WavUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ConfProcess implements Runnable {

    private static final int DUMMY_FILTER_THRESHOLD = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfProcess.class);
    private final Conference conference;
    public boolean isActive;
    public boolean minimizeTotalChannel = false;

    private ConferenceChannelsData active = new ConferenceChannelsData();
    private ConferenceChannelsData building = new ConferenceChannelsData();

    private Map<Integer, ByteBuffer> incoming = new HashMap<>();

    private long currentTime;

    public ConfProcess(Conference conference) {
        this.conference = conference;
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (isActive) {
            processInterval();
            currentTime += conference.getRecordInterval();
            try {
                Thread.sleep(currentTime - System.currentTimeMillis());
            } catch (InterruptedException e) {
                LOGGER.warn("Conf interrupted");
                isActive = false;
            }
        }
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

    public void addIncoming(Integer userId, ByteBuffer byteBuffer) {
        incoming.put(userId, dummyFilter(byteBuffer));
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
                    total = total + getaShort(hi, lo);
                }
            }
            int totalMinimized = minimizeTotalChannel ? total / incomingBackup.size() : total;
            byte totalHi = getHiPart(totalMinimized);
            byte totalLo = getLoPart(totalMinimized);
            putBytes(building.commonChannel, i, totalHi, totalLo);

            for (Map.Entry<Integer, ByteBuffer> entry : incomingBackup.entrySet()) {
                final byte hiPart;
                final byte loPart;
                if (checkLength(lengthsMap, i, entry)) {
                    byte hi = entry.getValue().get(i + 1);
                    byte lo = entry.getValue().get(i);
                    int specific = total - getaShort(hi, lo);
                    int specificMinimized = minimizeTotalChannel ? specific / incomingBackup.size() - 1 : specific;
                    hiPart = getHiPart(specificMinimized);
                    loPart = getLoPart(specificMinimized);
                } else {
                    hiPart = totalHi;
                    loPart = totalLo;
                }
                putBytes(building.audioChannels.get(entry.getKey()), i, hiPart, loPart);
            }
        }
    }

    private void putBytes(ByteBuffer byteBuffer, int i, byte hiPart, byte loPart) {
        byteBuffer.put(i + 1, hiPart);
        byteBuffer.put(i, loPart);
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


    public ByteBuffer dummyFilter(ByteBuffer byteBuffer) {
        int length = byteBuffer.limit();
        ByteBuffer ret = ByteBuffer.allocate(length);
        for(int i = 0; i< length; i=i+2){
            if(i > 44){
                byte lo = byteBuffer.get(i);
                byte hi = byteBuffer.get(i+1);
                short value = getaShort(hi, lo);
                if(value < DUMMY_FILTER_THRESHOLD && value > (DUMMY_FILTER_THRESHOLD * -1)){
                    value = 0;
                }
                ret.put(i, getLoPart(value));
                ret.put(i+1, getHiPart(value));
            } else {
                ret.put(i,byteBuffer.get(i));
                ret.put(i+1,byteBuffer.get(i+1));
            }
        }
        return ret;
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
