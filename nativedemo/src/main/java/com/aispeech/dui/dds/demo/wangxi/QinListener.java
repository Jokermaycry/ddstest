package com.aispeech.dui.dds.demo.wangxi;

public interface QinListener {

    /**
     * 聆听事件
     * @param data 语音数据byte数组
     * @param begin 数组开始下标
     * @param end 数组结束下表
     */
    void listen(byte[] data, int begin, int end);

    /**
     * 结束聆听
     * @param data 聆听到的所有语音数据byte数组
     * @param begin 数组开始下标
     * @param end 数组结束下表
     */
    void leave(byte[] data, int begin, int end);

}
