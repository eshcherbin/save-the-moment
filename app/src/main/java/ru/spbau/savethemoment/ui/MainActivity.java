package ru.spbau.savethemoment.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.spbau.savethemoment.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mainListButton = (Button) findViewById(R.id.button_main_list);
        mainListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (start List of moments activity)
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
