package com.example.callback;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
//import android.os.StrictMode;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

public class MainActivity extends Activity {
    private final int REQUEST_CONTACT = 1;
    private String balanceurlformat, callurlformat;
    private String username, password;

    public SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(this);


        Button rbutton = (Button) findViewById(R.id.reflesh);
        rbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                balanceurlformat = pref.getString("balanceurlapi", "");
                username = pref.getString("username", "");
                password = pref.getString("password", "");
                String balanceurl = String.format(balanceurlformat, username, password);
                htmlHandler(R.id.balance, balanceurl);
            }
        });

        Button cbutton = (Button) findViewById(R.id.call);
        cbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                callurlformat = pref.getString("callurlapi", "");
                username = pref.getString("username", "");
                EditText calleetext = (EditText) findViewById(R.id.callee);
                String callee = calleetext.getText().toString();
                String callurl = String.format(callurlformat, username, callee);
                htmlHandler(R.id.callstatus, callurl);
            }
        });

        Button contactbutton = (Button) findViewById(R.id.contact);
        contactbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType("vnd.android.cursor.dir/phone");
                startActivityForResult(intent, REQUEST_CONTACT);
            }
        });

        Button areabutton = (Button) findViewById(R.id.areabutton);
        areabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText calleetext = (EditText) findViewById(R.id.checkareatext);
                String num = calleetext.getText().toString();
                if (null != num && num.length() > 0) {
                    GetArea area = new GetArea(pref.getString("areadatabaseloc", ""), R.id.checkarea);
                    area.execute(num);
                }
            }

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (REQUEST_CONTACT):
                if (data == null) {
                    return;
                }
                ContentResolver cr = getContentResolver();
                Uri result = data.getData();
                Cursor c = cr.query(result, null, null, null, null);
                if (c.moveToFirst()) {
                    String number = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)).replaceAll(" |-", "");
                    EditText calleetext = (EditText) findViewById(R.id.callee);
                    calleetext.setText(number);
                }
                c.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.update:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://zhupengjia.cn:55/files/callback.apk"));
                startActivity(intent);
                return true;
        }
        return false;
    }

    public void htmlHandler(int v, String u) {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        TextView textview = (TextView) findViewById(v);
        if (networkInfo != null && networkInfo.isConnected()) {
            textview.setText("正在连接中......");
            webCallback task = new webCallback(v);
            task.execute(u);
        } else {
            textview.setText("没有网络");
        }
    }

    private class webCallback extends webCallbackTask {
        private TextView textview = null;

        public webCallback(int v) {
            this.textview = (TextView) findViewById(v);
        }

        @Override
        protected void onPostExecute(String result) {
            textview.setText(result);
        }
    }

    private class GetArea extends GetAreaTask {
        private TextView textview = null;

        public GetArea(String d, int v) {
            super(d);
            this.textview = (TextView) findViewById(v);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                textview.setText("没找到...");
            } else {
                textview.setText(result);
            }
        }
    }
}

