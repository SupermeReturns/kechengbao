package com.example.ooad;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	private ArrayList<View> views = new ArrayList<View>();
	private List<Map<String, String>>[] mDataList = new ArrayList[5];
	private Map<String, String> mMap;
	private Button logout, saoyisao;
	private String userId, password, course;
	private SharedPreferences sp;
	
	public void saveSp (String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public JSONObject readSp (String key) {
		try {
			return new JSONObject(sp.getString(key, ""));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	//解析课程中的JSON
	public void handleCources (JSONObject j) {
		try {
			String name = j.getString("Name");
			String CourseID = j.getString("CourseID");
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
				mMap = new HashMap<String, String>();
				mMap.put("name", name);
				mMap.put("class", startClass+"~"+endClass);
				mMap.put("addr", "Addr");
				mMap.put("CourseID", CourseID);
				mDataList[Integer.parseInt(day)-1].add(mMap);
				Log.e("mData", mDataList[4].get(0).toString());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//传递消息，解析JSON并填充数据
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				setListView((List<Map<String, String>>[]) msg.obj);
			}
		}
	};
	
	//设置ListView
	public void setListView(final List<Map<String, String>>[] nDataList) {
		View[] view = new View[5];
		int[] cours = new int[]{R.id.cources1, R.id.cources2, R.id.cources3, R.id.cources4, R.id.cources5};
		int[] layouts = new int[]{R.layout.layout1, R.layout.layout2, R.layout.layout3, R.layout.layout4, R.layout.layout5};
		ListView[] lv = new ListView[5];
		for (int i = 0; i < 5; i++) {
//		设置ListView
			try {
				//Field f;
				//f = R.drawable.class.getField(cours[i]);
				//int id = f.getInt(R.drawable.class);
				//int id = getResources().getIdentifier("list"+cours[i], "id", "com.example.ooad");
				view[i] = LayoutInflater.from(this).inflate(layouts[i], null);
				lv[i] = (ListView)view[i].findViewById(cours[i]);
				//Log.e("nData", nDataList[i].get(0).toString());
				SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
						nDataList[i], R.layout.list_courses, new String[]{"name", "class", "addr"}, new int[]{R.id.listNameText, R.id.listClassText, R.id.listAddrText});
				lv[i].setAdapter(mSimpleAdapter);
				final List<Map<String, String>> temp = nDataList[i];
				//setContentView(view[0]);
//			点击ListView进入新的Activity
				lv[i].setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
						// TODO Auto-generated method stub
						Bundle mBundle = new Bundle();
					  mBundle.putString("CourseID", temp.get(position).get("CourseID"));
					  mBundle.putString("UserID", userId);
					  mBundle.putString("Password", password);
						Intent mIntent = new Intent();
						//运行时使用
				    //mIntent.setClass(ShowCources.this, FAQActivity.class);
						mIntent.putExtras(mBundle);
						startActivity(mIntent);	
					}
				});
				views.add(view[i]);
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
		}
		initViewPager();
	}			
		
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
					HttpResponse response = new DefaultHttpClient().execute(httppost);
					str = EntityUtils.toString(response.getEntity());
					//str = "{\"UserID\":\"12330285\",\"Courses\":[\"00000001\"],\"Id\":{\"WorkID\":\"12330285\",\"CardID\":\"370682xxxx\",\"Email\":\"kassian@123.com\",\"Phone\":\"118012\"},\"Education\":{\"University\":\"\",\"School\":\"\",\"Major\":\"\",\"Level\":\"\",\"StartYear\":\"\",\"EndYear\":\"\"},\"Info\":{\"NickName\":\"\",\"Name\":\"\",\"Education\":[]}}";
					Log.e("返回值", str);
					
					//联网成功获得指定信息
					courses = new JSONObject(str);					
					saveSp("courses", str);
				}	catch (Exception e) {
					e.printStackTrace();
					courses = readSp("courses");
				}
			//解析JSON得到Courses id
				JSONArray jsonArray;
				try {
					jsonArray = courses.getJSONArray("Courses");
				  //对每个课程进行查询
					for (int i = 0; i < jsonArray.length(); i++) {
						Object courseid = jsonArray.get(i);
	          Log.e("obj", (String)courseid);
	          pair = new ArrayList<NameValuePair>();
	  				pair.add(new BasicNameValuePair("Action", "DETAIL"));
	  				pair.add(new BasicNameValuePair("UserID", userId));
	  				pair.add(new BasicNameValuePair("Password", password));
	  				pair.add(new BasicNameValuePair("CourseID", (String)courseid));
	  				try {
							httppost.setEntity(new UrlEncodedFormEntity(pair));
							HttpResponse response = new DefaultHttpClient().execute(httppost);
							str = EntityUtils.toString(response.getEntity());
						  //str = "{\"CourseID\": \"00000001\",\"Name\": \"Computer Graphics\",\"Code\": \"SE-314\",\"Term\": \"2015S\",\"Hour\": {\"StartWeek \": 1,\"EntWeek\": 18,\"ClassHours\": [{\"Day\": 5,\"StartClass\": 3,\"EndClass\": 5}]},\"Teachers\": [\"203124231\"],\"TAs\": [\"11330001\",\"11330002\"],\"Students\": [\"12330285\",\"12330284\",\"dongliangshishabi\"],\"Chapters\": [{\"No\": 0,\"Title\": \"Intro\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our intro lesson\"},{\"No\": 1,\"Title\": \"OpenGL API\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our first lesson\"}]}";
							course = new JSONObject(str);
		  				saveSp((String)courseid, str);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
							course = readSp((String)courseid);
						}
	  				//传递出去，进行UI更新
	  				handleCources(course);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				handler.obtainMessage(1, mDataList).sendToTarget();
			}
		});
		thread.start();
	}
	
	//初始化切换界面
	private void initViewPager(){
	   ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
	   MYViewPagerAdapter adapter = new MYViewPagerAdapter();
	   adapter.setViews(views);
	   viewPager.setAdapter(adapter);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_cources);
		
		sp = this.getSharedPreferences("Courses", Context.MODE_PRIVATE);
		userId = "12330285";
		password = "12330";
		course = "";
		//初始化mDatalist
		for (int i = 0; i < 5; i++) {
			mDataList[i] = new ArrayList<Map<String, String>>();
		}
		
		//查询课程信息
		queryData();
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
