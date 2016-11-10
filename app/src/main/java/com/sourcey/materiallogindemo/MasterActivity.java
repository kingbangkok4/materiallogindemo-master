package com.sourcey.materiallogindemo;


import android.util.Log;
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
 * Created by PC on 10/11/2559.
 */

public class MasterActivity {

    public int GetCountNotification(String user_id, String url){
        PostData[] listData;
        int count = 0;
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
                count = listData.length;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    private String getHttpPost(String url, List<NameValuePair> params) {
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

    private String getJSONUrl(String url, List<NameValuePair> params) {
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

}
