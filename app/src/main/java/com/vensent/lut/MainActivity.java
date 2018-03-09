package com.vensent.lut;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button btnLoadFromPng = findViewById(R.id.btn_load_from_png);
        Button btnLoadThroughJni = findViewById(R.id.btn_load_through_jni);
        Button btnLoadFromCubeFile = findViewById(R.id.btn_load_from_cube_file);

        btnLoadFromPng.setOnClickListener(this);
        btnLoadThroughJni.setOnClickListener(this);
        btnLoadFromCubeFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_load_from_png:
                intent = new Intent(this, LoadLutPngActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_load_through_jni:
                intent = new Intent(this, LoadLutJniActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_load_from_cube_file:
                intent = new Intent(this, LoadCubeFileActivity.class);
                startActivity(intent);
                break;
        }
    }
}
