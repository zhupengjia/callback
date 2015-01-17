package com.example.callback;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import com.android.internal.telephony.ITelephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
    private ITelephony mITelephony;
    public static AudioManager am;
    SharedPreferences pref;
    static private String callbackdest = null;
    static private boolean callbacknow = false;
    static private boolean directcallnow = false;
    static private boolean openspeak = false;
    Toast mytoast = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        am = (AudioManager) context.getSystemService("audio");
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (phoneNumber.length() < 15) {
                callbacktask(context, phoneNumber);
            }
        } else {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> telec = TelephonyManager.class;
            Method getITelephonyMethod = null;
            try {
                getITelephonyMethod = telec.getDeclaredMethod("getITelephony", (Class[]) null);
                getITelephonyMethod.setAccessible(true);
                mITelephony = (ITelephony) getITelephonyMethod.invoke(telephony, (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            CallListener callListener = new CallListener(context);
            telephony.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void shownumber(Context context, String number, int Counttime) {
//		LayoutInflater inflater = LayoutInflater.from(context);
//		View layout = inflater.inflate(R.layout.float_toast, null);
        TextView text = new TextView(context);
        text.setTextColor(Color.GREEN);
        text.setBackgroundColor(Color.TRANSPARENT);
        text.setTextSize(25);
        text.setText(number);
        mytoast = new Toast(context);
        mytoast.setGravity(Gravity.TOP, 0, 40);
        mytoast.setView(text);
        CountDownTimer timer = new CountDownTimer(Counttime, 1000) {
            public void onTick(long millisUntilFinished) {
                mytoast.show();
            }

            public void onFinish() {
                mytoast.cancel();
            }
        };
        timer.start();
    }

    private void makeCall(Context context, String number) {
        number = new String(number.trim().replace(" ", "%20").replace("&", "%26")
                .replace(",", "%2c").replace("(", "%28").replace(")", "%29")
                .replace("!", "%21").replace("=", "%3D").replace("<", "%3C")
                .replace(">", "%3E").replace("#", "%23").replace("$", "%24")
                .replace("'", "%27").replace("*", "%2A").replace("-", "%2D")
                .replace(".", "%2E").replace("/", "%2F").replace(":", "%3A")
                .replace(";", "%3B").replace("?", "%3F").replace("@", "%40")
                .replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D")
                .replace("_", "%5F").replace("`", "%60").replace("{", "%7B")
                .replace("|", "%7C").replace("}", "%7D"));
        //Log.i("ZZDEBUG",number);
        Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(call);
    }

//	private void sendDTMF(char keycode) {  
//        try {  
//        	Log.i("ZZDEBUG","DTMFsending: "+keycode);
//        	//Process process = Runtime.getRuntime().exec("su");
//        	Log.i("ZZDEBUG","DTMF:rooted");
//            Class cls_phoneFactory = Class  
//                    .forName("com.android.internal.telephony.PhoneFactory");  
//            Log.i("ZZDEBUG","DTMF:class built");
//            Method method_getDefaultPhone = cls_phoneFactory.getDeclaredMethod(  
//                    "getDefaultPhone", null);
//            Log.i("ZZDEBUG","DTMF:method built");
//            method_getDefaultPhone.setAccessible(true);  
//            Log.i("ZZDEBUG","DTMF:method set accessible done");
//            Object obj_phone = method_getDefaultPhone.invoke(null);  
//            Log.i("ZZDEBUG","DTMF:object created");
//            Method method_sendDTMF = obj_phone.getClass().getDeclaredMethod(  
//                    "sendDtmf", char.class);  
//            Log.i("ZZDEBUG","DTMF:method sendDtmf done");
//            method_sendDTMF.invoke(obj_phone, keycode);  
//            Log.i("ZZDEBUG","DTMF:method sendDtmf invoked");
//        } catch (Exception e) {  
//            e.printStackTrace();
//            Log.i("ZZDEBUG","DTMF: "+e.getMessage());
//        }  
//    }  

    private void endCall() {
        try {
            mITelephony.silenceRinger();
            mITelephony.endCall();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void answerCall(Context context) {
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        // froyo and beyond trigger on buttonUp instead of buttonDown
        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    }

    private class webCallback extends webCallbackTask {
        private Context context = null;

        public webCallback(Context c) {
            context = c;
        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(context,result,Toast.LENGTH_LONG).show();
            shownumber(context, result, 3000);
        }
    }

    private class GetArea extends GetAreaTask {
        private Context context = null;

        public GetArea(String d, Context c) {
            super(d);
            context = c;
        }

        @Override
        protected void onPostExecute(String result) {
            shownumber(context, result, 15000);
        }
    }

//	class dealnum{
//		public dealnum(String numinput){
//			String nums[]=numinput.split(",|u007C| ");			
//		}
//	}

    class CallListener extends PhoneStateListener {
        String blacknum, autoreceive, autocallback, directcallnum, callbacknum;
        boolean blockprivatenum, autospeak;
        Pattern pblacknum, pautoreceive, pautocallback, pdirectcallnum, pcallbacknum;
        int ringsec, callwaitsec;
        Context context = null;

        public CallListener(Context c) {
            context = c;
            blacknum = pref.getString("blacknum", "").replaceAll(" |-", "");
            autoreceive = pref.getString("autoreceive", "").replaceAll(" |-", "");
            autocallback = pref.getString("autocallback", "").replaceAll(" |-", "");
            directcallnum = pref.getString("directcallnum", "").replaceAll(" ", "");
            callbacknum = pref.getString("callbacknum", "").replaceAll(" ", "");
            ringsec = Integer.parseInt(pref.getString("ringsec", "0"));
            callwaitsec = Integer.parseInt(pref.getString("callwaitsec", "0"));
            String blacknumreg = String.format("\\+?\\d{0,3}(%s)\\d*", blacknum);
            String autoreceivereg = String.format("\\+?\\d{0,3}(%s)\\d*", autoreceive);
            String autocallbackreg = String.format("\\+?\\d{0,3}(%s)\\d*", autocallback);
            String directcallnumreg = String.format("\\+?\\d{0,3}(%s)", directcallnum);
            String callbacknumreg = String.format("\\+?\\d{0,3}(%s)", callbacknum);
            blockprivatenum = pref.getBoolean("blockprivatenum", false);
            autospeak = pref.getBoolean("autospeak", true);
            pblacknum = Pattern.compile(blacknumreg);
            pautoreceive = Pattern.compile(autoreceivereg);
            pautocallback = Pattern.compile(autocallbackreg);
            pdirectcallnum = Pattern.compile(directcallnumreg);
            pcallbacknum = Pattern.compile(callbacknumreg);
        }

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // phone ringing
                    //Log.i("ZZDEBUG", "RINGING, number");
                    if (callbacknow) {
                        shownumber(context, callbackdest, 40000);
                        callbackdest = null;
                        callbacknow = false;
                    } else if (phoneNumber == null || phoneNumber.length() < 1) {
                        //block private call
                        if (blockprivatenum) {
                            endCall();
                        }
                    } else {
                        //block phone call
                        if (blacknum.length() > 0 && pblacknum.matcher(phoneNumber).matches()) {
                            endCall();
                        }
                        //auto call back
                        else if (autocallback.length() > 0 && pautocallback.matcher(phoneNumber).matches()) {
                            SystemClock.sleep(ringsec);
                            endCall();
                            SystemClock.sleep(callwaitsec);
                            makeCall(context, phoneNumber);
                            if (autospeak) {
                                openspeak = true;
                            }
                        }
                        //auto listen
                        else if ((autoreceive.length() > 0 && pautoreceive.matcher(phoneNumber).matches())) {
                            answerCall(context);
                            if (autospeak) {
                                openspeak = true;
                            }
                        } else {
                            GetArea area = new GetArea(pref.getString("areadatabaseloc", ""), context);
                            area.execute(phoneNumber);
//						String result="";
//						try {
//							result = area.execute(phoneNumber).get();
//						} catch (ExecutionException e) {
//						} catch (InterruptedException e) {
//						}
//						//addcalllog(context.getContentResolver(),phoneNumber,1,result);
                        }
                    }

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // active
                    if (openspeak) {
                        //Log.i("ZZDEBUG","speakphone on");
                        SystemClock.sleep(2000);
                        am.setSpeakerphoneOn(true);
                        openspeak = false;
                    }

                case TelephonyManager.CALL_STATE_IDLE:
                    // run when class initial and phone call ended, need detect flag
                    // from CALL_STATE_OFFHOOK
//	        	Log.i("ZZDEBUG", "IDLE1"+phoneNumber);
//	        	Log.i("ZZDEBUG", "IDLE2"+callbackdest);
//	    		Log.i("ZZDEBUG", "IDLE3"+callbacknow);
                    //callback
                    if (directcallnow && directcallnum.length() > 0) {
                        //Log.i("ZZDEBUG", "IDLE"+directcallnum);
                        if (callbackdest.length() > 0) {

                            //Log.i("ZZDEBUG","try to dial callback "+callbackdest);
                            //SystemClock.sleep(10000);
                            //Log.i("ZZDEBUG","sleep done");
                            //for (char a:callbackdest.toCharArray()){
                            //sendDTMF(a);
                            //}
                            shownumber(context, callbackdest, 40000);
                            callbackdest = null;
                            directcallnow = false;
                        }
                    }
            }
        }
    }

    private void callbacktask(Context context, String phoneNumber) {
        //Log.i("ZZDEBUG", "call OUT:"+phoneNumber);
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String directcallnum = pref.getString("directcallnum", "").replaceAll(" ", "");
        String callbacknum = pref.getString("callbacknum", "").replaceAll(" ", "");
        String excludenum = pref.getString("callexclude", "").replaceAll(" |-", "");
        String includenum = pref.getString("callinclude", "").replaceAll(" |-", "");
        if (excludenum.length() < 1) {
            excludenum = directcallnum + "|" + callbacknum;
        } else {
            excludenum = excludenum + "|" + directcallnum + "|" + callbacknum;
        }
        String exregEx = String.format("\\+?\\d{0,3}(%s)\\d*", excludenum);
        String inregEx = String.format("\\+?\\d{0,3}(%s)\\d*", includenum);
        boolean webcallon = pref.getBoolean("webcallon", true);
        boolean callbackon = pref.getBoolean("callbackon", true);
        boolean directcallbackon = pref.getBoolean("directcallbackon", true);
        Pattern exp = Pattern.compile(exregEx);
        Pattern inp = Pattern.compile(inregEx);
        if ((includenum.length() > 0 && inp.matcher(phoneNumber).matches()) ||
                excludenum.length() < 1 ||
                !exp.matcher(phoneNumber).matches()) {
            if (webcallon && networkInfo != null && networkInfo.isConnected()) {
                //web callback if network avail
                Log.i("ZZDEBUG", "cancelling call");
                setResultData(null); //cancel call
                //return;
                addcalllog(context.getContentResolver(), phoneNumber, 2);
                String callurlformat = pref.getString("callurlapi", "");
                String username = pref.getString("username", "");
                String callurl = String.format(callurlformat, username, phoneNumber);
                webCallback task = new webCallback(context);
                task.execute(callurl);
            } else if (directcallnum.length() > 0 && directcallbackon) {
                setResultData(null); //cancel call
                addcalllog(context.getContentResolver(), phoneNumber, 2);
                callbackdest = phoneNumber + "#";
                directcallnow = true;
                makeCall(context, directcallnum);
            } else if (callbacknum.length() > 0 && callbackon) {
                setResultData(null); //cancel call
                addcalllog(context.getContentResolver(), phoneNumber, 2);
                callbackdest = phoneNumber + "#";
                callbacknow = true;
                makeCall(context, callbacknum);
            }
        }
    }

    private void addcalllog(ContentResolver contentResolver, String strNum, int calltype) {
        while (strNum.contains("-")) {
            strNum = strNum.substring(0, strNum.indexOf('-')) + strNum.substring(strNum.indexOf('-') + 1, strNum.length());
        }
        ContentValues values = new ContentValues();
        //Log.i("ZZDEBUG",CallLog.Calls.CACHED_NAME);
        values.put(CallLog.Calls.NUMBER, strNum);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, 0);
        values.put(CallLog.Calls.TYPE, calltype);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        if (null != contentResolver) {
            contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
        }
    }

}
