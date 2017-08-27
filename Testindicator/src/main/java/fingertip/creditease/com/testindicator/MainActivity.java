package fingertip.creditease.com.testindicator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    private NavigationTabStrip navigationTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationTabStrip= (NavigationTabStrip) findViewById(R.id.nts_top);
        navigationTabStrip.setTabIndex(0,true);
    }
}
