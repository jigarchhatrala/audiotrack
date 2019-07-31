package com.example.dhara.audiotrack;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class Flesh extends Thread {
    private CameraManager cameraManager;
    private FrequencyDetector frequencyDetector;
    public boolean isRunning = true;
    public static long thresholdFrequency = 14000, thresholdCount = 115, falseDetectionCount = 20, thresholdTimeOut = 30000, gap = 100, maxCount = 5;
    private int[] fleshDelays;
    private int[] flashDuration;
    private FrequencyAck frequencyAck;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Flesh(Context context) {
        frequencyDetector = new FrequencyDetector();
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        fleshDelays = new int[]{1300, 2000, 3000, 2000, 3000, 300, 300, 300, 300, 300, 300, 2000, 3000, 2000, 300, 300, 300, 300, 300, 300, 3000, 2000, 3000};
        flashDuration = new int[]{2000, 1500, 2000, 1500, 2000, 300, 300, 300, 300, 300, 300, 1500, 2000, 1500, 200, 200, 1000, 200, 200, 200, 3000, 2000, 3000};
    }

    public Flesh(Context context, FrequencyAck frequencyAck) {
        this(context);
        this.frequencyAck = frequencyAck;
    }


    public void end() {
        isRunning = false;
        stopFlesh();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        algoritham1();
        executeTrack();
        if (frequencyAck != null && isRunning) {
            frequencyAck.closeTrack();
        }
        isRunning = false;
        frequencyDetector.release();
    }


    public void algoritham1() {
        long frequency;
        int detectedCount = 0, fasleCount = 0, counter = 1, wrongCount = 0;
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime;
        long resetTimer = currentTime + thresholdTimeOut;
        long nextDetectionTime = currentTime + (gap * counter);
        while (isRunning) {
            currentTime = System.currentTimeMillis();
            frequency = frequencyDetector.getFrequency();
            if (nextDetectionTime <= currentTime) {
                counter++;
                nextDetectionTime = startTime + (counter * gap);
                //Log.d("Flesh", "Cureent : " + System.currentTimeMillis());
                //Log.d("Flesh", "Next : " + nextDetectionTime + "");
                if (frequency >= thresholdFrequency) {
                    detectedCount++;
                    if (detectedCount == 1) {
                        resetTimer = currentTime + thresholdTimeOut;
                    }
                    if (detectedCount >= 0 && fasleCount != 0) {
                        Log.d("Flesh", "Wrong Call In High");
                    }
                    fasleCount = 0;
                } else {
                    if (detectedCount >= thresholdCount) {
                        fasleCount++;
                        //Log.d("Flesh", "Next Time : " + nextDetectionTime);
                        //if (fasleCount == 1) {
                        //Log.d("Flesh", "Start : " + System.currentTimeMillis());
                        //}
                        if (fasleCount > falseDetectionCount) {
                            currentTime = System.currentTimeMillis();
                            while (nextDetectionTime > currentTime) {
                                currentTime = System.currentTimeMillis();
                            }
                            //Log.d("Flesh", "Ending : " + System.currentTimeMillis());
                            return;
                        }
                    }
                }
                sendMessageToUi(frequency, detectedCount, fasleCount, 0);
            }
            if (resetTimer < currentTime) {
                detectedCount = 0;
                fasleCount = 0;
                resetTimer = currentTime + thresholdTimeOut;
            }
        }
    }

    public void algoritham2() {
        long frequency, c1 = 0, c2 = 0, c = 0;
        long detectedCount = 0, fasleCount = 0, counter = 1;
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime;
        long nextDetectionTime = startTime + (gap * counter);
        while (isRunning) {
            frequency = frequencyDetector.getFrequency();
            currentTime = System.currentTimeMillis();
            if (nextDetectionTime <= currentTime) {
                counter++;
                nextDetectionTime = startTime + (gap * counter);
                c++;
//                Log.d("Flesh", "C : " + c);
//                Log.d("Flesh", "C1 : " + c1);
//                Log.d("Flesh", "C2 : " + c2);
                if (frequency >= thresholdFrequency) {
                    c1++;
                } else {
                    c2++;
                }
                if (c == maxCount) {
                    c = 0;
                    if (c1 > c2) {
                        Log.d("Flesh", "True : " + detectedCount);
                        detectedCount += maxCount;

                    } else {
                        fasleCount += maxCount;
                        Log.d("Flesh", "False : " + fasleCount);
                    }
                    c1 = 0;
                    c2 = 0;
                }
                sendMessageToUi(frequency, detectedCount, fasleCount, 0);
                if (detectedCount >= thresholdCount) {
                    if (fasleCount >= falseDetectionCount) {
                        return;
                    }
                } else {
                    fasleCount = 0;
                }
            }
        }
    }

    public void jamer(long miliSecond) {
        long jam = System.currentTimeMillis() + miliSecond;
        while (true) {
            long time = System.currentTimeMillis();
            if (time > jam) {
                return;
            }
        }
    }

    void sendMessageToUi(long frequency, long detectedCount, long falseCount, long timer) {
        if (frequencyAck != null) {
            frequencyAck.frequencyData(frequency, detectedCount, falseCount, timer);
        }
    }

    private void executeTrack() {
        long startTime = 0;
        long endTime = 0;
        if (isRunning) {
            for (int i = 0; i < fleshDelays.length && isRunning; i++) {
                startTime = System.currentTimeMillis();
                startFlesh();
                endTime = System.currentTimeMillis();
                try {
                    Thread.sleep(flashDuration[i] - (endTime - startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startTime = System.currentTimeMillis();
                stopFlesh();
                endTime = System.currentTimeMillis();
                try {
                    Thread.sleep(fleshDelays[i] - (endTime - startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startFlesh() {
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void stopFlesh() {
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    interface FrequencyAck {
        void frequencyData(long frequency, long detectedCount, long falseCount, long timer);

        void closeTrack();
    }
}
