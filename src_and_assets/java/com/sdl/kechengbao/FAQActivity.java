package com.sdl.kechengbao;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewAnimator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
import java.util.Objects;


public class FAQActivity extends ActionBarActivity implements OnClickListener{
    String serverUrl = "";  // 服务器地址
    String userId, password; // 用户名和密码
    String courseID; // 课程代号

    private Button askBtn;
    private EditText questionEdit;
    private ListView lv;
    private List<Map<String, String>> mData = new ArrayList<Map<String, String>>(); // 存储的FAQ数据，用于ListView进行展示

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
                    case MyThread.GET_QUESTIONS:
                        // 代表获取到问题列表，开始显示出来
                    {
                        JSONArray jsonObjs = new JSONArray((String)msg.obj);
                        for (int i = 0; i < jsonObjs.length(); i++) {
                            JSONObject jsonObj = (JSONObject)jsonObjs.opt(i);
                            JSONObject askObj = jsonObj.getJSONObject("Ask");
                            String questionNo = jsonObj.getString("QuestionNo");
                            String questionBody = askObj.getString("Text");
                            String questionStamp = "Asked by User "+ askObj.getString("UserID") +" on " + askObj.getString("Date");
                            JSONArray anwserArray;
                            if (jsonObj.has("Anwser")) {
                                anwserArray = jsonObj.getJSONArray("Anwser");
                            } else {
                                anwserArray = new JSONArray();
                            }

                            String anwserBody = "";
                            for (int j = 0; j < anwserArray.length(); j++) {
                                JSONObject ansObj =  (JSONObject)anwserArray.opt(j);
                                anwserBody += "User: " + ansObj.getString("UserID") + ansObj.getString("Text") + "\n" + ansObj.getString("Date") + "\n";
                            }

                            if (anwserBody.isEmpty()) {
                                anwserBody = "No one anwsered yet!";
                            }
                            Map<String, String> mMap;
                            mMap = new HashMap<String, String>();
                            mMap.put("questionNo", questionNo);
                            mMap.put("questionBody", questionBody);
                            mMap.put("questionStamp", questionStamp);
                            mMap.put("anwserBody", anwserBody);
                            mData.add(mMap);
                        }
                    }
                    Log.v("MyLog","After get question, mData:"+mData.toString());
                    MyAdatper adapter = new MyAdatper(FAQActivity.this);
                    lv.setAdapter(adapter);
                    break;
                    case MyThread.ANWSER:
                        // 代表获取到回答问题的回应，使用toast提醒用户
                        Toast.makeText(getApplicationContext(), "你的回答对方已经收到",
                                Toast.LENGTH_SHORT).show();
                        // 然后更新ListView
                        // TO DO
                        break;
                    case MyThread.ASK:
                        // 代表获取到提问的回应，使用toast提醒用户
                        Toast.makeText(getApplicationContext(), "你的提问已经收到",
                                Toast.LENGTH_SHORT).show();
                        // 然后更新ListView
                        // TO DO
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

        /////////////////////////////////////////////
        // 从ShowCourse的Bundle中获取用户名,密码,服务器地址，课程ID--孙栋梁添加
        Bundle mBundle = this.getIntent().getExtras();
        userId = mBundle.getString("UserID");
        password = mBundle.getString("Password");
        serverUrl = mBundle.getString("ServerUrl");
        courseID = mBundle.getString("CourseID");
        /////////////////////////////////////////////


        setContentView(R.layout.activity_faq);

