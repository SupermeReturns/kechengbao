package com.sdl.kechengbao;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ActGroup extends ActivityGroup {

    private LinearLayout container = null;

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
        ImageView btnModule1 = (ImageView) findViewById(R.id.btnModule1);
        btnModule1.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
                        ActGroup.this.finish();
						// Intent mIntent = new Intent(ActGroup.this, ShowCources.class);
						//startActivity(mIntent);
					}   
        });

        // 模块2
       ImageView btnModule2 = (ImageView) findViewById(R.id.btnModule2);
       btnModule2.setOnClickListener(new OnClickListener() {
           @Override
            public void onClick(View v) {
                if(ActGroup.this.getIntent().getExtras() == null) {
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
       ImageView btnModule3 = (ImageView) findViewById(R.id.btnModule3);
        btnModule3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        ImageView btnModule4 = (ImageView) findViewById(R.id.btnModule4);
        btnModule4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }
    
}
