package com.sdl.kechengbao;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActivityAsk extends ActionBarActivity implements View.OnClickListener {
    String serverUrl = "";  // 服务器地址
    String userId, password; // 用户名和密码
    String courseID; // 课程代号

    private Button askBtn;
    private EditText editText;

    // 用户处理服务器返回的信息。根据服务器返回的内容，执行特定动作（主要是调整用户UI）
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    // 代表服务交互出现问题,使用toast提示用户
                    Toast.makeText(getApplicationContext(), "网络故障，请检查连接~",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // 代表服务交互出现问题,使用toast提示用户
                    Toast.makeText(getApplicationContext(), "提问成功！~",
                            Toast.LENGTH_SHORT).show();

                    // 把结果返回上一级Activity
                    Bundle data = new Bundle();
                    // TO DO
                    Intent intent = new Intent();
                    intent.putExtras(data);
                    setResult(Activity.RESULT_OK, intent);
                    ActivityAsk.this.finish();

                    break;
                default:
                    return;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_ask);

        /////////////////////////////////////////////
        // 从ShowCourse的Bundle中获取用户名,密码,服务器地址，课程ID--孙栋梁添加
        Bundle mBundle = this.getIntent().getExtras();
        userId = mBundle.getString("UserID");
        password = mBundle.getString("Password");
        serverUrl = mBundle.getString("ServerUrl");
        courseID = mBundle.getString("CourseID");
        /////////////////////////////////////////////

        editText = (EditText)findViewById(R.id.editText);
        askBtn = (Button)findViewById(R.id.button);
        askBtn.setOnClickListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_ask, menu);
        return true;
    }
    /**
     * 用户点击按钮的回调函数
     * @param v 被点击的控件
     */
    public void onClick(View v) {
        // 用户点击了askBtn按钮提问，转到转到单独的线程与服务器进行交互
        if(this.editText.getText().toString().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "内容不能为空，请重新输入",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Thread thread = new Thread(new MyThread());
        thread.start();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.home)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        System.out.println("按下了back键   onBackPressed()");
    }

    /**
     * <code>MyThread</code> 类型在单独线程中用于与服务器进行通信
     */
    class MyThread implements Runnable{
        public void run() {
            try {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                list.add(new BasicNameValuePair("Action", "ASK"));
                list.add(new BasicNameValuePair("UserID", ActivityAsk.this.userId));
                list.add(new BasicNameValuePair("Password", ActivityAsk.this.password));
                list.add(new BasicNameValuePair("CourseID", ActivityAsk.this.courseID));
                list.add(new BasicNameValuePair("Body", ActivityAsk.this.editText.getText().toString()));

                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list,"UTF-8");  ;


                // 发送请求，并等待回应
                HttpPost request = new HttpPost(ActivityAsk.this.serverUrl);
                request.setEntity(entity);

                // 设置延时
                HttpParams my_httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(my_httpParams, 500);
                HttpConnectionParams.setSoTimeout(my_httpParams, 500);
                HttpResponse httpResponse = new DefaultHttpClient(my_httpParams).execute(request);

                // 解析回应，判断是否返回正确的HTTP状态码
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常，发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());
                    handler.obtainMessage(1, retStr).sendToTarget();
                } else {
                    // 如果返回的状态码不是200，代表发生错误,发送错误通知给UI
                    handler.obtainMessage(0).sendToTarget();
                }
            } catch (ConnectTimeoutException| SocketException | SocketTimeoutException e) {
                // handle time out, the server might be down
                handler.obtainMessage(0).sendToTarget();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
