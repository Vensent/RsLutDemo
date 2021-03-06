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
        Button btnLoadFromCubeFileViaJni = findViewById(R.id.btn_load_from_cube_file_via_jni);
        Button btnAdjustStrengthBlending = findViewById(R.id.btn_adjust_strength_via_image_blending);
        Button btnAdjustStrengthInterpolated = findViewById(R.id.btn_adjust_strength_via_lut_interpolated);

        btnLoadFromPng.setOnClickListener(this);
        btnLoadThroughJni.setOnClickListener(this);
        btnLoadFromCubeFile.setOnClickListener(this);
        btnLoadFromCubeFileViaJni.setOnClickListener(this);
        btnAdjustStrengthBlending.setOnClickListener(this);
        btnAdjustStrengthInterpolated.setOnClickListener(this);
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
            case R.id.btn_load_from_cube_file_via_jni:
                intent = new Intent(this, LoadCubeFileJniActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_adjust_strength_via_image_blending:
                intent = new Intent(this, AdjustStrengthBlendingActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_adjust_strength_via_lut_interpolated:
                intent = new Intent(this, AdjustStrengthLutActivity.class);
                startActivity(intent);
                break;
        }
    }
}
