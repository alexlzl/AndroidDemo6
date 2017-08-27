package com.app.fan.circularrevealdemo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TabLayout tab_layout;
    private ViewPager viewpager;
    private List<Fragment> mList = new ArrayList();
    private String[] mTitles = {"首页", "动态", "其它"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tab_layout = (TabLayout) findViewById(R.id.tab_layout);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        tab_layout.setTabMode(TabLayout.MODE_FIXED);
        mList.add(new TabFragment("首页"));
        mList.add(new TabFragment("动态"));
        mList.add(new TabFragment("其他"));
        viewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mList.get(position);
            }

            @Override
            public int getCount() {
                return mList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }
        });
        viewpager.setOffscreenPageLimit(0);
        tab_layout.setupWithViewPager(viewpager);


    }
}
