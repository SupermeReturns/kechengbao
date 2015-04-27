package com.sdl.kechengbao;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity implements OnClickListener{
    String serverUrl = "";  // 服务器地址
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
                    case MyThread.LOGIN:
                        // 如果返回“SUCCESS”信息，便提示用户，然后请求用户开始登陆
                        Toast.makeText(getApplicationContext(), "注册成功，请登陆：）",
                                Toast.LENGTH_SHORT).show();
                        passwordTxt.setText("");
                        break;
                    case MyThread.REGISTER:
                        // 收到LOGIN的回应，跳转到下一个课表的Acityty
                        Bundle mBundle = new Bundle();
                        mBundle.putString("UserID", UserID);
                        mBundle.putString("Password", Password);
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
                // 组装JSON Auth
                JSONObject param = new JSONObject();
                switch (this.actionID) {
                    case LOGIN:
                        param.put("Action", "LOGIN");
                        break;
                    case REGISTER:
                        param.put("Action", "REGISTER");
                        break;
                    default:
                        System.out.println("Unknown View");
                        return;
                }
                param.put("UserID", name);
                param.put("Password", pass);

                // 发送请求，并等待回应
                HttpPost request = new HttpPost(serverUrl);
                StringEntity se = new StringEntity(param.toString());
                request.setEntity(se);
                HttpResponse httpResponse = new DefaultHttpClient().execute(request);

                // 解析回应，判断是否返回正确的HTTP状态码
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常，发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());
                    handler.obtainMessage(this.actionID, retStr).sendToTarget();
                } else {
                    // 如果返回的状态码不是200，代表发生错误,发送错误通知给UI
                    handler.obtainMessage(0).sendToTarget();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
