package net.thinktodo.parsechatdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.thinktodo.parsechatdemo.utils.ParseChat2Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by thong.nguyen on 9/11/2015.
 */
public class MainActivity extends BaseActivity {
    //Variable of view
    ImageView imgSend;
    RelativeLayout laySend;
    EditText edMessage;
    ProgressBar pbWaiting;
    TextView tvJson;
    //Variable
    ParseChat2Json parseChat2Json=new ParseChat2Json();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        imgSend= (ImageView) findViewById(R.id.img_send);
        laySend= (RelativeLayout) findViewById(R.id.lay_send);
        edMessage= (EditText) findViewById(R.id.ed_message);
        pbWaiting= (ProgressBar) findViewById(R.id.pb_waiting);
        tvJson= (TextView) findViewById(R.id.tv_json);
        laySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message=edMessage.getText().toString();
                if(message.equals("")){
                    edMessage.setError(getString(R.string.activity_main_please_input_your_message));
                }else{
                    new ParseChatAsyncTask(MainActivity.this).execute(message);
                }
            }
        });
        String chat="@Thong, Olympics are starting soon; http://www.nbcolympics.com  http://www.google.com  http://www.yahoo.com  http://www.nbcolympics.com";
        edMessage.setText(chat);
    }


    @Override
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
        if (id == R.id.action_settings) {
            return true;
        }else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public class ParseChatAsyncTask extends AsyncTask<String,String,String>{
        WeakReference<Activity> baseActivityWeakReference;

        public ParseChatAsyncTask(Activity activity) {
            baseActivityWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        protected void onPreExecute() {
            imgSend.setVisibility(View.GONE);
            pbWaiting.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONObject result=parseChat2Json.parseSyncChat2Json(strings[0]);
            try {
                return result.toString(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            //Check activity was destroyed or not
            if(baseActivityWeakReference.get()!=null) {
                imgSend.setVisibility(View.VISIBLE);
                pbWaiting.setVisibility(View.GONE);
                edMessage.setText("");
                tvJson.setText(s);
            }else{
                //Activity was destroyed so do nothing
            }
        }
    }
}
