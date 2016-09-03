package com.gjiazhe.wavesidebar.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.gjiazhe.wavesidebar.WaveSideBar;

public class MainActivity extends AppCompatActivity {
    WaveSideBar sideBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sideBar = (WaveSideBar) findViewById(R.id.sideBar);
        sideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String item) {
                Log.i("selectItem", item);
            }
        });
    }
}
