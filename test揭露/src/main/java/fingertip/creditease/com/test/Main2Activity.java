package fingertip.creditease.com.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

    }
    @Override
    protected void onPause() {
        overridePendingTransition(0,0);
        super.onPause();
    }

    public void ontest(View view){
        startActivity(new Intent(this,MainActivity.class));
    }
}
