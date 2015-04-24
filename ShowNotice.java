package com.example.ooad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ShowNotice extends Activity {
	private List<Map<String, String>> mDataList = new ArrayList<Map<String, String>>();
	private ListView lv;
	private String cource;
	
	//传递消息，解析JSON并填充数据
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				String notice = "";
				Map<String, String> mMap;
				mMap = new HashMap<String, String>();
				mMap.put("notice", notice);
				mDataList.add(mMap);
			}
		}
	};
	//向服务器请求信息
	private void queryData() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String url = "http:/server/api";
				String str = "";
				HttpPost httppost = new HttpPost(url);
				JSONObject notices = null;
				List<NameValuePair> pair = new ArrayList<NameValuePair>();
				pair.add(new BasicNameValuePair("", cource));
				
				try {
					httppost.setEntity(new UrlEncodedFormEntity(pair));
					HttpResponse response = new DefaultHttpClient().execute(httppost);
					str = EntityUtils.toString(response.getEntity());
					notices = new JSONObject(str);
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler.obtainMessage(1, notices).sendToTarget();
			}
		});
		thread.start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_notices);
		
		Bundle mBundle = this.getIntent().getExtras();
		cource = mBundle.getString("cource");
		queryData();
//	设置ListView
		lv = (ListView)findViewById(R.id.notices);
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
				mDataList, R.layout.list_notices, new String[]{"notices"}, new int[]{R.id.listNotice});
		lv.setAdapter(mSimpleAdapter);
		
//	长按删除
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				mDataList.remove(position);
				((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
				return true;
			}
	});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_cources, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
