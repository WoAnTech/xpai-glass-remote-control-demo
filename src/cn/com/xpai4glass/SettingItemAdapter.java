package cn.com.xpai4glass;

import cn.com.xpai4glass.R;
import cn.com.xpai.core.RecordMode;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SettingItemAdapter extends BaseAdapter {

	private Context context;
	private PopListView resolutionMenu;
	private PopListView recordTypeMenu;
	private PopSeekbar popNetworkTimeout;
	private PopSeekbar popBitrate;
	private Activity activity;
	final private int MENU_RESOLUTION_IDX = 0;
	final private int MENU_BITRATE_IDX = 1;
	final private int MENU_NET_TIMEOUT_IDX = 2;
	final private int MENU_RECORD_TYPE_IDX = 3;
	
	SettingItemAdapter(Activity activity) {
		context = activity.getBaseContext();
		this.activity = activity;
		resolutionMenu = new PopListView(activity, new ResolutionListAdapter(activity, this), "设置分辨率");
		recordTypeMenu = new PopListView(activity, new RecordTypeAdapter(activity, this), "设置录制类型");
		popNetworkTimeout = new PopSeekbar(activity, 10, 100, Config.netTimeout,
				new PopSeekbar.onChangeListener() {
					
					@Override
					void onChanged(int value) {
						Config.netTimeout = value;
						Config.save();
						notifyDataSetChanged();
					}
				}, "设置网络超时时间");
		
		popBitrate = new PopSeekbar(activity, 200, 4096, Config.videoBitRate,
				new PopSeekbar.onChangeListener() {
					
					@Override
					void onChanged(int value) {
						Config.videoBitRate = value;
						Config.save();
						notifyDataSetChanged();
					}
				}, "设置视频码流");
	}
	
	@Override
	public int getCount() {
		return settingName.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ItemViewCache tag = (ItemViewCache)v.getTag();
			int [] location = new int[4];
			v.getLocationOnScreen(location);
			v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			int pos_x =  location[0] + v.getMeasuredWidth();
			Log.i("location", "x:" + location[0] + " y:" + location[1]);
			switch(tag.position) {
			case MENU_RESOLUTION_IDX:
				//resolutionMenu.showAtLocation(activity.findViewById(R.id.btn_connection),
				//		 Gravity.LEFT, pos_x, 10);
				break;
			case MENU_RECORD_TYPE_IDX:
				//recordTypeMenu.showAtLocation(activity.findViewById(R.id.btn_connection),
				//		 Gravity.LEFT, pos_x, 10);
				break;
			case MENU_NET_TIMEOUT_IDX:
				//popNetworkTimeout.showAtLocation(activity.findViewById(R.id.btn_connection),
				//		 Gravity.LEFT, pos_x, 0);
				break;
			case MENU_BITRATE_IDX:
				///popBitrate.showAtLocation(activity.findViewById(R.id.btn_connection),
				//		 Gravity.LEFT, pos_x, 0);
				break;
			}
		}
	};
	
	@Override
	public View getView(int position, View convert_view, ViewGroup parent) {
        if(convert_view == null) {  
            convert_view = LayoutInflater.from(context).inflate(R.layout.txt_item, null, true);  
            ItemViewCache viewCache = new ItemViewCache();  
            viewCache.txtName = (TextView)convert_view.findViewById(R.id.txt_name);  
            viewCache.txtValue =(TextView)convert_view.findViewById(R.id.txt_value);
            viewCache.position = position;
            convert_view.setTag(viewCache);
            convert_view.setOnClickListener(onClickListener);
        }
        ItemViewCache cache=(ItemViewCache)convert_view.getTag();  
        cache.txtName.setText(settingName[position]);
        switch (position) {
        case MENU_RESOLUTION_IDX:
        	cache.txtValue.setText(String.format("%dx%d", Config.videoWidth, Config.videoHeight));
        	break;
        case MENU_BITRATE_IDX:
        	cache.txtValue.setText(Config.videoBitRate + "Kbit");
        	break;
        case MENU_NET_TIMEOUT_IDX:
        	cache.txtValue.setText(Config.netTimeout + "秒");
        	break;
        case MENU_RECORD_TYPE_IDX:
        	cache.txtValue.setText(Config.recordMode.toString());
        	break;
        }
        return convert_view; 
	}
	
	private static class ItemViewCache {
		int position;
		TextView txtName;
		TextView txtValue;
	}

	private String [] settingName = new String []{"分辨率","码流","网络超时","录制类型"};
}
