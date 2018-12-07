package com.aispeech.dui.dds.demo.wangxi;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class XiaoqinRecorder implements QinRecorder {

    private final static String TAG = "XiaoqinRecorder";
    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式：PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static XiaoqinRecorder instance;

    private AudioRecord audioRecord;
    // 缓冲区大小：缓冲区字节大小
    private int bufferSizeInBytes = 0;
    // 读入缓冲区的总byte数(下标)
    private int index = 0;
    // 录音状态
    private Status status = Status.STATUS_NO_READY;

    private byte[] voiceData;

    // 考虑 List<HashMap<Integer,QinListener>> 形式
    private List<QinListener> listeners;
    private List<Integer> indexs;
    private List<byte[]> bs;

    private XiaoqinRecorder() {
        listeners = new ArrayList<>();
        indexs = new ArrayList<>();
        bs = new ArrayList<>();
        createAudio();
    }

    public static synchronized XiaoqinRecorder getInstance() {
        if (instance == null) {
            instance = new XiaoqinRecorder();
        }
        return instance;
    }

    /**
     * 创建默认的录音对象
     */
    private void createAudio() {
        createAudio(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
    }

    private void createAudio(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        audioRecord.startRecording();
        status = Status.STATUS_READY;
    }

    @Override
    public void listened(QinListener listener) {
        switch (status) {
            case STATUS_NO_READY:
                throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
            case STATUS_READY:
                start();
            case STATUS_KEEP:
                indexs.add(index);
                listeners.add(listener);
                Log.d(TAG, "===keepRecord===" + listeners.size());
                break;
            case STATUS_PAUSE:
                throw new IllegalStateException("未知故障,请检查何时暂停状态~");
            case STATUS_STOP:
                throw new IllegalStateException("未知故障,请检查何时停止状态~");
            default:
                Log.d(TAG, "不在状态");
                break;
        }
    }

    @Override
    public void away(QinListener listener) {
        int i = listeners.indexOf(listener);
        int index = indexs.remove(i);
        listeners.remove(i).leave(
                bs.remove(i),
                0, bufferSizeInBytes - index
        );
        if (listeners.isEmpty()) {
            end();
        }
    }

    @Override
    public void start() {
        if (listeners.isEmpty() && status == Status.STATUS_READY) {
            // audioRecord.startRecording();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    voiceToListener();
                }
            }).start();
            Log.d(TAG, "===startRecord===" + audioRecord.getState());
        }
    }

    @Override
    public void end() {
        if (!status()) {
            throw new IllegalStateException("录音尚未开始");
        } else if (listeners.isEmpty()) {
            // audioRecord.stop();
            status = Status.STATUS_READY;
            Log.d(TAG, "===stopRecord===");
            // release();
        }
    }

    @Override
    public boolean status() {
        return status == Status.STATUS_KEEP;
    }

    /**
     * 调用回调函数
     */
    private void voiceToListener() {
        voiceData = new byte[bufferSizeInBytes];
        //将录音状态设置成正在录音状态
        if (status == Status.STATUS_READY) {
            status = Status.STATUS_KEEP;
        }
        while (status == Status.STATUS_KEEP) {
            index = audioRecord.read(voiceData, 0, bufferSizeInBytes);
            if (!listeners.isEmpty() && !indexs.isEmpty()) {
                for (int i = 0; i < listeners.size(); i++) {
                    QinListener qinListener = listeners.get(i);
                    //byte[] data = Arrays.copyOfRange(voiceData, indexs.get(i), bufferSizeInBytes);
                    byte[] data = voiceData;
                    if (AudioRecord.ERROR_INVALID_OPERATION != index) {
                        qinListener.listen(data, 0, data.length);
                        bs.add(data);
                    }
                }
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        listeners.clear();
        listeners = null;
        indexs.clear();
        indexs = null;
        voiceData = null;
        bs.clear();
        bs = null;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_NO_READY;
        Log.d(TAG, "===release===");
        instance = null;
        System.gc();
    }
}
