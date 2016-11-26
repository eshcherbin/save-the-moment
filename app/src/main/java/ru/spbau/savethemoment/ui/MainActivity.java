package ru.spbau.savethemoment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.spbau.savethemoment.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mainListButton = (Button) findViewById(R.id.button_main_list);
        mainListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListOfMomentsActivity.class);
                startActivity(intent);
            }
        });

        Button mainMapButton = (Button) findViewById(R.id.button_main_map);
        mainMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (start Map of moments activity)
            }
        });

        Button mainCreateButton = (Button) findViewById(R.id.button_main_create);
        mainCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (Create new moment and start View moment activity)
            }
        });
    }

}
