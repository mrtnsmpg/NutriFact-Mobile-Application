package com.example.nutrifact;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_SCREEN = 5000;

//Variable_animation
    Animation topAnim, botAnim;
    ImageView logo;
    TextView txtLogo,txtSlogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //animation
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        botAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        //hooks
        logo = findViewById(R.id.logo);
        txtLogo = findViewById(R.id.txtLogo);
        txtSlogan = findViewById(R.id.txtSlogan);

        logo.setAnimation(topAnim);
        txtLogo.setAnimation(botAnim);
        txtSlogan.setAnimation(botAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,WelcomeScreen.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);




    }
}