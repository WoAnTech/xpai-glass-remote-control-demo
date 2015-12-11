package cn.com.xpai4glass;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import cn.com.xpai4glass.R;
import cn.com.xpai.core.Manager;


public class RecordIcon extends ImageView {

	public RecordIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				return;
				/*
				if (Manager.RecordStatus.IDLE != Manager.getRecordStatus()) {
					Manager.stopRecord();
					setBackgroundResource(R.drawable.record);
				} else {
					//Manager.startPreview();
					if (!Manager.startRecord(Config.recordMode, Config.videoBitRate * 1024,
							true, false)) {
						//@Todo 启动录制失败，显示错误信息
					}
				}
				XPAndroid.mainHandler.sendEmptyMessage(MainHandler.MSG_SWITCH_BTN_VISIBILITY);
				*/
			}
		});
	}
	
	boolean flipFlag;
	long lastFlip;
	
	public void update() {
		if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
			long now = System.currentTimeMillis();
			//让按钮用两个图片切换产闪烁
			if (now - lastFlip > 400) {
				lastFlip = now;
				flipFlag = !flipFlag;
				if (flipFlag) {
					this.setBackgroundResource(R.drawable.record_active);
				} else {
					this.setBackgroundResource(R.drawable.record);
				}
			}
			
		} else if (flipFlag) {
			flipFlag = false;
		}
	}
}