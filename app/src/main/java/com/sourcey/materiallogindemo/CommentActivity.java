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
import android.widget.SimpleAdapter;
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
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 11/13/2016.
 */

public class CommentActivity extends Activity {
    private String user_id = "";
    private ListView listView;
    private MasterActivity master = new MasterActivity();
    private  int notifications = 0;
    TextView badge;

    @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_comment);

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
        final Button btnNotification = (Button) findViewById(R.id.btnNotification);
        final Button btnLogout = (Button) findViewById(R.id.btnLogout);
        listView = (ListView) this.findViewById(R.id.postListView);
        badge = (TextView) findViewById(R.id.badge);

        this.LoadData();
       this.ConutNotification();

       btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), NotificationActivity.class);
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

    private void LoadData() {
        String url = getString(R.string.url) + "comment.php";
        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_id", user_id));
        try {
            JSONArray data = new JSONArray(getJSONUrl(url, params));
            final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);

                map = new HashMap<String, String>();
                map.put("name", c.getString("name"));
                map.put("rating", c.getString("rating"));
                map.put("detail", c.getString("detail"));
                map.put("comment_datetime", c.getString("comment_datetime"));
                MyArrList.add(map);

            }

            SimpleAdapter sAdap;
            sAdap = new SimpleAdapter(getBaseContext(), MyArrList, R.layout.activity_comment_column,
                    new String[]{"name", "comment_datetime"}, new int[]{R.id.ColName, R.id.ColDate});
            listView.setAdapter(sAdap);

            final AlertDialog.Builder viewDetail = new AlertDialog.Builder(this);
            // OnClick Item
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView,
                                        int position, long mylng) {

                    String name = MyArrList.get(position).get("name")
                            .toString();
                    String rating = MyArrList.get(position).get("rating")
                            .toString();
                    String detail = MyArrList.get(position).get("detail")
                            .toString();
                    String comment_datetime = MyArrList.get(position).get("comment_datetime")
                            .toString();

                    viewDetail.setIcon(android.R.drawable.btn_star_big_on);
                    viewDetail.setTitle("รายละเอียด Comment");
                    viewDetail.setMessage("ชื่อผู้ Comment : " + name + "\n"
                            + "rating : " + rating + "\n"
                            + "รายละเอียด : " + detail + "\n"
                            + "วันที่-เวลา : " + comment_datetime + "\n");
                    viewDetail.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                    dialog.dismiss();
                                }
                            });
                    viewDetail.show();

                }
            });


        } catch (JSONException e) {
            // TODO Auto-generated catch block
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
