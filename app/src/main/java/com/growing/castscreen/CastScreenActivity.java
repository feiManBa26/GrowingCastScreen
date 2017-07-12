package com.growing.castscreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.growing.castscreen.base.BaseAppCommpatActivity;
import com.growing.castscreen.localSocket.LClient;
import com.growing.castscreen.services.CastScreenServices;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * File: CastScreenActivity.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-07-12 16:25
 */

public class CastScreenActivity extends BaseAppCommpatActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LClient lClient = CastScreenServices.getmLClient();
        lClient.send(2);
        setContentView(R.layout.activity_cast_screen);
        ButterKnife.bind(this);
        initToolBar();

    }

    public static Intent getIntent(Context context) {
        return new Intent(context, CastScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    private void initToolBar() {
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setTitle("屏幕同步");
        mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}

