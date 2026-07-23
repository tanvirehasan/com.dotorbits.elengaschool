package com.dotorbits.elengaschool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private static final long SPLASH_DURATION = 3200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.rgb(6, 59, 61));
        buildWelcomeScreen();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void buildWelcomeScreen() {
        FrameLayout root = new FrameLayout(this);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.school_playground);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(background, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(22), dp(54), dp(22), dp(22));

        GradientDrawable panel = new GradientDrawable();
        panel.setColor(Color.argb(232, 255, 255, 255));
        panel.setCornerRadius(dp(28));
        panel.setStroke(dp(2), Color.rgb(217, 166, 46));
        content.setBackground(panel);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.school_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(260), dp(260));
        content.addView(logo, logoParams);

        TextView welcome = new TextView(this);
        welcome.setText(R.string.welcome_to);
        welcome.setTextColor(Color.rgb(217, 166, 46));
        welcome.setTextSize(14);
        welcome.setLetterSpacing(0.18f);
        welcome.setTypeface(Typeface.DEFAULT_BOLD);
        welcome.setGravity(Gravity.CENTER);
        content.addView(welcome, new LinearLayout.LayoutParams(-1, -2));

        TextView school = new TextView(this);
        school.setText(R.string.school_name);
        school.setTextColor(Color.rgb(7, 91, 76));
        school.setTextSize(21);
        school.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        school.setGravity(Gravity.CENTER);
        school.setPadding(0, dp(7), 0, dp(5));
        content.addView(school, new LinearLayout.LayoutParams(-1, -2));

        TextView motto = new TextView(this);
        motto.setText(R.string.school_motto);
        motto.setTextColor(Color.rgb(6, 59, 61));
        motto.setTextSize(14);
        motto.setGravity(Gravity.CENTER);
        content.addView(motto, new LinearLayout.LayoutParams(-1, -2));

        ProgressBar loading = new ProgressBar(this);
        loading.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(7, 91, 76)));
        LinearLayout.LayoutParams loadingParams = new LinearLayout.LayoutParams(dp(38), dp(38));
        loadingParams.topMargin = dp(12);
        content.addView(loading, loadingParams);

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(-1, -2);
        panelParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        panelParams.setMargins(dp(22), dp(62), dp(22), 0);
        root.addView(content, panelParams);

        setContentView(root);
    }
}
