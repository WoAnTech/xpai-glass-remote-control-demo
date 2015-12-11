package cn.com.xpai4glass;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import cn.com.xpai4glass.R;
import cn.com.xpai.core.Manager;

class MainHandler extends Handler {
	TextView txtDuration = null;
	TextView txtFps = null;
	TextView txtNetSpeed = null;
	TextView txtCache = null;
	TextView txtSdkVer = null;
	TextView txtConnection = null;
	PopListView settingMenu = null;
	Activity activity = null;
	public final static int MSG_UPDATE_INFO = 11000;
	public final static int MSG_SWITCH_BTN_VISIBILITY = 11001;
	public final static int MSG_NETWORK_CONNECTED = 11002;
	public final static int MSG_NETWORK_DISCONNECT = 11003;
	
	private final static String TAG = "MainHandler";

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_UPDATE_INFO:
			updateRunInfo();
			break;
		case MSG_NETWORK_CONNECTED:
			break;
		case MSG_NETWORK_DISCONNECT:
			break;
		default:
			super.handleMessage(msg);
		}
	}
	

	final private void updateRunInfo() {
		if (Manager.isConnected()) {
			txtConnection.setText(String.format("%s %s",
					activity.getApplication().getText(R.string.info_connection),
					activity.getApplication().getText(R.string.connect_ok)
					));
			txtConnection.setTextColor(activity.getApplication().getResources().getColor(R.color.brightGreen));
		} else {
			txtConnection.setText(String.format("%s %s",
					activity.getApplication().getText(R.string.info_connection),
					activity.getApplication().getText(R.string.connect_lost)
					));
			txtConnection.setTextColor(activity.getApplication().getResources().getColor(R.color.brightRed));
		}
		if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
			txtDuration.setVisibility(View.VISIBLE);
			txtFps.setVisibility(View.VISIBLE);
			txtDuration.setText(String.format("%s %.2f",
					activity.getApplication().getText(R.string.info_duration),
					(float) Manager.getRecordDuration() / 1000));
			txtFps.setText(String.format("%s %d fps", 
					activity.getApplication().getText(R.string.info_fps),
					Manager.getCurrentFPS()));
		} 
		txtNetSpeed.setText(String.format("%s %.2f KBps",
				activity.getApplication().getText(R.string.info_net_speed),
				(float) Manager.getUploadingSpeed() / 1024));
		txtCache.setText(String.format("%s %.2f KByte",
				activity.getApplication().getText(R.string.info_cache),
				(float) Manager.getCacheRemaining() / 1024));
	}

	private Timer mTimer = new Timer();// 定时器
	
	public MainHandler(Activity activity) {
		txtDuration = (TextView) activity.findViewById(R.id.txt_record_duration);
		txtFps = (TextView) activity.findViewById(R.id.txt_frame_rate);
		txtNetSpeed = (TextView) activity.findViewById(R.id.txt_net_speed);
		txtCache = (TextView) activity.findViewById(R.id.txt_cache_remain);
		txtSdkVer = (TextView) activity.findViewById(R.id.txt_sdk_ver);
		txtConnection = (TextView) activity.findViewById(R.id.txt_connection);
		
		this.activity = activity;
		
		txtSdkVer.setText(activity.getApplication().getText(R.string.info_sdk_ver).toString()
				+ Manager.getSdkVersion());
		settingMenu = null;
		
		// 创建定时线程执行更新任务
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendEmptyMessage(MSG_UPDATE_INFO);// 向Handler发送消息
			}
		}, 100, 500);// 定时任务
	}
	

}