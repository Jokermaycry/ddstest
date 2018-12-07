package com.aispeech.dui.dds.demo.wangxi;

public interface QinRecorder {

    /**
     * 听者加入事件
     * @param listener 听者
     */
    void listened(QinListener listener);

    /**
     * 听者退出事件
     * @param listener 听者
     */
    void away(QinListener listener);

    /**
     * 录音机开启事件
     */
    void start();

    /**
     * 录音机关闭事件
     */
    void end();

    /**
     * 录音机是否正在收音
     * @return 运行状态
     */
    boolean status();

}
