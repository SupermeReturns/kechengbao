package com.sdl.kechengbao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FAQActivity extends Activity implements OnClickListener {
    String serverUrl = "";  // 服务器地址
    String userId, password; // 用户名和密码
    String courseID; // 课程代号

    private Button askBtn;
    private ListView lv;
    private List<Map<String, String>> mData = new ArrayList<Map<String, String>>(); // 存储的FAQ数据，用于ListView进行展示
    private MyAdatper adapter;

    // 用户处理服务器返回的信息。根据服务器返回的内容，执行特定动作（主要是调整用户UI）
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
                    case 1:
                        // 代表获取到问题列表，开始显示出来
                        // 清空以前的数据
                        mData.clear();

                        // 开始获取新的数据
                        JSONArray jsonObjs = new JSONArray((String) msg.obj);
                        for (int i = 0; i < jsonObjs.length(); i++) {
                            JSONObject jsonObj = (JSONObject) jsonObjs.opt(i);
                            JSONObject askObj = jsonObj.getJSONObject("Ask");
                            String questionNo = jsonObj.getString("QuestionNo");
                            String questionBody = askObj.getString("Text");
                            String questionStamp = "Asked by " + askObj.getString("UserID") + " on " + askObj.getString("Date").substring(5, 16);
                            JSONArray answerArray;
                            if (jsonObj.has("Anwser")) {
                                answerArray = jsonObj.getJSONArray("Anwser");
                            } else {
                                answerArray = new JSONArray();
                            }

                            String answerBody = "";
                            for (int j = 0; j < answerArray.length(); j++) {
                                JSONObject ansObj = (JSONObject) answerArray.opt(j);
                                answerBody += ansObj.getString("UserID") + ": " + ansObj.getString("Text")  + "\n";
                            }

                            if (answerBody.isEmpty()) {
                                answerBody = "No one answered yet!";
                            }

                            Map<String, String> mMap;
                            mMap = new HashMap<String, String>();
                            mMap.put("questionNo", questionNo);
                            mMap.put("questionBody", questionBody);
                            mMap.put("questionStamp", questionStamp);
                            mMap.put("answerBody", answerBody);
                            mData.add(mMap);
                        }
                        Log.v("MyLog", "After get question, mData:" + mData.toString());

                        // 刷新ListView的显示
                        FAQActivity.this.adapter.notifyDataSetChanged();
                        break;
                    case 2:
                        FAQActivity.this.adapter.notifyDataSetChanged();
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
        setContentView(R.layout.activity_faq);

        /////////////////////////////////////////////
        // 从ShowCourse的Bundle中获取用户名,密码,服务器地址，课程ID--孙栋梁添加
        Bundle mBundle = this.getIntent().getExtras();
        userId = mBundle.getString("UserID");
        password = mBundle.getString("Password");
        serverUrl = mBundle.getString("ServerUrl");
        courseID = mBundle.getString("CourseID");
        /////////////////////////////////////////////
        Log.v("MyLog", "FAQ onCreate: userId" + userId + " password:" + password + " serverUrl:" + serverUrl);

        askBtn = (Button) findViewById(R.id.askBtn);
        askBtn.setOnClickListener(this);
        lv = (ListView) findViewById(R.id.questionsListView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Bundle mBundle = FAQActivity.this.getIntent().getExtras();
                mBundle.putString("questionNo", mData.get(position).get("questionNo"));
                mBundle.putString("questionBody", mData.get(position).get("questionBody"));
                mBundle.putString("questionStamp", mData.get(position).get("questionStamp"));
                mBundle.putString("answerBody", mData.get(position).get("answerBody"));
                Intent mIntent = new Intent();
                //运行时使用
                mIntent.setClass(FAQActivity.this, ActivityAnswer.class);
                mIntent.putExtras(mBundle);
                startActivityForResult(mIntent, 0);
            }
        });
        adapter = new MyAdatper(FAQActivity.this);
        lv.setAdapter(adapter);
        this.getQuestion();
    }

    /**
     * 向服务器获取FAQ信息
     */
    private void getQuestion() {
        // 尝试在单独的线程中与服务器联系
        Thread thread = new Thread(new MyThread());
        thread.start();
    }

    /**
     * 用户点击按钮的回调函数
     *
     * @param v 被点击的控件
     */
    public void onClick(View v) {
        // 用户点击了askBtn按钮提问，转到ActivityAsk
        Intent mIntent = new Intent();
        mIntent.setClass(FAQActivity.this, ActivityAsk.class);
        mIntent.putExtras(FAQActivity.this.getIntent().getExtras());
        startActivityForResult(mIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 0)
        {
            // ActivityAnswer的返回信息
            switch (resultCode)
            {
                case RESULT_OK:
                {
                    // 用户回答问题成功,尝试刷新ListView
                    Thread thread = new Thread(new MyThread(MyThread.UPDATE_AFTER_ANSWER, data));
                    thread.start();
                }
                case RESULT_CANCELED:
                {
                    // 用户回答问题失败
                    return;
                }
            }
        }
        else if (requestCode == 1)
        {
            // ActionAsk的返回信息
            switch (resultCode)
            {
                case RESULT_OK:
                {
                    // 用户回答问题成功,尝试刷新ListView
                    Thread thread = new Thread(new MyThread(MyThread.UPDATE_AFTER_ASK));
                    thread.start();
                }
                case RESULT_CANCELED:
                {
                    // 用户回答问题失败
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_faq, menu);
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
     * <code>ViewHolder</code> 用于保存每个ListItem中的控件
     */
    public final class ViewHolder {
        public TextView questionNo;
        public TextView questionBody;
        public TextView questionStamp;
        public TextView answerBody;
    }

    /**
     * <code>MyAdatper</code> 用于适配mData数据与ListView控件
     */
    public class MyAdatper extends BaseAdapter {
        private LayoutInflater mInflater;

        public MyAdatper(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                // TEST //
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.list_questions, null);
                holder.questionNo = (TextView) convertView.findViewById(R.id.questionNoText);
                holder.questionBody = (TextView) convertView.findViewById(R.id.questionBodyText);
                holder.questionStamp = (TextView) convertView.findViewById(R.id.questionStampText);
                holder.answerBody = (TextView) convertView.findViewById(R.id.answerBodyText);
                convertView.setTag(holder);
            } else {
                // TEST //
                holder = (ViewHolder) convertView.getTag();
            }
            holder.questionNo.setText(mData.get(position).get("questionNo"));
            holder.questionBody.setText(mData.get(position).get("questionBody"));
            holder.questionStamp.setText(mData.get(position).get("questionStamp"));
            String ans;
            if (mData.get(position).get("answerBody").length() < 25)
                ans = mData.get(position).get("answerBody");
            else
                ans = mData.get(position).get("answerBody").substring(0, 25) + "...";
            holder.answerBody.setText(ans);

            return convertView;
        }
    }

    /**
     *  <code>MyThread</code>  实现了Runnable接口的类，用于在单独的线程与服务器进行通信
     */
    class MyThread implements  Runnable {
        public static final int UPDATE_FIST_TIME = 1;
        public static final int UPDATE_AFTER_ASK = 2;
        public static final int UPDATE_AFTER_ANSWER = 3;
        private int actionID = -1;
        private Intent data = null;

        public MyThread(int id_, Intent data_)
        {
            actionID = id_;
            data = data_;
        }

        public MyThread(int id_)
        {
            actionID = id_;
        }

        public  MyThread()
        {
            actionID = UPDATE_FIST_TIME;
            data = null;
        }

        public void run() {
            try {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                list.add(new BasicNameValuePair("Action", "LISTQ"));
                list.add(new BasicNameValuePair("UserID", FAQActivity.this.userId));
                list.add(new BasicNameValuePair("Password", FAQActivity.this.password));
                list.add(new BasicNameValuePair("CourseID", FAQActivity.this.courseID));

                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list,"UTF-8");  ;

                Log.v("MyLog", "pairs:"+list.toString());

                // 发送请求，并等待回应
                HttpPost request = new HttpPost(FAQActivity.this.serverUrl);
                request.setEntity(entity);

                // 设置延时
                HttpParams my_httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(my_httpParams, 500);
                HttpConnectionParams.setSoTimeout(my_httpParams, 500);

                HttpResponse httpResponse = new DefaultHttpClient(my_httpParams).execute(request);

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常，发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());

                    // 1.首先生成该用户的偏好记录
                    SharedPreferences settings = getSharedPreferences(FAQActivity.this.userId + "_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(FAQActivity.this.courseID + "_FAQ", retStr);
                    editor.commit();

                    // TEST //
                    Log.v("MyLog", retStr);
                    //////////

                    handler.obtainMessage(1, retStr).sendToTarget();
                } else {
                    Log.v("MyLog", "Bad Things happen!");
                    // 如果返回的状态码不是200，代表发生错误,发送错误通知给UI
                    handler.obtainMessage(0).sendToTarget();
                }
            } catch (SocketException | ConnectTimeoutException | SocketTimeoutException e) {
                // handle time out, the server might be down
                SharedPreferences settings = getSharedPreferences(FAQActivity.this.userId+"_info", Context.MODE_PRIVATE);
                String faqs =  settings.getString(FAQActivity.this.courseID + "_FAQ", null);

                if (actionID == UPDATE_FIST_TIME)
                {
                    if ( faqs != null) {
                        Log.v("MyLog", "1");
                        handler.obtainMessage(1, faqs).sendToTarget();
                    } else{
                        Log.v("MyLog", "2");
                        handler.obtainMessage(0).sendToTarget();
                    }
                } else if(actionID==UPDATE_AFTER_ASK){
                    handler.obtainMessage(0).sendToTarget();
                }
                else if (actionID == UPDATE_AFTER_ANSWER)
                {
                    try {
                            Bundle bundle = data.getExtras();
                            String questionNo = bundle.getString("QuestionNo");
                            String answerArrayStr = bundle.getString("AnswerArray");
                            JSONArray newAnswerArray = new JSONArray(answerArrayStr);

                            // 更新mData,然后刷新ListView
                            for (int i = 0; i < mData.size(); i++) {
                                if (mData.get(i).get("questionNo").equals(questionNo)) {
                                    String oldAnswers = mData.get(i).get("answerBody");
                                    if (oldAnswers.equals("No one answered yet!"))
                                        oldAnswers = "";
                                    for (int j = 0; j < newAnswerArray.length(); j++) {
                                        JSONObject ansObj = (JSONObject) newAnswerArray.opt(j);
                                        oldAnswers += ansObj.getString("UserID") + ": " + ansObj.getString("Text") + "\n";
                                    }
                                    mData.get(i).put("answerBody", oldAnswers);
                                    break;
                                }
                            }
                            handler.obtainMessage(2).sendToTarget();

                            // 更新保存的SharedPreferences
                            JSONArray jsonObjs = new JSONArray(faqs);
                            for (int i = 0; i < jsonObjs.length(); i++) {
                                JSONObject jsonObj = (JSONObject) jsonObjs.opt(i);
                                String qsn = jsonObj.getString("QuestionNo");
                                if (qsn.equals(questionNo)) {
                                    JSONArray oldAnswerArray;
                                    if (jsonObj.has("Anwser")) {
                                        oldAnswerArray = jsonObj.getJSONArray("Anwser");
                                    } else {
                                        oldAnswerArray = new JSONArray();
                                    }
                                    for (int j = 0; j < newAnswerArray.length(); j++) {
                                        oldAnswerArray.put(newAnswerArray.opt(j));
                                    }
                                    jsonObj.put("Answer", oldAnswerArray);
                                    jsonObjs.put(i, jsonObj);

                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString(FAQActivity.this.courseID + "_FAQ", jsonObjs.toString());
                                    editor.commit();
                                    break;
                                }
                            }

                    } catch (JSONException ex){
                        throw new RuntimeException(ex);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}