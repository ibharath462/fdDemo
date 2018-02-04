package f1nd.initial.bharath.osdictdemo1;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class serviceHandler extends Service {

    private static final String TAG = "BackgroundSoundService";
    ClipboardManager clipBoard ;
    static boolean bHasClipChangedListener = false;
    private WindowManager wm;
    TextView m;
    EditText w = null;
    boolean meaningFlag = false;
    WindowManager.LayoutParams p;
    View myView;
    ListView lv;
    JSONObject sWord;
    boolean isFABOpen = false;
    FloatingActionButton mainFAB , close , hide;
    data databaseHelper;



    ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {

            ClipData clipData = clipBoard.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);
            String word = item.getText().toString();
            word = word.trim();
            int wordLength = 0;
            if (!word.isEmpty())
                wordLength = word.split("\\s+").length;
            Log.d("Database22", "ML" + wordLength);
            //Checking of we neeed a sentence parser
            if(wordLength == 1){
                //Single word...
            }else{
                //Need a parser pie...
            }
            databaseHelper=new data(serviceHandler.this);
            word = word.toLowerCase();
            word = word.substring(0, 1).toUpperCase() + word.substring(1);
            String meaning = databaseHelper.getMeaning(word);
            if(meaning != null){
                meaning = meaning.replaceAll("^ +| +$|( )+", " ");
                meaning = meaning.replace("\n", "").replace("\r", "");
                int meaningLength = 0;
                meaningLength = meaning.split("\\s+").length;
                Log.d("Database22", "ML" + meaningLength);
                //Checking if we need a popUp of make a Toast....
                if(meaningLength < 15){
                    //We can toast it...
                    Log.d("Database22", "Can finish in Toast");
                    Toast.makeText(getApplicationContext(),"" + meaning,Toast.LENGTH_LONG).show();
                }else{
                    //Need a popUp baby.....
                    Log.d("Database22", "Need a popup baby....");
                    w = (EditText) myView.findViewById(R.id.word);
                    m = (TextView)myView.findViewById(R.id.meaning);
                    mainFAB = (FloatingActionButton)myView.findViewById(R.id.mainFAB);
                    close = (FloatingActionButton)myView.findViewById(R.id.c);
                    hide = (FloatingActionButton)myView.findViewById(R.id.hide);

                    close.setVisibility(View.VISIBLE);
                    mainFAB.setVisibility(View.VISIBLE);
                    hide.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.INVISIBLE);

                    setListeners();

                    m.setText(meaning);
                    m.setVisibility(View.VISIBLE);
                    w.setEnabled(false);
                    w.setTextSize(28);
                    w.setText(word);
                    wm.addView(myView, p);
                }
            }
            else{
                Log.d("Database22", "Word not found hai...");
                Toast.makeText(getApplicationContext(),"Word not found",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void RegPrimaryClipChanged(){
        if(!bHasClipChangedListener){
            clipBoard.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
            bHasClipChangedListener = true;
        }
    }

    @Override
    public void onCreate() {
        clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        RegPrimaryClipChanged();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        p=new WindowManager.LayoutParams(1000,1000, WindowManager.LayoutParams.TYPE_PHONE,  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSPARENT);
        LayoutInflater fac=LayoutInflater.from(serviceHandler.this);
        myView = fac.inflate(R.layout.popup, null);
        lv = (ListView)myView.findViewById(R.id.lv);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String mmmmMeaning = sWord.getString(lv.getItemAtPosition(i).toString());
                    mmmmMeaning = mmmmMeaning.replaceAll("^ +| +$|( )+", " ");
                    mmmmMeaning = mmmmMeaning.replace("\n", "").replace("\r", "");
                    m.setText(mmmmMeaning);
                    meaningFlag = true;
                    w.setEnabled(false);
                    w.setTextSize(28);
                    w.setText(lv.getItemAtPosition(i).toString());
                    Log.d("Database22", "" + sWord.getString(lv.getItemAtPosition(i).toString()));
                    m.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.INVISIBLE);
                    hide.setVisibility(View.VISIBLE);

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
        super.onCreate();
    }

    private void UnRegPrimaryClipChanged(){
        if(bHasClipChangedListener){
            clipBoard.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
            bHasClipChangedListener = false;
        }
    }

    public serviceHandler() {
    }

    public void setListeners(){
        mainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.removeView(myView);
            }
        });

        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lv.setVisibility(View.VISIBLE);
                m.setVisibility(View.INVISIBLE);
                closeFABMenu();
                w.setText("");
                meaningFlag = false;
                w.setEnabled(true);
            }
        });

        w.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(meaningFlag == false){
                    sWord = databaseHelper.search(w.getText().toString());
                    final ArrayList<String> words = new ArrayList<String>();
                    Iterator<String> iter = sWord.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        words.add(key);
                        try {
                            Object value = sWord.get(key);
                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                    ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(serviceHandler.this, android.R.layout.simple_expandable_list_item_1, words);
                    lv.setAdapter(mArrayAdapter);
                }
            }

        });
    }


    private void showFABMenu(){
        isFABOpen=true;
        mainFAB.animate().alpha((float) 0.7);
        close.animate().alpha((float) 1);
        hide.animate().alpha((float) 1);
        close.animate().translationX(-getResources().getDimension(R.dimen.standard_105));
        hide.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        mainFAB.animate().alpha((float) 1.0);
        close.animate().translationX(0);
        close.animate().alpha((float) 0);
        hide.animate().alpha((float) 0);
        hide.animate().translationY(0);
    }





    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG, "onBind()" );
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getApplicationContext(),"Started service",Toast.LENGTH_SHORT).show();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(serviceHandler.this);
        localBroadcastManager.sendBroadcast(new Intent("com.OSdict.action.close"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        UnRegPrimaryClipChanged();
        super.onDestroy();
    }


}
