package fingertip.creditease.com.testsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textViewWrite;
    private TextView textViewRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewWrite = (TextView) findViewById(R.id.write);
        textViewRead = (TextView) findViewById(R.id.read);
    }

    public void testSp(View view) {
        textViewWrite.setText("testtttt");
        /**
         * write
         */
        SharedPreferences sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE);
//        SharedPreferences sharedPreferences1= PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("test", "testtttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttttesttttt");
        editor.apply();//异步提交
//        editor.commit();//同步提交
        /**
         * read
         */
        SharedPreferences sharedPreferences2 = getSharedPreferences("sp", Context.MODE_PRIVATE);
        String s = sharedPreferences2.getString("test", "default");
        textViewRead.setText(s);

    }
}
