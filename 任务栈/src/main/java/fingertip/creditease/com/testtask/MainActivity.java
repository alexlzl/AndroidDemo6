package fingertip.creditease.com.testtask;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView= (TextView) findViewById(R.id.test_one);
        textView.setText(getTaskId()+"");
        Log.e("TAG","启动页onCreate");
    }

    public void test(View view){
        Intent intent=new Intent(this,Main2Activity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("TAG","启动页onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("TAG","启动页onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("TAG","启动页onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAG","启动页onResume");
    }
}
