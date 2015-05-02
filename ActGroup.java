package com.example.ooad;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
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
        Intent mIntent = new Intent(ActGroup.this, ShowNotice.class);
        mIntent.putExtras(mBundle);
        container = (LinearLayout) findViewById(R.id.containerBody);
        container.removeAllViews();
        container.addView(getLocalActivityManager().startActivity(
                "ShowNotice",
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());

        // 模块1
        ImageView btnModule1 = (ImageView) findViewById(R.id.btnModule1);
        btnModule1.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent mIntent = new Intent(ActGroup.this, ShowCources.class);
						startActivity(mIntent);
					}   
        });

        // 模块2
//        ImageView btnModule2 = (ImageView) findViewById(R.id.btnModule2);
//        btnModule2.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                container.removeAllViews();
//                container.addView(getLocalActivityManager().startActivity(
//                        "Module2",
//                        new Intent(TestView.this, ModuleView2.class)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                        .getDecorView());
//            }
//        });

        // 模块3
//        ImageView btnModule3 = (ImageView) findViewById(R.id.btnModule3);
//        btnModule3.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                container.removeAllViews();
//                container.addView(getLocalActivityManager().startActivity(
//                        "Module3",
//                        new Intent(TestView.this, ModuleView3.class)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                        .getDecorView());
//            }
//        });
    }
    
    // 模块4
//  ImageView btnModule4 = (ImageView) findViewById(R.id.btnModule4);
//  btnModule4.setOnClickListener(new OnClickListener() {
//      @Override
//      public void onClick(View v) {
//          container.removeAllViews();
//          container.addView(getLocalActivityManager().startActivity(
//                  "Module4",
//                  new Intent(TestView.this, ModuleView4.class)
//                          .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                  .getDecorView());
//      }
//  });
    
}
