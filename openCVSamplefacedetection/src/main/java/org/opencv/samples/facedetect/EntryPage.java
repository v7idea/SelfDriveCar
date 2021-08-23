package org.opencv.samples.facedetect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.v7idea.Activity.BaseActivity;
import com.v7idea.tool.Constants;

public class EntryPage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);

        Handler handler = new Handler();
        handler.postDelayed(delayRunnable, Constants.ENTRY_PAGE_DELAY_TIME);
    }

    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(currentActivity, ChooseModeActivity.class);
            startActivity(intent);
            finish();
            setTurnInNextPageAnimation(currentActivity);
        }
    };
}
