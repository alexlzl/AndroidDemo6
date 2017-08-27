package fingertip.creditease.com.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView textView;
    private MainActivity mainActivity = this;
    private RevealAnimation mFirstDemoActSwitchAnimTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.test);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFirstDemoActSwitchAnimTool.setAnimType(0).start();
            }
        });
        initTool();
    }

    private void initTool() {
        Intent intent = new Intent(mainActivity, Main2Activity.class);

        mFirstDemoActSwitchAnimTool = new RevealAnimation(MainActivity.this).setAnimType(0)
                .animationView(textView)
                .setIsNeedBackAnimation(false)
                .setStartAnimationColor(Color.parseColor("#FF5777"))
                .setEndAnimationColor(Color.parseColor("#FF5777"))
                .startActivity(intent, false);

    }

    @Override
    protected void onResume() {
        if (mFirstDemoActSwitchAnimTool == null)
            return;
        if (mFirstDemoActSwitchAnimTool.getIsNeedBackAnimation()) {
            mFirstDemoActSwitchAnimTool.setAnimType(1)
                    .setIsWaitingResume(false)
                    .start();
        }

        super.onResume();
    }
}
