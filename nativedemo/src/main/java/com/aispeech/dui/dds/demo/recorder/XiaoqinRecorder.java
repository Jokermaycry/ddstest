package com.aispeech.dui.dds.demo.recorder;

/**
 * 小秦语音助手-录音操作-接口
 * @author      王西, wangxi@zhong-qin.com
 * @version     2018.1130
 * @since       1.0
 */
public interface XiaoqinRecorder{

    /**
     * 开始录音
     * 如调用时{@link #isRecoding()}为{@code true},重新开始录制
     */
    public void begin();

    /**
     * 获取录音
     * @return byte[] 从上次录制到现在为止的语音数据
     */
    public byte[] record();

    /**
     * 暂停录音
     * @return byte[] 从上次录制到现在为止的语音数据
     */
    public byte[] doPause();

    /**
     * 继续录音
     * @return byte[] 从上次录制到现在为止的语音数据
     */
    public byte[] doContinue();

    /**
     * 结束录音
     * @return byte[] 从上次录制到现在为止的语音数据
     */
    public byte[] end();

    /**
     * 是否正在录制
     * @return boolean 录制状态
     */
    public boolean isRecoding();

}