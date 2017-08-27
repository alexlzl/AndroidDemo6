package fingertip.creditease.com.testtask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.e("TAG","第二页onCreate");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("TAG","第二页onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("TAG","第二页onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("TAG","第二页onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAG","第二页onResume");
    }
}
