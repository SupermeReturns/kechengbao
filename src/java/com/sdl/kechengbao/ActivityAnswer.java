package com.sdl.kechengbao;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


public class ActivityAnswer extends Activity implements View.OnClickListener  {
    String serverUrl = "";  // 服务器地址
    String userId, password; // 用户名和密码
    String courseID; // 课程代号
    String questionNo,questionBody, questionStamp, answerBody;

    private Button askBtn;
    private EditText editText;
    private TextView questionNoText, questionBodyText, questionStampText, answerBodyText;

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
                    Toast.makeText(getApplicationContext(), "回答成功！~",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    return;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_answer);

        /////////////////////////////////////////////
        // 从ShowCourse的Bundle中获取用户名,密码,服务器地址，课程ID--孙栋梁添加
        Bundle mBundle = this.getIntent().getExtras();
        userId = mBundle.getString("UserID");
        password = mBundle.getString("Password");
        serverUrl = mBundle.getString("ServerUrl");
        courseID = mBundle.getString("CourseID");
        questionNo = mBundle.getString("questionNo");
        questionBody = mBundle.getString("questionBody");
        questionStamp = mBundle.getString("questionStamp");
        answerBody = mBundle.getString("answerBody");
        /////////////////////////////////////////////

        questionNoText = (TextView)findViewById(R.id.questionNoText);
        questionNoText.setText(questionNo);
        questionBodyText = (TextView)findViewById(R.id.questionBodyText);
        questionBodyText.setText(questionBody);
        questionStampText= (TextView)findViewById(R.id.questionStampText);
        questionStampText.setText(questionStamp);
        answerBodyText= (TextView)findViewById(R.id.answerBodyText);
        answerBodyText.setText(answerBody);

        editText = (EditText)findViewById(R.id.editText2);
        askBtn = (Button)findViewById(R.id.button2);
        askBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_answer, menu);
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
        // 用户点击了askBtn按钮提问，转到转到单独的线程与服务器进行交互
        Thread thread = new Thread(new MyThread());
        thread.start();
    }
    /**
     * <code>MyThread</code> 类型在单独线程中用于与服务器进行通信
     */
    class MyThread implements Runnable{
        public void run() {
            try {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                list.add(new BasicNameValuePair("Action", "ANWSER"));
                list.add(new BasicNameValuePair("UserID", ActivityAnswer.this.userId));
                list.add(new BasicNameValuePair("Password", ActivityAnswer.this.password));
                list.add(new BasicNameValuePair("CourseID", ActivityAnswer.this.courseID));
                list.add(new BasicNameValuePair("QuestionNo", ActivityAnswer.this.questionNo));
                list.add(new BasicNameValuePair("Body", ActivityAnswer.this.editText.getText().toString()));

                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list,"UTF-8");  ;


                // 发送请求，并等待回应
                HttpPost request = new HttpPost(ActivityAnswer.this.serverUrl);
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
                    Log.v("MyLog", "str: "+EntityUtils.toString(httpResponse.getEntity()) + ", id: " + httpResponse.getStatusLine().getStatusCode() );
                    handler.obtainMessage(0).sendToTarget();
                }
            } catch (ConnectException | ConnectTimeoutException | SocketTimeoutException e) {
                // handle time out, the server might be down
                handler.obtainMessage(0).sendToTarget();
            }catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