        askBtn = (Button)findViewById(R.id.askBtn);
        askBtn.setOnClickListener(this);
        questionEdit = (EditText)findViewById(R.id.questionEdit);
        lv = (ListView)findViewById(R.id.questionsListView);
        lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO
                lv.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                EditText editText = (EditText) view.findViewById(R.id.anwserEditText);
                editText.requestFocus();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
                lv.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });
        this.getQuestion();
    }

    /**
     * 向服务器获取FAQ信息
     */
    private void getQuestion() {
        // 尝试在单独的线程中与服务器联系
        Thread thread = new Thread(new MyThread(userId, password, courseID, MyThread.GET_QUESTIONS));
        thread.start();
    }

    /**
     * 用户点击按钮的回调函数
     * @param v 被点击的控件
     */
    public void onClick(View v) {
        // 用户点击了askBtn按钮提问，尝试在单独的线程中与服务器联系
        Thread thread = new Thread(new MyThread(userId, password, courseID, MyThread.ASK,questionEdit.getText().toString()));
        thread.start();
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
     *  <code>ViewHolder</code> 用于保存每个ListItem中的控件
     */
    public final class ViewHolder{
        public TextView questionNo;
        public TextView questionBody;
        public TextView questionStamp;
        public TextView answerBody;
        public EditText answerEdit;
        public Button answerBt;
    }

    /**
     *  <code>MyAdatper</code> 用于适配mData数据与ListView控件
     */
    public class MyAdatper extends BaseAdapter{
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
            Log.v("MyLog","Postion" + position);

            if (convertView == null) {
                // TEST //
                Log.v("MyLog","Making a new Holder");
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.list_questions, null);
                holder.questionNo = (TextView)convertView.findViewById(R.id.questionNoText);
                holder.questionBody = (TextView)convertView.findViewById(R.id.questionBodyText);
                holder.questionStamp = (TextView)convertView.findViewById(R.id.questionStampText);
                holder.answerBody = (TextView)convertView.findViewById(R.id.anwserBodyText);
                holder.answerEdit = (EditText)convertView.findViewById(R.id.anwserEditText);
                holder.answerBt = (Button)convertView.findViewById(R.id.anwserBt);

                convertView.setTag(holder);
            } else {
                // TEST //
                Log.v("MyLog", "Get a old Holder");
                holder = (ViewHolder)convertView.getTag();
            }
            holder.questionNo.setText(mData.get(position).get("questionNo"));
            holder.questionBody.setText(mData.get(position).get("questionBody"));
            holder.questionStamp.setText(mData.get(position).get("questionStamp"));
            holder.answerBody.setText(mData.get(position).get("anwserBody"));

            if (!holder.answerBt.hasOnClickListeners()) {
                holder.answerBt.setOnClickListener(new MyButtonListener(holder.answerEdit, mData.get(position).get("questionNo")));
            }
            return convertView;
        }

        /**
         *  <code>MyButtonListener</code> 监听器 用于监听ListView中每个Item中的回复按钮
         */
        class MyButtonListener implements OnClickListener {
            private EditText answerText;
            private String questionNo;

            public MyButtonListener(EditText et, String qn) {
                this.answerText = et;
                this.questionNo = qn;
                Log.v("MyLog", "MyButtonListener questionNo: "+MyButtonListener.this.questionNo);

            }
            public void onClick(View v) {
                String ans = answerText.getText().toString();
                if (ans.isEmpty()) {
                    return;
                }
                // 用户点击了回答按钮，尝试在单独的线程中与服务器联系
                Thread thread = new Thread(new MyThread(userId, password, courseID, MyThread.ANWSER,
                        answerText.getText().toString(),this.questionNo));
                thread.start();
            }
        }
    }

    /**
     *  <code>MyThread</code>  实现了Runnable接口的类，用于在单独的线程与服务器进行通信
     */
    class MyThread implements  Runnable {
        private String name;
        private String pass;
        private String courseID;

        private int function;   // 执行的功能代码
        private String body;
        private String questionNo;

        public static final  int ANWSER = 1;
        public static final int ASK = 2;
        public static final int GET_QUESTIONS = 3;

        /**
         * <code>MyThread</code> 类型在单独线程中用于与服务器进行通信
         * @param name_ 用户名
         * @param pass_ 密码
         * @param courseID_ 课程代号
         * @param fc 执行功能代号
         * @param bd 内容主体
         * @param qn 问题编号
         * @return 返回MyThread实例
         */
        public MyThread(String name_, String pass_, String courseID_, int fc,  String bd, String qn) {
            this.name = name_;
            this.pass = pass_;
            this.courseID = courseID_;

            this.function = fc;
            this.body = bd;
            this.questionNo = qn;

            // TEST //
            Log.v("MyLog", "MyThread questionNo: "+MyThread.this.questionNo);
        }

        public MyThread(String name_, String pass_, String courseID_, int fc) {
            this(name_, pass_, courseID_, fc, "", "");
        }

        public MyThread(String name_, String pass_, String courseID_, int fc, String bd) {
            this(name_, pass_, courseID_, fc, bd, "");
        }

        public void run() {
            try {
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                list.add(new BasicNameValuePair("UserID", name));
                list.add(new BasicNameValuePair("Password", pass));
                list.add(new BasicNameValuePair("CourseID", courseID));
                switch  (this.function) {
                    case GET_QUESTIONS:
                    {
                        list.add(new BasicNameValuePair("Action", "LISTQ"));
                    }
                    break;
                    case ANWSER:
                    {
                        list.add(new BasicNameValuePair("Action", "ANWSER"));
                        list.add(new BasicNameValuePair("QuestionNo", questionNo));
                        list.add(new BasicNameValuePair("Body", body));
                    }
                    break;
                    case ASK:
                    {
                        ///////////////////////////
                        // 尚且没有生成QuestionID
                        list.add(new BasicNameValuePair("Action", "ASK"));
                        list.add(new BasicNameValuePair("Body", body));
                        //////////////////////////
                    }
                    break;
                    default:
                        return;
                }
                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(list,"UTF-8");  ;

                Log.v("MyLog", "pairs:"+list.toString());

                // 发送请求，并等待回应
                HttpPost request = new HttpPost(serverUrl);
                request.setEntity(entity);
                HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常，发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());

                    // TEST //
                    Log.v("MyLog", retStr);
                    //////////

                    handler.obtainMessage(this.function, retStr).sendToTarget();
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
