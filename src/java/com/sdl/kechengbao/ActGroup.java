package com.sdl.kechengbao;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActGroup extends ActivityGroup {

    private LinearLayout container = null;
    private int curIdx = -1;
    private TextView[] textViews = new TextView[4];
    private ImageView[] imageViews = new ImageView[4];
    private static final int[] buttons_normal = {R.drawable.buttons_1_normal, R.drawable.buttons_2_normal,R.drawable.buttons_3_normal,R.drawable.buttons_4_normal};
    private static final int[] buttons_selected = {R.drawable.buttons_1_selected,R.drawable.buttons_2_selected,R.drawable.buttons_3_selected,R.drawable.buttons_4_selected};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_appgroup);

        Bundle mBundle = this.getIntent().getExtras();
        final Intent mIntent = new Intent(ActGroup.this, ShowChapters.class);
        mIntent.putExtras(mBundle);
        container = (LinearLayout) findViewById(R.id.containerBody);
        container.removeAllViews();
        container.addView(getLocalActivityManager().startActivity(
                "Show Chapters",
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());

        // 模块1
        textViews[0] = (TextView)findViewById(R.id.text1);
        imageViews[0] = (ImageView) findViewById(R.id.btnModule1);
        imageViews[0].setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // 转换按键风格
                ActGroup.this.changeButtonStyleTo(0);

                ActGroup.this.finish();
                // Intent mIntent = new Intent(ActGroup.this, ShowCources.class);
                //startActivity(mIntent);
            }
        });

        // 模块2
        textViews[1] = (TextView)findViewById(R.id.text2);
        imageViews[1] = (ImageView) findViewById(R.id.btnModule2);
        imageViews[1].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 转换按键风格
                ActGroup.this.changeButtonStyleTo(1);

                // 跳转Activity
                if (ActGroup.this.getIntent().getExtras() == null) {
                    Log.v("MyLog", "Null Bundle");
                }
                Intent mIntent = new Intent();
                mIntent.setClass(ActGroup.this, ShowChapters.class);
                mIntent.putExtras(ActGroup.this.getIntent().getExtras());

                container.removeAllViews();
                container.addView(getLocalActivityManager().startActivity(
                        "Show Chapters",
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        .getDecorView());
            }
        });

        // 模块3
        textViews[2] = (TextView)findViewById(R.id.text3);
        imageViews[2] = (ImageView) findViewById(R.id.btnModule3);
        imageViews[2].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 转换按键风格
                ActGroup.this.changeButtonStyleTo(2);

                // 跳转Activity
                Intent mIntent = new Intent();
                mIntent.setClass(ActGroup.this, FAQActivity.class);
                mIntent.putExtras(ActGroup.this.getIntent().getExtras());

                container.removeAllViews();
                container.addView(getLocalActivityManager().startActivity(
                        "Show FAQ",
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        .getDecorView());
            }
        });
        // 模块4
        textViews[3] = (TextView)findViewById(R.id.text4);
        imageViews[3] = (ImageView) findViewById(R.id.btnModule4);
        imageViews[3].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 转换按键风格
                ActGroup.this.changeButtonStyleTo(3);

                // 跳转Activity
                Intent mIntent = new Intent();
                mIntent.setClass(ActGroup.this, ShowNotice.class);
                mIntent.putExtras(ActGroup.this.getIntent().getExtras());

                container.removeAllViews();
                container.addView(getLocalActivityManager().startActivity(
                        "ShowNotices",
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        .getDecorView());
            }
        });

        // 转换按键风格
        this.changeButtonStyleTo(1);
    }

    private void  changeButtonStyleTo(int i)
    {
        // 恢复上一个按键的风格
        if (curIdx != -1)
        {
            textViews[curIdx].setTextColor(Color.rgb(0, 0, 0));
            imageViews[curIdx].setImageResource(buttons_normal[curIdx]);
        }

        // 调整下一个按键的风格
        textViews[i].setTextColor(Color.rgb(255, 255, 255));
        imageViews[i].setImageResource(buttons_selected[i]);

        // 标记选中按钮的索引
        curIdx = i;
    }

}
