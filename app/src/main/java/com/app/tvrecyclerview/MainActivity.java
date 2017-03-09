package com.app.tvrecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNormal = (Button) findViewById(R.id.btn_normal);
        btnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NormalFocusActivity.class);
                startActivity(intent);
            }
        });

        Button btnAuto = (Button) findViewById(R.id.btn_auto_focus);
        btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AutoCarouselActivity.class);
                startActivity(intent);
            }
        });

        Button btnMaulHorizontal = (Button) findViewById(R.id.btn_maul_focus);
        btnMaulHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, maulCarouselActivity.class);
                startActivity(intent);
            }
        });

        Button btnMaulVertical = (Button) findViewById(R.id.btn_maul_vertical_focus);
        btnMaulVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, maulVerticalCarouselActivity.class);
                startActivity(intent);
            }
        });
    }
}
