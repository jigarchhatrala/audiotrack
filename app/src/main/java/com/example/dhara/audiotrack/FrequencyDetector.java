package com.example.dhara.audiotrack;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class FrequencyDetector {

    private final int SAMPLE_RATE = 44100;
    private short[] audioBuffer = null;
    private AudioRecord record;

    public FrequencyDetector() {

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        audioBuffer = new short[bufferSize / 2];

        record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
    }

    public void release(){
        record.release();
    }


    public long getFrequency() {
        long frequency, startTime, endTime, avg, timeDifference;
        record.startRecording();
        startTime = System.currentTimeMillis();
        int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
        avg = calculate(numberOfShort, audioBuffer);
        endTime = System.currentTimeMillis();
        timeDifference = endTime - startTime;
        if (timeDifference < 1) {
            record.stop();
            return 0;
        }
        frequency = (avg * 1000) / timeDifference;
        return frequency;
    }


    private int calculate(int sampleRate, short[] audioData) {
        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples - 1; p++) {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0)) {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float) numSamples / (float) sampleRate;
        float numCycles = numCrossing / 2f;
        float frequency = numCycles / numSecondsRecorded;
        return (int) frequency;
    }


}
