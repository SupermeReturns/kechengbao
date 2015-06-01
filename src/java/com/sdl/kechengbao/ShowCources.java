package com.sdl.kechengbao;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sdl.kechengbao.MYViewPagerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
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
import android.view.View.OnClickListener;

public class ShowCources extends ActionBarActivity {
	private ArrayList<View> views = new ArrayList<View>();
	private List<Map<String, String>>[] mDataList = new ArrayList[5];
	private Map<String, String> mMap;
	private Button logout, saoyisao;
	private String userId, password, url;
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
            String chapterToParse = j.getString("Chapters");
            JSONObject location = j.getJSONObject("Room");
            String addr = location.getString("Campus") + " " + location.getString("Building") + " " + location.getString("RoomNo") ;
            for (int i = 0; i < classHours.length(); i++) {
				JSONObject temp = classHours.getJSONObject(i);
				String day = temp.getString("Day");
				String startClass = temp.getString("StartClass");
				String endClass = temp.getString("EndClass");

                Log.v("MyLog","day, start class, end, class");
				Log.v("MyLog", day);
				Log.v("MyLog", startClass);
				Log.v("MyLog", endClass);
				mMap = new HashMap<String, String>();
				mMap.put("name", name);
				mMap.put("class", startClass+"~"+endClass);
				mMap.put("addr", addr);
				mMap.put("CourseID", CourseID);
                mMap.put("ChapterToParse", chapterToParse);
				mDataList[Integer.parseInt(day)-1].add(mMap);
				Log.v("MyLog", mDataList[4].get(0).toString());
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
		int[] cours = new int[]{R.id.courses1, R.id.courses2, R.id.courses3, R.id.courses4, R.id.courses5};
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
					    mBundle.putString("UserID", userId);
					    mBundle.putString("Password", password);
              mBundle.putString("ServerUrl", url);
              mBundle.putString("CourseID", temp.get(position).get("CourseID"));
              mBundle.putString("ChapterToParse", temp.get(position).get("ChapterToParse"));
						Intent mIntent = new Intent();
						//运行时使用
				    mIntent.setClass(ShowCources.this, ActGroup.class);
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

                    // 设置延时
                    HttpParams my_httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(my_httpParams, 500);
                    HttpConnectionParams.setSoTimeout(my_httpParams, 500);
    				HttpResponse response = new DefaultHttpClient(my_httpParams).execute(httppost);

					str = EntityUtils.toString(response.getEntity());
//				    str = "{\"UserID\":\"12330285\",\"Courses\":[\"00000001\"],\"Id\":{\"WorkID\":\"12330285\",\"CardID\":\"370682xxxx\",\"Email\":\"kassian@123.com\",\"Phone\":\"118012\"},\"Education\":{\"University\":\"\",\"School\":\"\",\"Major\":\"\",\"Level\":\"\",\"StartYear\":\"\",\"EndYear\":\"\"},\"Info\":{\"NickName\":\"\",\"Name\":\"\",\"Education\":[]}}";
					Log.e("返回值", str);
					
					//联网成功获得指定信息
					courses = new JSONObject(str);
					saveSp("courses", str);
				}	catch (Exception e) {
					e.printStackTrace();
                    if (sp.getString("courses", null) != null)
					    courses = readSp("courses");
                    else
                        return;
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

                                // 设置延时
                                HttpParams my_httpParams = new BasicHttpParams();
                                HttpConnectionParams.setConnectionTimeout(my_httpParams, 500);
                                HttpConnectionParams.setSoTimeout(my_httpParams, 500);
							    HttpResponse response = new DefaultHttpClient(my_httpParams).execute(httppost);

							    str = EntityUtils.toString(response.getEntity());
//						      str = "{\"CourseID\": \"00000001\",\"Name\": \"Computer Graphics\",\"Code\": \"SE-314\",\"Term\": \"2015S\",\"Hour\": {\"StartWeek \": 1,\"EntWeek\": 18,\"ClassHours\": [{\"Day\": 5,\"StartClass\": 3,\"EndClass\": 5}]},\"Teachers\": [\"203124231\"],\"TAs\": [\"11330001\",\"11330002\"],\"Students\": [\"12330285\",\"12330284\",\"dongliangshishabi\"],\"Chapters\": [{\"No\": 0,\"Title\": \"Intro\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our intro lesson\"},{\"No\": 1,\"Title\": \"OpenGL API\",\"Intro\": \"Intro to xxx\",\"Text\": \"Hello every body this is our first lesson\"}]}";
							    course = new JSONObject(str);
                                Log.e("课程", str);
		  				        saveSp((String)courseid, str);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
                            if (sp.getString((String)courseid, null) != null)
							    course = readSp((String)courseid);
                            else
                                return;
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

       // 判断今天是星期几，然后跳转到相应时间的课程表
       Calendar rightNow=Calendar.getInstance();
       int day=rightNow.get(rightNow.DAY_OF_WEEK);
       if ((day == 1)||(day == 7))
           day = 1;
        Log.v("MyLog", "Today is: " + day);
       viewPager.setCurrentItem(day-2);
	}

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent i= new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
        System.out.println("按下了back键   onBackPressed()");
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_cources);

        Bundle bundle = this.getIntent().getExtras();
		userId = bundle.getString("UserID");
		password = bundle.getString("Password");
        url = bundle.getString("ServerUrl");
        sp = this.getSharedPreferences(this.userId + "_info", Context.MODE_PRIVATE);
        Log.v("MyLog", "ShowCourse onCreate userId:" + userId + " password: " + password + "url: " + url );

        // 激活ActionBar上显示应用图标
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

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
		getMenuInflater().inflate(R.menu.menu_show_cources, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_scan) {
            Intent mIntent = new Intent();
            Bundle mBundle = new Bundle();
            mBundle.putString("UserID", userId);
            mBundle.putString("Password", password);
            mIntent.putExtras(mBundle);
            mIntent.setClass(ShowCources.this, CaptureActivity.class);
            startActivity(mIntent);
			return true;
		} else if (id == R.id.action_quit) {
            // 修改该用户的全局设置，取消自动登录
            SharedPreferences settings = getSharedPreferences("Global_info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isLastUserLogin", false);
            editor.commit();

            // 结束ShowCourse Activity
            this.finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
	}
}
