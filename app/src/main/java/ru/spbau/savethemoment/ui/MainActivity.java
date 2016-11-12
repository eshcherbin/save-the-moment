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

        // set button click handlers
        Button main_list_button = (Button) findViewById(R.id.button_main_list);
        main_list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (start List of moments activity)
            }
        });

        Button main_map_button = (Button) findViewById(R.id.button_main_map);
        main_map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (start Map of moments activity)
            }
        });

        Button main_create_button = (Button) findViewById(R.id.button_main_create);
        main_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle click (Create new moment and)
            }
        });
    }
}
