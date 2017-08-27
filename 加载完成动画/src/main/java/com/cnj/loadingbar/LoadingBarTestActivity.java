package com.cnj.loadingbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.demo.library.LoadingView;

/**
 * @author wangzy
 * @desciption
 * @date 2015/10/23. 10:13
 */
public class LoadingBarTestActivity extends AppCompatActivity{
    private LoadingView lb;
    private LoadingView lbFailed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        lb = (LoadingView) findViewById(R.id.lb_loading);
        lbFailed = (LoadingView) findViewById(R.id.lb_loadingfailed);
        findViewById(R.id.failed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                lbFailed.loadingComplete(false);
                lb.loadingComplete(false);
            }
        });

        findViewById(R.id.success).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lb.loadingComplete(true);
            }
        });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lb.startLoading();
            }
        });
        findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lb.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lb.setVisibility(View.GONE);
            }
        });
    }
}
