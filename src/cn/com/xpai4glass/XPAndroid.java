package cn.com.xpai4glass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cnnt.player.Player;
import org.cnnt.player.Surface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.xpai4glass.R;
import cn.com.xpai.core.Manager;
import cn.com.xpai.core.Manager.AudioEncoderType;
import cn.com.xpai.core.RecordMode;
import cn.com.xpai.security.utils.SignatureUtils;
import cn.com.xpai4glass.demo.player.FilelistActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class XPAndroid extends Activity  {
	/** Called when the activity is first created. */
	private SurfaceView mPreview = null;
	private static String TAG = "XPAndroid";
	private static XPAndroid instance = null;

	private static Menu menu = null;

	//private GestureDetector mDetector;

	static String lastPictureFileName = null;

	static MainHandler mainHandler;
	private Timer timer;
	static boolean isAuthSuccess = false;
	private Player player;
	private String fileName;
	private Surface surface = null;
	private static final int START_PLAY = 0x30001;
	private boolean isPlaying = false;
	private String lastPlayUrl;
	private SensorManager sensorManager;
	public static float currentLight;
	
	private GestureDetector mGestureDetector;

	static Menu getMenu() {
		return menu;
	}

	public final static int MENU_UPLOAD_PICTURE = 20004;
	public final static int MENU_UPLOAD_VF_WHOLE = 20013;
	public final static int MENU_UPLOAD_VF = 20014;

	public static XPAndroid getInstance() {
		return instance;
	}
	
	private Handler playerHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case Player.MSG_OPEN_OK:
					//Toast.makeText(getApplication(), "读取视频文件 " + fileName + " 成功!", Toast.LENGTH_LONG).show();
					break;
				case Player.MSG_OPEN_ERROR:
					isPlaying = false;
					//Toast.makeText(getApplication(), "读取视频文件 " + fileName + " 失败!", Toast.LENGTH_LONG).show();
					break;
				case Player.MSG_PROGRESS_UPDATE:
					break;
				case Player.MSG_PLAYER_STOPPED:
					isPlaying = false;
					//Toast.makeText(getApplication(), "播放结束", Toast.LENGTH_LONG).show();
					break;
				case Player.MSG_READ_ERROR:
					isPlaying = false;
					//Toast.makeText(getApplication(), "读取数据错误", Toast.LENGTH_LONG).show();
					/*下面代码不是必须的而是为了演示在直播过程中网络发生错误时，自动重连*/
					break;
				case START_PLAY:
					if (player != null) {
						player.onDestroy();
					}
					if (!isPlaying) {
						if(fileName != null && fileName.equals(lastPlayUrl)) {
							Log.i(TAG, "the playUrl is the same!");
							return;
						}
						lastPlayUrl = fileName;
						newPlayer();
					}
					isPlaying = true;
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		instance = this;
		timer = new Timer();
		XPHandler.register(this);
		Config.load(this);
		if (0 != Manager.init(this, XPHandler.getInstance())) {
			Log.e(TAG, "init core manager failed");
		}
		//Manager.setVideoFpsRange(30, 30);
		Manager.setVideoFpsRange(100, 100);
		List<Manager.Resolution> res_list = Manager
				.getSupportedVideoResolutions();
		if (null != res_list && res_list.size() > 0) {
			if (0 == Config.videoWidth || 0 == Config.videoHeight) {
				// 使用第一个可用分辨率作为默认分辨率
				Manager.Resolution res = res_list.get(0);
				//Config.videoWidth = res.width;//默认设置为640*480,实际应用过程中应用应先获取设备支持的分辨率
				//Config.videoHeight = res.height;
			}
		} else {
			Log.e(TAG, "cannto get supported resolutions");
		}
		
		Manager.setVideoResolution(Config.videoWidth, Config.videoHeight);
		Manager.setAudioRecorderParams(Config.audioEncoderType, Config.channel,
				Config.audioSampleRate, Config.audioBitRate);
		/* 当缓冲超过10240字节时，开始降帧，最多每帧间隔300ms，即降到3.3帧 */
		//Manager.setNetWorkingAdaptive(true);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		mPreview = (SurfaceView) findViewById(R.id.preview_view);
		mainHandler = new MainHandler(this);
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (player != null && isPlaying)
					return;
				if (isAuthSuccess) {
					getTaskList();
				}
			}
		}, 1*1000, 1000*1);
		
		//mDetector = new GestureDetector(this, this);
		mGestureDetector = new GestureDetector(this, new GlassDPadController(this));
		// Set the gesture detector as the double tap
		// listener.
	}
	
	private void getTaskList () {
		String uri="/api/20140928/task_list";
		if(Config.serviceKey == null || "".equals(Config.serviceKey)) {
			return;
		}
		String result = aws_by_getMethod(uri, Config.serviceCode, Config.serviceKey, null);
		if (result == null) {
			return;
		}
		try {
			JSONObject jobj = new JSONObject(result);
			int ret = jobj.getInt("ret");
			if (ret == 0) {
				String opaque = "";
				String httpLiveUrl = "";
				JSONObject opaqueJObj = null;
				JSONArray taskListArray = jobj.getJSONArray("task_list");
				String fName = "";
				for (int i=0;i<taskListArray.length();i++) {
					JSONObject taskObj = taskListArray.getJSONObject(i);
					JSONArray outputsArray = taskObj.getJSONArray("outputs");
					for (int j=0;j<outputsArray.length();j++) {
						JSONObject outputJObj = outputsArray.getJSONObject(j);
						String format = outputJObj.getString("format");
						if ("flv".equals(format)) {
							fName = outputJObj.getString("file_name");
							break;
						}
					}
					opaque = URLDecoder.decode(taskObj.getString("opaque"), "UTF-8");
					try {
						opaqueJObj = new JSONObject(opaque);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
					String userName = opaqueJObj.getString("player_user");
					if (Config.userName.equals(userName)) {
						httpLiveUrl = taskObj.getString("http_live_url");
						fileName = httpLiveUrl + fName;
						playerHandler.sendEmptyMessage(START_PLAY);
					}
				}
			} else {
				Log.e(TAG, "Error msg:" + jobj.getString("msg"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "get task_list parse json error!");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void newPlayer() {
		Log.v(TAG, "play url: " + fileName);
		player = new Player(getApplication(), playerHandler, fileName, new String[]{"-live"});
		surface = new Surface(getApplication(), player);
	
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.gravity = Gravity.CENTER;
		surface.setLayoutParams(params);
		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.framecontainer);
		frameContainer.addView(surface);
	}
	
	private String aws_by_getMethod(String uri, String code,
			String secret_key, String queryString) {
		String xvs_signature = null;
		BufferedReader reader = null;
		String result = null;
		try {
			String timeStamp = SignatureUtils.getTimeStamp("c.zhiboyun.com", Config.serviceCode);
			String url_str = "http://c.zhiboyun.com" + uri + "?"
					+ "service_code=" + code;
			xvs_signature = SignatureUtils.getSignature(secret_key, uri, code,
					queryString, timeStamp);
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setHeader("xvs-timestamp", timeStamp);
			request.setHeader("xvs-signature", xvs_signature);
			request.setURI(new URI(url_str));
			HttpResponse response = client.execute(request);
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));

			StringBuffer strBuffer = new StringBuffer("");
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuffer.append(line);
			}
			result = strBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "get task_list error");
		}

		return result;
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		menu = m;
		menu.add(0, MENU_UPLOAD_PICTURE, 0, "上传照片");
		menu.add(0, MENU_UPLOAD_VF_WHOLE, 0, "上传离线录制的文件");
		menu.add(0, MENU_UPLOAD_VF, 0, "续传视频文件");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu m) {
		super.onPrepareOptionsMenu(m);
		if (Manager.RecordStatus.IDLE != Manager.getRecordStatus()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF_WHOLE).setEnabled(false);
		} else {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(true);
			m.findItem(MENU_UPLOAD_VF).setEnabled(true);
			m.findItem(MENU_UPLOAD_VF_WHOLE).setEnabled(true);
		}
		if (Manager.isConnected()
				&& Manager.RecordStatus.IDLE != Manager.getRecordStatus()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
		}

		if (!Manager.isConnected()) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
			m.findItem(MENU_UPLOAD_VF).setEnabled(false);
		}

		if (lastPictureFileName == null) {
			m.findItem(MENU_UPLOAD_PICTURE).setEnabled(false);
		}

		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_UPLOAD_PICTURE:
			if (null != lastPictureFileName) {
				Manager.uploadFile(lastPictureFileName);
				Log.v(TAG, "upload file name:" + lastPictureFileName);
			} else {
				Message msg = new Message();
				msg.what = XPHandler.SHOW_MESSAGE;
				Bundle bdl = new Bundle();
				bdl.putString(XPHandler.MSG_CONTENT, "未找到最近拍摄的照片!");
			}
			return true;
		case MENU_UPLOAD_VF_WHOLE:
			Intent intent = new Intent(this, FileChooser.class);
			startActivityForResult(intent, 0);
			return true;
		case MENU_UPLOAD_VF:
			intent = new Intent(this, FileChooser.class);
			startActivityForResult(intent, 1);
			return true;
		}
		return false;
	}

	protected void onDestroy() {
		Log.i(TAG, "mini app destroy");
		XPHandler.getInstance().exitApp();
		Manager.deInit();
		super.onDestroy();
		System.exit(0);
	}

	/* 覆盖 onActivityResult() */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		switch (resultCode) {
		case RESULT_OK:
			/* 取得来自Activity2的数据，并显示于画面上 */
			Bundle b = i.getExtras();
			String file_name = b.getString("file_name");
			Log.i(TAG, "Get file name:" + file_name);
			// Manager.uploadVideoFile(..., false)
			// 第二个参数为 false代表新上传一个文件, 服务器总是将上传的数据存为一个新的视频文件
			// 第二个参数为 true 代表续传
			if (!Manager.uploadVideoFile(file_name, requestCode == 1, null)) {
				// todo 错误处理
				Log.w(TAG, "Upload file failed.");
			}
			break;
		default:
			break;
		}
	}
	
	private int currentExposureCompensation = 0;
	@SuppressLint("NewApi")

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
			Log.i(TAG, "key down");
			//setMeteringAreas();
			return true;
		}
		return super.onKeyDown(keycode, event);
	}

	@Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
	
}
