package com.ambigu.dao;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class VoiceReceiveDAO extends Thread {
	private AudioTrack track = null;// 录音文件播放对象
	private int frequence = 8000;// 采样率 8000
	private int channelInConfig = AudioFormat.CHANNEL_CONFIGURATION_DEFAULT;// 定义采样通道
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 定义音频编码（16位）
	private int bufferSize = -1;// 播放缓冲大小
	private byte[] data;

	public VoiceReceiveDAO() {

	}

	public void init() {
		// 获取缓冲 大小
		bufferSize = AudioTrack.getMinBufferSize(frequence, channelInConfig, audioEncoding);
		// 实例AudioTrack
		track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelInConfig, audioEncoding, bufferSize,
				AudioTrack.MODE_STREAM);

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			DatagramSocket socket = new DatagramSocket(VoiceSendDAO.PORT);
			while (true) {
				// 数组的创建载什么时候，是否影响数据信息？
				data = new byte[bufferSize];
				DatagramPacket dp = new DatagramPacket(data, data.length);
				socket.receive(dp);
				// 将语音数据写入即可。
				track.write(data, bufferSize, data.length);
				track.play();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
