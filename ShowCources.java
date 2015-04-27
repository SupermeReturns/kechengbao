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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ooad.MYViewPagerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ShowCources extends Activity {
	private List<Map<String, String>> mDataList;
	private Button logout, saoyisao;
	private String userId, password, cource;
	
	//传递消息，解析JSON并填充数据
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				JSONObject j = (JSONObject)msg.obj;
				try {
					String name = j.getString("Name");
					JSONObject hour = j.getJSONObject("Hour");
					JSONArray classHours = hour.getJSONArray("ClassHours");
					for (int i = 0; i < classHours.length(); i++) {
						JSONObject temp = classHours.getJSONObject(i);
						String day = temp.getString("Day");
						String startClass = temp.getString("StartClass");
						String endClass = temp.getString("EndClass");
						Log.e("day", day);
						Log.e("startClass", startClass);
						Log.e("endClass", endClass);
						Map<String, String> mMap;
						mMap = new HashMap<String, String>();
						mMap.put("name", name);
						mMap.put("class", startClass+"~"+endClass);
						//mMap.put("addr", addr);
						mDataList = new ArrayList<Map<String, String>>();
						mDataList.add(mMap);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	//设置ListView
	public void setListView() {
		Resources res = getResources();
		String[] cours = {"cources1", "cources2", "cources3", "cources4", "cources5"};
		ListView[] lv = new ListView[5];
		for (int i = 0; i < 5; i++) {
//		设置ListView
			lv[i] = (ListView)findViewById(res.getIdentifier(cours[i], "id", getPackageName()));
			SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
					mDataList, R.layout.list_courses, new String[]{"name", "class", "addr"}, new int[]{R.id.listNameText, R.id.listClassText, R.id.listAddrText});
			lv[i].setAdapter(mSimpleAdapter);
			
//		点击ListView进入新的Activity
			lv[i].setOnItemClickListener(new OnItemClickListener() {
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
		
//		长按删除
//			lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//				public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
//						long id) {
//					// TODO Auto-generated method stub
//					mDataList.remove(position);
//					TextView tv = (TextView) view.findViewById(R.id.name);
//					String str = tv.getText().toString();
//					((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
//					return true;
//				}
//		});
		}
	}
	//向服务器请求信息
	private void queryData() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				String url = "http://172.18.35.52:8080/api";
				String str = "";
				JSONObject courses, course;
				HttpPost httppost = new HttpPost(url);
				List<NameValuePair> pair = new ArrayList<NameValuePair>();
				pair.add(new BasicNameValuePair("Action", "PROFILE"));
				pair.add(new BasicNameValuePair("UserID", userId));
				pair.add(new BasicNameValuePair("Password", password));
				//第一次请求，得到学生的所有课程的ID
				try {
					httppost.setEntity(new UrlEncodedFormEntity(pair));
					//HttpResponse response = new DefaultHttpClient().execute(httppost);
					//str = EntityUtils.toString(response.getEntity());
					str = "{\"UserID\":\"12330285\",\"Courses\":[\"00000001\", \"00000002\"],\"Id\":{\"WorkID\":\"12330285\",\"CardID\":\"370682xxxx\",\"Email\":\"kassian@123.com\",\"Phone\":\"118012\"},\"Education\":{\"University\":\"\",\"School\":\"\",\"Major\":\"\",\"Level\":\"\",\"StartYear\":\"\",\"EndYear\":\"\"},\"Info\":{\"NickName\":\"\",\"Name\":\"\",\"Education\":[]}}";
					Log.e("返回值", str);
					courses = new JSONObject(str);
					//解析JSON得到Courses id
					JSONArray jsonArray = courses.getJSONArray("Courses");
					//对每个课程进行查询
					for (int i = 0; i < jsonArray.length(); i++) {
						Object courseid = jsonArray.get(i);
	          Log.e("obj", (String)courseid);
	          pair = new ArrayList<NameValuePair>();
	  				pair.add(new BasicNameValuePair("Action", "DETAIL"));
	  				pair.add(new BasicNameValuePair("UserID", userId));
	  				pair.add(new BasicNameValuePair("Password", password));
	  				pair.add(new BasicNameValuePair("CourseID", (String)courseid));
	  				httppost.setEntity(new UrlEncodedFormEntity(pair));
						//HttpResponse response = new DefaultHttpClient().execute(httppost);
						//str = EntityUtils.toString(response.getEntity());
	  				str = "{\"CourseID \": \"00000001\",\"Name\": \"Computer Graphics\",\"Code\": \"SE-314\",\"Term\": \"2015S\",\"Hour\": {\"StartWeek \": 1,\"EntWeek\": 18,\"ClassHours\": [{\"Day\": 5,\"StartClass\": 3,\"EndClass\": 5}]},\"Teachers\": [\"203124231\"],\"TAs\": [\"11330001\",\"11330002\"],\"Students\": [\"12330285\",\"12330284\",\"dongliangshishabi\"],\"Chapters\": [{\"No\": 0,\"Title\": \"Intro\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our intro lesson\"},{\"No\": 1,\"Title\": \"OpenGL API\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our first lesson\"}]}";
	  				course = new JSONObject(str);
	  				//传递出去，进行UI更新
	  				handler.obtainMessage(1, course).sendToTarget();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
	
	//初始化切换界面
	private void initViewPager(){
	   ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
	   
	   View view1 = LayoutInflater.from(this).inflate(R.layout.layout1, null);
	   View view2 = LayoutInflater.from(this).inflate(R.layout.layout2, null); 
	   View view3 = LayoutInflater.from(this).inflate(R.layout.layout3, null);
	   View view4 = LayoutInflater.from(this).inflate(R.layout.layout4, null);
	   View view5 = LayoutInflater.from(this).inflate(R.layout.layout5, null);
	   
	   ArrayList<View> views = new ArrayList<View>();
	   views.add(view1);
	   views.add(view2);
	   views.add(view3);
	   views.add(view4);
	   views.add(view5);
	   
	   MYViewPagerAdapter adapter = new MYViewPagerAdapter();
	   adapter.setViews(views);
	   viewPager.setAdapter(adapter);
	  }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_cources);
		
		initViewPager();
		
		userId = "12330285";
		password = "12330";
		cource = "";
		queryData();
		//setListView();
		
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
