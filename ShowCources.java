package com.example.ooad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ShowCources extends Activity {
	private List<Map<String, String>> mDataList = new ArrayList<Map<String, String>>();
	private ListView lv;
	private Button logout, saoyisao;
	private String userId, password, cource;
	
	//传递消息，解析JSON并填充数据
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				String name = "", c = "", addr = "";
				Map<String, String> mMap;
				mMap = new HashMap<String, String>();
				mMap.put("name", name);
				mMap.put("class", c);
				mMap.put("addr", addr);
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
				JSONObject cources = null;
				List<NameValuePair> pair = new ArrayList<NameValuePair>();
				pair.add(new BasicNameValuePair("UserID", userId));
				pair.add(new BasicNameValuePair("Passwird", password));
				
				try {
					httppost.setEntity(new UrlEncodedFormEntity(pair));
					HttpResponse response = new DefaultHttpClient().execute(httppost);
					str = EntityUtils.toString(response.getEntity());
					cources = new JSONObject(str);
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler.obtainMessage(1, cources).sendToTarget();
			}
		});
		thread.start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_cources);
		
		userId = "";
		password = "";
		cource = "";
		queryData();
//	设置ListView
		lv = (ListView)findViewById(R.id.courses);
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
				mDataList, R.layout.list_courses, new String[]{"name", "class", "addr"}, new int[]{R.id.listNameText, R.id.listClassText, R.id.listAddrText});
		lv.setAdapter(mSimpleAdapter);
		
//	点击ListView进入新的Activity
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Bundle mBundle = new Bundle();
			  mBundle.putString("cource", cource);
				Intent mIntent = new Intent();
		    mIntent.setClass(ShowCources.this, ShowNotice.class);
				mIntent.putExtras(mBundle);
				startActivity(mIntent);	
			}
		});
		
//	长按删除
//		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
//					long id) {
//				// TODO Auto-generated method stub
//				mDataList.remove(position);
//				TextView tv = (TextView) view.findViewById(R.id.name);
//				String str = tv.getText().toString();
//				((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
//				return true;
//			}
//	});
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
