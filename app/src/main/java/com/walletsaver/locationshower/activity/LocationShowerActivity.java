package com.walletsaver.locationshower.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.walletsaver.locationshower.R;

public class LocationShowerActivity extends FullscreenActivity {

    @InjectView(R.id.dummy_button) Button refreshButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);
    }

    @OnClick(R.id.dummy_button)
    public void refreshPosition(Button button) {
        // TODO - Refresh the user position
    }
}
