package com.sourcey.materiallogindemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    //private DecimalFormat df = new DecimalFormat("#,###,###.##");
    //private SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String datePoint = "";
    private EditText txtStart;
    private EditText txtEnd;
    private EditText txtAppoint;
    private EditText txtTime;
    private EditText txtLicensePlate;
    private RadioGroup radioGroup;

    private String user_id = "";
    private String meeting_point = "";
    private String license_plate = "";
    private String map_datetime = "";
    private String start = "";
    private String end = "";
    private String type = "";
    private String url = "";

    private int mYear, mMonth, mDay;
    private int mHour;
    private int mMinute;
    static final int TIME_DIALOG_ID = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permission StrictMode
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Bundle extras = getIntent().getExtras();
        // เช็คว่ามีค่าที่ส่งมาจากหน้าอื่นหรือไม่ถ้ามีจะไม่เท่ากับ null
        if (extras != null) {
            user_id = extras.getString("user_id");
        }

        txtStart = (EditText) findViewById(R.id.txtStart);
        txtEnd = (EditText) findViewById(R.id.txtEnd);
        txtAppoint = (EditText) findViewById(R.id.txtAppoint);
        txtTime = (EditText) findViewById(R.id.txtTime);
        txtLicensePlate = (EditText) findViewById(R.id.txtLicensePlate);

        radioGroup = (RadioGroup) findViewById(R.id.radio);

        final Button btnSave = (Button) findViewById(R.id.btnSave);
        final Button btnCancel = (Button) findViewById(R.id.btnCancel);
        //final Button btnPost = (Button) findViewById(R.id.btnPost);
        final Button btnFeed = (Button) findViewById(R.id.btnFeed);
        final Button btnSearch = (Button) findViewById(R.id.btnSearch);
        final Button btnNotification = (Button) findViewById(R.id.btnNotification);
        final Button btnLogout = (Button) findViewById(R.id.btnLogout);
        final Button btnTime = (Button) findViewById(R.id.btnTime);
        // get the current time
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        mHour = c.get(Calendar.HOUR);
        mMinute = c.get(Calendar.MINUTE);

        // display the current time
        updateCurrentTime();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogMap();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                type = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
                if("มีรถ".equals(type)){
                    type = "CAR";
                }else{
                    type = "NOCAR";
                }
               /* Toast.makeText(getBaseContext(),
                        type, Toast.LENGTH_SHORT).show();*/

                start = txtStart.getText().toString();
                end = txtEnd.getText().toString();
                meeting_point = txtAppoint.getText().toString();
                map_datetime = datePoint.trim();
                license_plate = txtLicensePlate.getText().toString();



                if ("".equals(start) || "".equals(end) || "".equals(meeting_point) || "".equals(map_datetime) || "".equals(license_plate)) {
                    MessageDialog("กรุณากรอกข้อมูลให้ครบถ้วน");
                } else {
                    String url = getString(R.string.url)+"save.php";
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("user_id", user_id));
                    params.add(new BasicNameValuePair("start", start));
                    params.add(new BasicNameValuePair("end", end));
                    params.add(new BasicNameValuePair("meeting_point", meeting_point));
                    params.add(new BasicNameValuePair("map_datetime", map_datetime));
                    params.add(new BasicNameValuePair("license_plate", license_plate));
                    params.add(new BasicNameValuePair("type", type));

                    String resultServer  = getHttpPost(url,params);

                    JSONObject c;
                    try {
                        c = new JSONObject(resultServer);
                        String status = c.getString("status");
                        MessageDialog(status);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        MessageDialog(e.getMessage());
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClearData();
            }
        });

/*        btnPost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               *//* Intent i = new Intent(getBaseContext(), PostItemActivity.class);
                i.putExtra("user_id", user_id);
                startActivity(i);*//*
                // get selected radio button from radioGroup

            }
        });*/

        btnFeed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              /*  if(!start.getText().toString().equals("") &&!end.getText().toString().equals("")&&
                        !point.getText().toString().equals("")&&!time.getText().toString().equals("")&&!license.getText().toString().equals("")) {
                    Intent newActivity = new Intent(MainActivity.this, commit.class);
                    startActivity(newActivity);
                }*/
                Intent i = new Intent(getBaseContext(), PostItemActivity.class);
                i.putExtra("user_id", user_id);
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(i);
            }
        });

  /*      final Button btn5 = (Button) findViewById(R.id.button3);
        btn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });*/


        //RadioGroup car = (RadioGroup) this.findViewById ( R.id.textView18 );

        // car.check(R.id.radioButton);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void DialogMap() {
        View dialogBoxView = View.inflate(this, R.layout.activity_map, null);
        final WebView map =(WebView)dialogBoxView.findViewById(R.id.webView);

        String url = getString(R.string.url_map)+"index.php?poinFrom="+txtStart.getText().toString().trim()+"&poinTo="+txtEnd.getText().toString().trim();

        map.getSettings().setLoadsImagesAutomatically(true);
        map.getSettings().setJavaScriptEnabled(true);
        map.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        map.loadUrl(url);

        AlertDialog.Builder builderInOut = new AlertDialog.Builder(this);
        builderInOut.setTitle("Map");
        builderInOut.setMessage("")
                .setView(dialogBoxView)
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void MessageDialog(String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public String getHttpPost(String url, List<NameValuePair> params) {
        StringBuilder str = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) { // Status OK
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
            } else {
                Log.e("Log", "Failed to download result..");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    private void ClearData() {
        txtStart.setText("");
        txtEnd.setText("");
        txtAppoint.setText("");
        txtTime.setText("");
        txtLicensePlate.setText("");
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener,
                        mHour, mMinute, false);
        }
        return null;
    }

    // updates the time we display in the editText
    private void updateCurrentTime() {
        datePoint =(mYear+"-"+mMonth+"-"+mDay+" "+mHour+":"+mMinute);
        //txtTime.setText(datePoint.toString());
        txtTime.setText(
                new StringBuilder()
                        .append(mHour).append(":")
                        .append(mMinute));
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    // TODO Auto-generated method stub
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateCurrentTime();
                }
            };

    /*@Override
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


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.sourcey.materiallogindemo/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.sourcey.materiallogindemo/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }*/
}
