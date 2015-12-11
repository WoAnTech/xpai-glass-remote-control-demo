package cn.com.xpai4glass;

import java.util.ArrayList;
import java.util.List;

import cn.com.xpai.core.Manager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class GlassDPadController extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 1000;
    private static final int zoomStep = 5;
    private Activity activity;
	private int currentZoomLevel = 0;

    public GlassDPadController(Activity a) {
    	activity = a;
    }
    
    @Override
    public boolean onScroll(MotionEvent start, MotionEvent finish, float distanceX, float distanceY) {
        if(finish.getX() > start.getX()) {
            Log.d("Event", "On Scroll Forward");
    		if (currentZoomLevel+zoomStep <= Manager.getMaxZoomLevel()) {
    			Manager.setZoom(currentZoomLevel+=zoomStep, true);
    		}
        }
        else {
            Log.d("Event", "On Scroll Backward");
    		if (currentZoomLevel-zoomStep >= 0) {
    			Manager.setZoom(currentZoomLevel-=zoomStep, true);
    		}
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
        try {
            float totalXTraveled = finish.getX() - start.getX();
            float totalYTraveled = finish.getY() - start.getY();
            if (Math.abs(totalXTraveled) > Math.abs(totalYTraveled)) {
                if (Math.abs(totalXTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (totalXTraveled > 10) {
                        Log.d("Event", "On Fling Forward");
                    } else {
                        Log.d("Event", "On Fling Backward");
                    }
                }
            } else {
                if (Math.abs(totalYTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if(totalYTraveled > 0) {
                        Log.d("Event", "On Fling Down");
                        Manager.stopPreview();
                        Manager.stopRecord();
                        activity.finish();
                    } else {
                        Log.d("Event", "On Fling Up");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void switchGlareScene() {
    	boolean glare_scene_toggle = Manager.isGlareScene();
		Manager.setGlareScene(!glare_scene_toggle);
		glare_scene_toggle = Manager.isGlareScene();
		Toast.makeText(activity.getBaseContext(), "强光调节: " + (glare_scene_toggle?"启用":"关闭"), Toast.LENGTH_SHORT)
		.show();
    }
    
    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }
    
    @SuppressLint("NewApi")
	public void setMeteringAreas() {
		Rect rect = new Rect();
		rect.left = -400;
		rect.top = -400;
		rect.right = 400;
		rect.bottom = 400;
		Camera.Area area = new Camera.Area(rect, 1000);
		List<Camera.Area> area_list = new ArrayList<Camera.Area>();
		area_list.add(area);
		Manager.setMeteringAreas(area_list);
		Manager.setFocusAreas(area_list);
	}

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d("Event", "On Double Tap");
        switchGlareScene();
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("Event", "On Single Tap");
    	setMeteringAreas();
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return super.onDown(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);    //To change body of overridden methods use File | Settings | File Templates.
    }
}