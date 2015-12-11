package cn.com.xpai4glass;

import cn.com.xpai.core.Manager;
import cn.com.xpai.core.RecordMode;
import cn.com.xpai.core.Manager.AudioEncoderType;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class Config {

	final static String PREFS_NAME = "XPAndroid";

	static String userName = android.os.Build.SERIAL;//用户名，可使用眼镜的序列号，也可使用在云平台注册的帐号
	static String userPass = "123"; //for test, not been encrypt.//密码
	static int timeOut = 5 * 1000;
	static int retryConnectTimes = 3;

	static String mvHost = "192.168.199.101";
	static String mvPort = "9999";
	//通过connectCloud api与直播云建立连接
	static String getVSUrl = "http://c.zhiboyun.com/api/20140928/get_vs";
	static String serviceCode = "";//服务码
	static String serviceKey = "";//密钥
	
	//音频编码类型 默认为设置为AAC
	static AudioEncoderType audioEncoderType = AudioEncoderType.AAC;
	//声道
	static int channel = 1;
	//音频采样率
	static int audioSampleRate = 44100;
	//音频比特率
	static int audioBitRate = 44100;
	static int hwMode = 0;
	static int videoWidth = 640;
	static int videoHeight = 480;
	static int videoBitRate = 560; //bitrate单位kbit

	static int photoWidth = 2048;
	static int photoHeight = 1536;
	
	static int netTimeout = 30; //网络超时时间，0代表不判断， 单位是秒
	
	static SharedPreferences sp;
	
	//决定拍传流中包含音频或视频的组合
	static RecordMode recordMode = RecordMode.HwAudioAndVideo;
	
	static String playUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
	
	static void load(Activity a) {
		SharedPreferences settings = a.getSharedPreferences(PREFS_NAME, 0);
		sp = settings;
		netTimeout = sp.getInt("net_time_out", netTimeout);
		photoWidth = sp.getInt("photo_width", photoWidth);
		photoHeight = sp.getInt("photo_height", photoHeight);	

		videoBitRate = sp.getInt("bit_rate", videoBitRate);
		videoWidth = sp.getInt("video_width", videoWidth);
		videoHeight = sp.getInt("video_height", videoHeight);
		serviceCode = sp.getString("service_code", serviceCode);
		mvPort = sp.getString("mv_port", "9999");
		mvHost = sp.getString("mv_host", mvHost);
		retryConnectTimes = sp.getInt("retry_connect_times", 3);
		timeOut = sp.getInt("time_out", timeOut);
		userName = sp.getString("user_name", userName);
		userPass = sp.getString("user_pass", userPass);
		playUrl = sp.getString("play_url", playUrl);
		int st_value = sp.getInt("stream_type", recordMode.value());
		RecordMode st = RecordMode.cast(st_value);
		if (st != null) {
			recordMode = st;
		} else {
			recordMode = RecordMode.HwAudioAndVideo;
		}
	}
	
	static void save() {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("net_time_out", netTimeout);
		editor.putInt("photo_width", photoWidth);
		editor.putInt("photo_height", photoHeight);	

		editor.putInt("bit_rate", videoBitRate);
		editor.putInt("video_width", videoWidth);
		editor.putInt("video_height", videoHeight);
		editor.putString("service_code", serviceCode);
		editor.putString("mv_port", mvPort);
		editor.putString("mv_host", mvHost);
		editor.putInt("retry_connect_times", retryConnectTimes);
		editor.putInt("time_out", timeOut);
		editor.putString("user_name", userName);
		editor.putString("user_pass", userPass);
		editor.putString("play_url", playUrl);
		editor.putInt("stream_type", recordMode.value());
		editor.commit();
	}
}
