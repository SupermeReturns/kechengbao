package com.sdl.kechengbao;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private List<Map<String, String>> mData; // 存储的FAQ数据，用于ListView进行展示

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
                            JSONArray answerArray = jsonObj.getJSONArray("ANSWER");

                            String questionNo = jsonObj.getString("QuestionNo");
                            String questionBody = askObj.getString("Text");
                            String questionStamp = "Asked by User "+ askObj.getString("UserID") +" on " + askObj.getString("Date");
                            String answerBody = "";
                            for (int j = 0; j < answerArray.length(); j++) {
                                JSONObject ansObj =  (JSONObject)answerArray.opt(j);
                                answerBody += "User: " + ansObj.getString("UserID") + ansObj.getString("Text") + "\n" + ansObj.getString("Date") + "\n";
                            }

                            Map<String, String> mMap;
                            mMap = new HashMap<String, String>();
                            mMap.put("questionNo", questionNo);
                            mMap.put("questionBody", questionBody);
                            mMap.put("questionStamp", questionStamp);
                            mMap.put("answerBody", answerBody);
                            mData.add(mMap);
                        }
                    }
                    break;
                    case MyThread.ANSWER:
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
        serverUrl = savedInstanceState.getString("ServerUrl");
        courseID = savedInstanceState.getString("CourseID");
        /////////////////////////////////////////////

        setContentView(R.layout.activity_faq);

        askBtn = (Button)findViewById(R.id.askBtn);
        askBtn.setOnClickListener(this);

        questionEdit = (EditText)findViewById(R.id.questionEdit);

        this.getQuestion();
        lv = (ListView)findViewById(R.id.questionsListView);
        MyAdatper adapter = new MyAdatper(this);
        lv.setAdapter(adapter);
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
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.list_questions, null);
                holder.questionNo = (TextView)convertView.findViewById(R.id.questionNoText);
                holder.questionBody = (TextView)convertView.findViewById(R.id.questionBodyText);
                holder.questionStamp = (TextView)convertView.findViewById(R.id.questionStampText);
                holder.answerBody = (TextView)convertView.findViewById(R.id.answerEditText);
                holder.answerBody = (Button)convertView.findViewById(R.id.answerBt);

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder)convertView.getTag();
            }
            holder.questionNo.setText(mData.get(position).get("questionNo"));
            holder.questionBody.setText(mData.get(position).get("questionBody"));
            holder.questionStamp.setText(mData.get(position).get("questionStamp"));
            holder.answerBody.setText(mData.get(position).get("answerBody"));


            holder.answerBt.setOnClickListener(new MyButtonListener(holder.answerEdit, mData.get(position).get("questionNo")));
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
            }
            public void onClick(View v) {
                String ans = answerText.getText().toString();
                if (ans.isEmpty()) {
                    return;
                }
                // 用户点击了回答按钮，尝试在单独的线程中与服务器联系
                Thread thread = new Thread(new MyThread(userId, password, courseID, MyThread.ANSWER,
                        answerText.getText().toString()), this.questionNo);
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

        public static final  int ANSWER = 1;
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
        }

        public MyThread(String name_, String pass_, String courseID_, int fc) {
            this(name_, pass_, courseID_, fc, "", "");
        }

        public MyThread(String name_, String pass_, String courseID_, int fc, String bd) {
            this(name_, pass_, courseID_, fc, bd, "");
        }

        public void run() {
            try {
                // 组装JSON
                JSONObject param = new JSONObject();

                param.put("UserID", name);
                param.put("Password", pass);
                param.put("CourseID", courseID);

                switch  (this.function) {
                    case GET_QUESTIONS:
                    {
                        param.put("Action", "LISTQ");
                    }
                    break;
                    case ANSWER:
                    {
                        param.put("Action", "ANSWER");
                        param.put("QuestionNo", questionNo);
                        param.put("Body", body);
                    }
                    break;
                    case ASK:
                    {
                        ///////////////////////////
                        // 尚且没有生成QuestionID
                        param.put("Action", "ASK");
                        param.put("Body", body);
                        //////////////////////////
                    }
                    break;
                    default:
                        return;
                }

                // 发送请求，并等待回应
                HttpPost request = new HttpPost(serverUrl);
                StringEntity se = new StringEntity(param.toString());
                request.setEntity(se);
                HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 如果返回码为200，代表与服务器的交互正常，发送返回信息给UI处理
                    String retStr = EntityUtils.toString(httpResponse.getEntity());
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
