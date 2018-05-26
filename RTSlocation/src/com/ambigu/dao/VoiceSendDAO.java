package com.ambigu.dao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class VoiceSendDAO extends Thread {
	AudioRecord recorder;
	boolean isRecording = false; // true表示正在录音
	int bufferSize = 0;// 最小缓冲区大小
	int sampleRateInHz = 44100;// 采样率
	int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_DEFAULT; // 默认
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 量化位数
	String TAG = "AudioRecord";
	float sampleRate = 44100.0f;
	public static final int PORT = 23333;
	private InetAddress toIp;// 目的ip
	private byte[] data;// 存放每次从麦克获得的数据
	AudioFormat format;

	public VoiceSendDAO(InetAddress toIp) {
		this.toIp = toIp;
	}

	@SuppressLint("NewApi")
	public void init() {
		bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);// 计算最小缓冲区
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat,
				bufferSize);// 创建AudioRecorder对象
		format = recorder.getFormat();
	}
	// AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean
	// signed, boolean bigEndian)

	@Override
	public void run() {
		isRecording = true;
		init();
		try {
			// DataOutputStream dos = new DataOutputStream(new
			// BufferedOutputStream(new FileOutputStream(recordingFile)));
			data = new byte[bufferSize];
			recorder.startRecording();// 开始录音
			while (isRecording) {
				recorder.read(data, 0, bufferSize);
				sendData();
			}
			recorder.stop();// 停止录音
		} catch (Throwable t) {
			Log.e(TAG, "Recording Failed");
		}

	}

	private void sendData() {
		try {
			DatagramPacket dp = new DatagramPacket(data, data.length, toIp, PORT);
			DatagramSocket ds = new DatagramSocket();
			ds.send(dp);
			ds.close();
			Log.e("tag", "send success");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
