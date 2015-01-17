package com.example.callback;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
//import android.util.Log;


public class GetAreaTask extends AsyncTask<String, String, String> {
    Cursor cur = null;
    String checknum, loc, car;
    String dbloc;

    public GetAreaTask(String d) {
        dbloc = d;
    }

    @Override
    protected String doInBackground(String... params) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbloc, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        String num = params[0];
        String bit1 = num.substring(0, 1);
        if (bit1.equals("+")) {
            if (num.substring(1, 3) == "86") {
                num = num.substring(3);
            } else {
                return null;
            }
        } else if (bit1.equals("0")) {
            num = num.substring(1);
        }
        int firstbit = Character.getNumericValue(num.charAt(0));
        if (num.length() < 7) {
            checknum = num;
        } else if (firstbit == 1) {
            int secondbit = Character.getNumericValue(num.charAt(1));
            if (secondbit == 0 || secondbit == 9) {
                checknum = num.substring(0, 2);
            } else {
                checknum = num.substring(0, 7);
            }
        } else if (firstbit == 2) {
            checknum = num.substring(0, 2);
        } else {
            checknum = num.substring(0, 3);
        }
        try {
            cur = db.rawQuery("select * from codearea where id==" + checknum, null);
            cur.moveToFirst();
            if (cur.getCount() > 0) {
                loc = cur.getString(cur.getColumnIndex("loc"));
                car = cur.getString(cur.getColumnIndex("car"));
                return loc + car;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}