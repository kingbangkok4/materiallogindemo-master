package com.sourcey.materiallogindemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class post extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);


        final Button btn1 = (Button) findViewById(R.id.button);
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent newActivity = new Intent(post.this, feed.class);
                startActivity(newActivity);
            }
        });

        final Button btn2 = (Button) findViewById(R.id.button4);
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent newActivity = new Intent(post.this, MainActivity.class);
                startActivity(newActivity);
            }
        });

        final Button btn3 = (Button) findViewById(R.id.button10);
        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent newActivity = new Intent(post.this, req.class);
                startActivity(newActivity);
            }
        });

        final Button btn5 = (Button) findViewById(R.id.button3);
        btn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(post.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
}
