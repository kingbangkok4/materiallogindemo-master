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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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


/**
 * Created by PC on 10/27/2016.
 */

public class NotificationActivity extends Activity {
    private PostData[] listData;
    private String user_id = "";
    private RadioGroup radioGroup;
    ListView listView;
    private String type = "CAR";
    private MasterActivity master = new MasterActivity();
    private  int notifications = 0;
    TextView badge;
    //private String strStart = "";
    //private String strEnd = "";
    String request_id = "";
    String map_id = "";
    String r_user_id = "";
    String r_type  = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nontification);

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

        final Button btnPost = (Button) findViewById(R.id.btnPost);
        final Button btnFeed = (Button) findViewById(R.id.btnFeed);
        final Button btnLogout = (Button) findViewById(R.id.btnLogout);
        final Button btnComment = (Button) findViewById(R.id.btnComment);
        listView = (ListView) this.findViewById(R.id.postListView);
        badge = (TextView) findViewById(R.id.badge);

        this.generateDummyData();
        this.ConutNotification();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                request_id = listData[position].request_id;
                map_id = listData[position].postMapID;
                r_user_id = listData[position].r_user_id;
                r_type  = listData[position].type;
                DialogConfirmRequest();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                i.putExtra("user_id", user_id);
                startActivity(i);

            }
        });
        btnFeed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), CommentActivity.class);
                i.putExtra("user_id", user_id);
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

    private void DialogConfirmRequest() {
        View dialogBoxView = View.inflate(this, R.layout.dialog_confirm_request, null);
        final Button btnAccept = (Button) dialogBoxView.findViewById(R.id.btnAccept);
        final Button btnReject = (Button) dialogBoxView.findViewById(R.id.btnReject);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfirm(request_id, "ACCEPT");
            }
        });
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfirm(request_id, "REJECT");
            }
        });
       /* String url = getString(R.string.url_map)+"index.php?poinFrom="+txtStart.getText().toString().trim()+"&poinTo="+txtEnd.getText().toString().trim();

        map.getSettings().setLoadsImagesAutomatically(true);
        map.getSettings().setJavaScriptEnabled(true);
        map.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        map.loadUrl(url);*/

        AlertDialog.Builder builderInOut = new AlertDialog.Builder(this);
        builderInOut.setTitle("ตอบรับการร้องขอมา");
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

    private void saveConfirm(String request_id, String action) {
        String url = getString(R.string.url) + "match.php";

        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("request_id", request_id));
        params.add(new BasicNameValuePair("action", action));
        params.add(new BasicNameValuePair("map_id", map_id));
        if("NOCAR".equals(type)){
            params.add(new BasicNameValuePair("passenger", r_user_id));
        }else {
            params.add(new BasicNameValuePair("passenger", user_id));
        }
        String resultServer  = getHttpPost(url,params);

        JSONObject c;
        try {
            c = new JSONObject(resultServer);
            String status = c.getString("status");
            MessageDialog(status);
            generateDummyData();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            MessageDialog(e.getMessage());
        }

        this.ConutNotification();
    }

    private void generateDummyData() {
        String url = getString(R.string.url) + "notification.php";
        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_id", user_id));
        try {
            JSONArray data = new JSONArray(getJSONUrl(url, params));
            String txtType = "";

            if (data.length() > 0) {
                PostData data_add = null;
                // listData = null;
                listData = new PostData[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = data.getJSONObject(i);

                    if ("CAR".equals(c.getString("type"))) {
                        txtType = "ผู้โดยสาร";
                    } else if ("NOCAR".equals(c.getString("type"))) {
                        txtType = "ผู้ขับ";
                    }

                    data_add = new PostData();
                    data_add.request_id = c.getString("request_id");
                    data_add.postMapID = c.getString("map_id");
                    data_add.m_user_id = c.getString("m_user_id");
                    data_add.r_user_id = c.getString("r_user_id");
                    data_add.type = c.getString("type");
                    data_add.postName = "ชื่อ" + txtType + ": " + c.getString("firstname") + " " + c.getString("lastname");
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
            } else {
                listView.setAdapter(null);
            }
            this.ConutNotification();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
