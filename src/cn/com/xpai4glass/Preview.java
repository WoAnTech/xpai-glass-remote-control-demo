package cn.com.xpai4glass;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import cn.com.xpai.core.Manager;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";
	private SurfaceHolder mHolder;
	private int screenWidth;
	private int screenHeight;

	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Manager.setPreviewSurface(this);
//		Manager.initNet(Config.mvHost, Integer.parseInt(Config.mvPort), 
//				60000, Config.serviceCode, 0);
		Manager.connectCloud(Config.getVSUrl, 60000, Config.serviceCode, 0);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.w(TAG, "Destroying surface!");
		Manager.stopRecord();
		Manager.stopPreview();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i(TAG, "Surface Changed!");
		Manager.setPreviewSize(w,h);
		if (Manager.isPreviewing()) {
			Manager.stopPreview();
			Manager.startPreview();
		}
	}
}