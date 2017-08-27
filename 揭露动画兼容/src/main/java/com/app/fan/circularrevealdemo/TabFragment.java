package com.app.fan.circularrevealdemo;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.util.Random;

import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by fan on 2016/1/18.
 */
public class TabFragment extends Fragment {
    private TextView mTextView;
    private String text;
    private final int[] COLORS = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.GRAY};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_layout, null);
        mTextView = (TextView) v.findViewById(R.id.tv_fr);
        mTextView.setText(text);
        v.post(new Runnable() {
            @Override
            public void run() {
                animateRevealColorFromCoordinates(mTextView, 0, 0);
            }
        });
        return v;
    }

    public TabFragment(String text) {
        this.text = text;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private Animator animateRevealColorFromCoordinates(View viewRoot, int x, int y) {
        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());
        viewRoot.setBackgroundColor(COLORS[new Random().nextInt(4)]);
        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        anim.setDuration(1500);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }
}
