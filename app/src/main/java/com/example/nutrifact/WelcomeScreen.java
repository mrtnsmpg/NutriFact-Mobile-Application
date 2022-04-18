package com.example.nutrifact;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class WelcomeScreen extends AppCompatActivity {

    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;
    private SliderAdapter sliderAdapter;
    private TextView[] dots;
    private TextView mFinishButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen_main);


        mFinishButton = findViewById(R.id.finishBtn);
        mSlideViewPager = findViewById(R.id.viewPager);
        mDotLayout = findViewById(R.id.dotLayout);

        sliderAdapter = new SliderAdapter(WelcomeScreen.this);
        mSlideViewPager.setAdapter(sliderAdapter);

        addDots(0);
        mSlideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addDots(position);
                if(position == 2){
                    mFinishButton.setEnabled(true);
                    mFinishButton.setVisibility(View.VISIBLE);
                }else{
                    mFinishButton.setEnabled(false);
                    mFinishButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this,CameraX.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addDots(int position){

        dots = new TextView[3];
        mDotLayout.removeAllViews();
        for(int i = 0; i < dots.length; i++){
            dots[i] = new TextView(WelcomeScreen.this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.white));

            mDotLayout.addView(dots[i]);

        }

        if(dots.length>0){
            dots[position].setTextColor(getResources().getColor(R.color.red));
        }

    }
}
