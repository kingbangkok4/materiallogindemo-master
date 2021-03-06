package com.sourcey.materiallogindemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rssreader.adapter.PostItemAdapter;
import com.rssreader.vo.PostData;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class PostItemActivity extends Activity {
    private PostData[] listData;
    private String user_id = "";
    private RadioGroup radioGroup;
    ListView listView;
    private String type = "CAR";
    private String strStart = "";
    private String strEnd = "";
    private MasterActivity master = new MasterActivity();
    private  int notifications = 0;
    TextView badge;
    String name = "";
    String image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postlist);

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
            name = extras.getString("name");
            image = extras.getString("image");
        }
        final Button btnPost = (Button) findViewById(R.id.btnPost);
       // final Button btnFeed = (Button) findViewById(R.id.btnFeed);
        final Button btnNotification = (Button) findViewById(R.id.btnNotification);
        final Button btnComment = (Button) findViewById(R.id.btnComment);
        final Button btnLogout = (Button) findViewById(R.id.btnLogout);
        radioGroup = (RadioGroup) findViewById(R.id.radio);
        listView = (ListView) this.findViewById(R.id.postListView);
        badge = (TextView) findViewById(R.id.badge);

        this.generateDummyData();
        this.ConutNotification();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find the radiobutton by returned id
                type = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString();
                if("มีรถ".equals(type)){
                    type = "CAR";
                }else{
                    type = "NOCAR";
                }
                generateDummyData();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                i.putExtra("user_id", user_id);
                i.putExtra("name", name);
                i.putExtra("image", image);
                startActivity(i);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String map_id = listData[position].postMapID;
                String passenger = listData[position].user_id;
                strStart = listData[position].postStart.replace("ต้นทาง: ", "");
                strEnd = listData[position].postEnd.replace("ปลายทาง: ", "");
                DialogRequest(map_id, passenger);
            }
        });
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), NotificationActivity.class);
                i.putExtra("user_id", user_id);
                i.putExtra("name", name);
                i.putExtra("image", image);
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
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), CommentActivity.class);
                i.putExtra("user_id", user_id);
                i.putExtra("name", name);
                i.putExtra("image", image);
                startActivity(i);
            }
        });
    }

    private void ConutNotification() {
        String url = getString(R.string.url) + "notification.php";
        notifications = master.GetCountNotification(user_id, url);
        if (notifications > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(notifications));
        } else {
            badge.setVisibility(View.GONE);
        }
    }
    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 */
    private void generateDummyData() {
        String url = getString(R.string.url) + "map.php";

        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", type));
        try {
            JSONArray data = new JSONArray(getJSONUrl(url, params));
            String txtType = "";
            if("CAR".equals(type)){
                txtType = "ผู้ขับ";
            }else if("NOCAR".equals(type)){
                txtType = "ผู้โดยสาร";
            }
            if (data.length() > 0) {
                PostData data_add = null;
               // listData = null;
                listData = new PostData[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = data.getJSONObject(i);

                    data_add = new PostData();
                    data_add.postMapID = c.getString("map_id");
                    data_add.user_id = c.getString("user_id");
                    data_add.postName = "ชื่อ"+txtType+": " + c.getString("firstname") + " " + c.getString("lastname");
                    data_add.postStart = "ต้นทาง: " + c.getString("start");
                    data_add.postEnd = "ปลายทาง: " + c.getString("end");
                    data_add.postPoint = "จุดนัดพบ: " + c.getString("meeting_point");
                    data_add.postTime = "วัน-เวลา: " + c.getString("map_datetime") + " น.";
                    data_add.postLicensePlate = "ทะเบียน: " + c.getString("license_plate");
                    data_add.postThumbUrl = null;
                    listData[i] = data_add;
                }
                PostItemAdapter itemAdapter = new PostItemAdapter(this,
                        R.layout.postitem, listData);
                listView.setAdapter(itemAdapter);
            }else {
                listView.setAdapter(null);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveRequest(String map_id, String id) {
        String url = getString(R.string.url) + "request.php";

        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("map_id", map_id));
        params.add(new BasicNameValuePair("user_id", id));
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

        this.ConutNotification();
    }

    private void DialogRequest(final String map_id, final String passenger) {
        View dialogBoxView = View.inflate(this, R.layout.dialog_request, null);
        final Button btnMap =(Button)dialogBoxView.findViewById(R.id.btnMap);
        final Button btnRequest =(Button)dialogBoxView.findViewById(R.id.btnRequest);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogMap();
            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("CAR".equals(type)){
                    saveRequest(map_id, user_id);
                }else if("NOCAR".equals(type)){
                    saveRequest(map_id, passenger);
                }else {
                    MessageDialog("ไม่ทราบประเภท!!");
                }
            }
        });
       /* String url = getString(R.string.url_map)+"index.php?poinFrom="+txtStart.getText().toString().trim()+"&poinTo="+txtEnd.getText().toString().trim();

        map.getSettings().setLoadsImagesAutomatically(true);
        map.getSettings().setJavaScriptEnabled(true);
        map.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        map.loadUrl(url);*/

        AlertDialog.Builder builderInOut = new AlertDialog.Builder(this);
        builderInOut.setTitle("ส่งการร้องขอ");
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

    private void DialogMap() {
        View dialogBoxView = View.inflate(this, R.layout.activity_map, null);
        final WebView map =(WebView)dialogBoxView.findViewById(R.id.webView);

        String url = getString(R.string.url_map)+"index.php?poinFrom="+strStart+"&poinTo="+strEnd;

        map.getSettings().setLoadsImagesAutomatically(true);
        map.getSettings().setJavaScriptEnabled(true);
        map.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        map.loadUrl(url);

        AlertDialog.Builder builderInOut = new AlertDialog.Builder(this);
        builderInOut.setTitle("แผนที่");
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
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
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

    public String getJSONUrl(String url, List<NameValuePair> params) {
        StringBuilder str = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) { // Download OK
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
            } else {
                Log.e("Log", "Failed to download file..");
            }
        } catch (ClientProtocolException e) {
            e.getMessage();
        } catch (IOException e) {
            e.getMessage();
        }
        return str.toString();
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

}
