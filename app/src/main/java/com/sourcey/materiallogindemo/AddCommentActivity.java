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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 11/13/2016.
 */

public class AddCommentActivity extends Activity {
    private RatingBar ratingBar;
    private EditText editTextDetail;
    private TextView txtRatingValue;
    private TextView txtName;
    private Button btnSubmit;
    private String user_id = "";
    private String driver_id = "";
    private String match_id = "";
    private String firstname = "";
    private String lastname = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        // Permission StrictMode
        if (Build.VERSION.SDK_INT > 9) {
            android.os.StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            android.os.StrictMode.setThreadPolicy(policy);
        }

        Bundle extras = getIntent().getExtras();
        // เช็คว่ามีค่าที่ส่งมาจากหน้าอื่นหรือไม่ถ้ามีจะไม่เท่ากับ null
        if (extras != null) {
            user_id = extras.getString("user_id");
        }

        addListenerOnRatingBar();
        addListenerOnButton();

        if(checkComment(getString(R.string.url), user_id)){
            txtName.setText("ชื่อผู้ขับ : "+firstname+" "+lastname);
            editTextDetail.setText("");
        }else {
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            i.putExtra("user_id", user_id);
            startActivity(i);
        }

    }

    public void addListenerOnRatingBar() {
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);
        txtName = (TextView) findViewById(R.id.txtName);
        editTextDetail = (EditText)findViewById(R.id.editTextDetail);

        txtRatingValue.setText(String.valueOf(ratingBar.getRating()));

        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

        txtRatingValue.setText(String.valueOf(rating));

    }
});
        }

public void addListenerOnButton() {

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        //if click on me, then display the current rating value.
        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(AddCommentActivity.this,
                        String.valueOf(ratingBar.getRating()),
                        Toast.LENGTH_SHORT).show();
                updateComment();
                if(checkComment(getString(R.string.url), user_id)){
                    txtName.setText("ชื่อผู้ขับ : "+firstname+" "+lastname);
                    editTextDetail.setText("");
                }else {
                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    i.putExtra("user_id", user_id);
                    startActivity(i);
                }
            }

        });
    }

    public  boolean checkComment(String url, String userId){
        String strStatusID = "";
        String Error = "";
        url = url+"checkComment.php";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_id", userId));
        String resultServer  = getHttpPost(url,params);
        JSONObject c;
        try {
            c = new JSONObject(resultServer);
            strStatusID = c.getString("StatusID");
            match_id = c.getString("match_id");
            driver_id = c.getString("driver");
            firstname = c.getString("firstname");
            lastname = c.getString("lastname");
            Error = c.getString("Error");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(strStatusID.equals("0")){
            return false;
        }
        else {
            return true;
        }
    }

    public void updateComment(){
        String status = "";
        String url = getString(R.string.url)+"updateComment.php";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("match_id", match_id));
        params.add(new BasicNameValuePair("user_id", user_id));
        params.add(new BasicNameValuePair("driver_id", driver_id));
        params.add(new BasicNameValuePair("detail", editTextDetail.getText().toString().trim()));
        params.add(new BasicNameValuePair("rating", String.valueOf(ratingBar.getRating())));
        String resultServer  = getHttpPost(url,params);
        JSONObject c;
        try {
            c = new JSONObject(resultServer);
            status = c.getString("status");
            MessageDialog(status);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            status = e.getMessage();
            MessageDialog(status);
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
