package com.sdl.kechengbao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity implements OnClickListener{
    String serverUrl = "http://172.18.35.97:8080/api";  // 服务器地址
    String UserID, Password; // 用户名和密码
    Button loginBt, registerBt;
    EditText passwordTxt, nameTxt;

    // 根据服务器返回的内容，执行特定动作（主要是调整用户UI）
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case 0:
                        // 代表服务交互出现问题,使用toast提示用户
                        Toast.makeText(getApplicationContext(), "网络故障，请检查连接~",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MyThread.REGISTER:
                        // 如果返回“SUCCESS”信息，便提示用户，然后请求用户开始登陆
                        Toast.makeText(getApplicationContext(), "注册成功，请登陆：）",
                                Toast.LENGTH_SHORT).show();
                        passwordTxt.setText("");
                        break;
                    case MyThread.LOGIN:
                        // 跳转到下一个课表的Acityty
                        Bundle mBundle = new Bundle();
                        mBundle.putString("UserID", UserID);
                        mBundle.putString("Password", Password);
                        mBundle.putString("ServerUrl", serverUrl);
                        Intent mIntent = new Intent();
                        mIntent.setClass(LoginActivity.this, ShowCources.class);
                        mIntent.putExtras(mBundle);
                        startActivity(mIntent);
                        break;
                    default:
                        return;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        passwordTxt = (EditText)findViewById(R.id.passwordText);
        nameTxt = (EditText)findViewById(R.id.nameText);
        loginBt = (Button)findViewById(R.id.loginButton);
        registerBt = (Button)findViewById(R.id.registerButton);
        loginBt.setOnClickListener(this);
        registerBt.setOnClickListener(this);

        // 接下来尝试自动登录
        SharedPreferences settings = getSharedPreferences("Global_info", Context.MODE_PRIVATE);
        String lastUserName = settings.getString("lastUserName", null);
        String lastUserPass = settings.getString("lastUserPass", null);
        boolean isLastUserLogin = settings.getBoolean("isLastUserLogin", false);

        if (lastUserName == null)
            return;

        nameTxt.setText(lastUserName);

        if (!isLastUserLogin)
            return;

        Thread thread = new Thread(new MyThread(lastUserName, lastUserPass, MyThread.LOGIN));
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 用户点击按钮的回调函数
     * @param v 被点击的控件
     */
    public void onClick(View v) {
        // 与服务器通信前首先确认用户输入是否符合规范
        Password = passwordTxt.getText().toString();
        UserID = nameTxt.getText().toString();
        if (!this.validateNameAndPass(UserID, Password)) {
            // 提示用户的输入不符合规范
            Toast.makeText(getApplicationContext(), "您的输入不符合规范！",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 尝试在单独的线程中与服务器联系
        int actionID;
        switch (v.getId()) {
            case R.id.loginButton:
                actionID = MyThread.LOGIN;
                break;
            case R.id.registerButton:
                actionID = MyThread.REGISTER;
                break;
            default:
                return;
        }
        Thread thread = new Thread(new MyThread(UserID, Password, actionID));
        thread.start();

        /////// TEST //////////////
        /*
        Bundle mBundle = new Bundle();
        mBundle.putString("UserID", UserID);
        mBundle.putString("Password", Password);
        mBundle.putString("ServerUrl", serverUrl);
        Intent mIntent = new Intent();
        mIntent.setClass(LoginActivity.this, ShowCources.class);
        mIntent.putExtras(mBundle);
        startActivity(mIntent);
        */
    }

    /**
     * 检验用户名和密码是否符合规范（具体规范在以下代码注释中）
     * @param name 用户名
     * @param pass 密码
     * @return 如果符合规范返回true，否则返回false
     */
    private boolean validateNameAndPass(String name, String pass) {
        // 账户名和密码都不能少于5位
        if((name.length() < 5) || (pass.length()< 5)) {
            return false;
        }

        // 帐户名不能以"_"开头
        if (name.startsWith("_")) {
            return false;
        }

        // 帐户名和密码都不能包含空格
        return !(name.contains(" ") || pass.contains(" "));

    }

    /**
     * <code>MyThread</code> 类型在单独线程中用于与服务器进行通信
     */
    class MyThread implements Runnable{
        private String name;
        private String pass;
        private int actionID;

        public static final  int LOGIN = 1;
        public static final int REGISTER = 2;

        /**
         * MyThread的构造函数，用于初始化变量
         * @param name_ 用户名
         * @param pass_ 密码
         * @param id 采用的动作的代号只有两种情况(LOGIN和REGISTER)
         * @return 返回MyThread实例
         */
        public MyThread(String name_, String pass_, int id) {
            this.name = name_;
            this.pass = pass_;
            this.actionID = id;
        }

        public void run() {
            try {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                NameValuePair pair1, pair2, pair3;
                switch (this.actionID) {
                    case LOGIN:
                        pair1 = new BasicNameValuePair("Action", "LOGIN");
                        break;
                    case REGISTER:
                        pair1 = new BasicNameValuePair("Action", "REGISTER");
                        break;
                    default:
                        System.out.println("Unknown View");
                        return;
                }
                pair2 = new BasicNameValuePair("UserID", name);
                pair3 = new BasicNameValuePair("Password", pass);
                list.add(pair1);
                list.add(pair2);
                list.add(pair3);
                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list,"UTF-8");


                // 发送请求，并等待回应
                HttpPost request = new HttpPost(serverUrl);
                request.setEntity(entity);

                // 设置延时
                HttpParams my_httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(my_httpParams, 500);
                HttpConnectionParams.setSoTimeout(my_httpParams, 500);
                HttpResponse httpResponse = new DefaultHttpClient(my_httpParams).execute(request);

                // 解析回应，判断是否返回正确的HTTP状态码
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常
                    // 1.首先生成该用户的偏好记录
                    SharedPreferences settings = getSharedPreferences(UserID + "_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("UserID", name);
                    editor.putString("Password", pass);
                    editor.commit();

                    // 2.在全局设置中标记用户用于下次的自动登录
                    settings = getSharedPreferences("Global_info", Context.MODE_PRIVATE);
                    editor = settings.edit();
                    editor.putString("lastUserName", UserID);
                    editor.putString("lastUserPass", Password);
                    editor.putBoolean("isLastUserLogin", true);
                    editor.commit();

                    // 3.然后发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());
                    handler.obtainMessage(this.actionID, retStr).sendToTarget();
                } else {
                    // 如果返回的状态码不是200，代表请求发生错误,发送错误通知给UI
                    Log.v("MyLog","Wrong: " + httpResponse.getStatusLine().getStatusCode());
                    Log.v("MyLog", EntityUtils.toString(httpResponse.getEntity()));
                    handler.obtainMessage(0).sendToTarget();
                }
            } catch (ConnectException | ConnectTimeoutException | SocketTimeoutException e) {
                // handle time out, the server might be down

                SharedPreferences settings = getSharedPreferences(this.name+"_info", Context.MODE_PRIVATE);
                if ((this.actionID == LOGIN)
                        && (settings.getString("UserID", null) != null)
                            && (settings.getString("Password", null).equals(this.pass))) {
                    // 如果用户尝试登录，且之前有该用户的记录并且密码匹配，则
                    // 1.在全局设置中标记用户用于下次的自动登录
                    settings = getSharedPreferences("Global_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("lastUserName", this.name);
                    editor.putString("lastUserPass", this.pass);
                    editor.putBoolean("isLastUserLogin", true);
                    editor.commit();

                    // 2.把以前的信息传递给handler
                    handler.obtainMessage(this.actionID, "Offline Validation Success!").sendToTarget();
                } else{
                    // 否则传递错误信息
                    handler.obtainMessage(0).sendToTarget();
                }
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
