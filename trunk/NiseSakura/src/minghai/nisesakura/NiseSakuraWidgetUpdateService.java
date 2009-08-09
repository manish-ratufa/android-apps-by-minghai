/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package minghai.nisesakura;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import minghai.nisesakura.SimpleTwitterHelper.ApiException;
import minghai.nisesakura.SimpleTwitterHelper.ParseException;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NiseSakuraWidgetUpdateService extends Service {

  @Override
  public void onCreate() {
    super.onCreate();
    // 3分後からHTTP_TASKを開始する
    mHandler.sendEmptyMessageDelayed(HTTP_TASK_START, 180000);
  }
  
  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    String msg = intent.getStringExtra("message"); 
    NiseSakuraWidgetUpdater updater = NiseSakuraWidgetUpdater.getInstance();
    
    updater.setContext(this);

    if (msg != null) updater.addMessage(msg);
    
    String action = intent.getAction();
    if (action == null) {
      // Right after onCreate
      int i = loadExecutionCount(this);
      Log.d("TEST", "onStart: i = " + i);
      if (i == Integer.MIN_VALUE) {
        openingEvent(updater);
        updater.play();
        saveExecutionCount(this, 1);
      } else {
        saveExecutionCount(this, ++i);
        updater.updateLeftMessagesSize();
      }
    } else if (Intent.ACTION_INSERT.equals(action)) {
      // Nothing to do
    } else if (Intent.ACTION_VIEW.equals(action)) {
      updater.play();
    } else if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
      updater.playOrPause();
    }
  }

  private void openingEvent(NiseSakuraWidgetUpdater updater) {
    try {
      BufferedReader br = new BufferedReader(
          new InputStreamReader(getResources().openRawResource(R.raw.event_opening_talk), "UTF-8"));

      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.equals("") || line.startsWith("#")) continue;
        updater.addMessage(line);
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (NotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    // There's no need to implement this currently.
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    NiseSakuraWidgetUpdater updater = NiseSakuraWidgetUpdater.getInstance();
    updater.forceStop();
    
    mHandler.removeMessages(HTTP_TASK_START);
  }
  
  private static final String PREFS_NAME = "minghai.nisesakura.NiseSakuraWidget";
  private static final String EXECUTION_COUNTER_KEY = "execution_counter";
  
  // Write the prefix to the SharedPreferences object for this widget
  static void saveExecutionCount(Context context, int executionCount) {
    SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
    prefs.putInt(EXECUTION_COUNTER_KEY, executionCount);
    prefs.commit();
  }

  // Read the prefix from the SharedPreferences object for this widget.
  // If there is no preference saved, get the default from a resource
  static int loadExecutionCount(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    int count = prefs.getInt(EXECUTION_COUNTER_KEY, Integer.MIN_VALUE);
    return count;
  }
  
  static final private int HTTP_TASK_START = 1;
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case HTTP_TASK_START:
        new TwitterTask().execute(NiseSakuraWidgetUpdateService.this);
        sendEmptyMessageDelayed(HTTP_TASK_START, 600000);
        break;
      }
    }
  };
  
  private class TwitterTask extends AsyncTask<Context, String, String> {
    private NiseSakuraWidgetUpdater mUpdater;

    @Override
    protected void onPreExecute() {
      mUpdater = NiseSakuraWidgetUpdater.getInstance();
    }

    @Override
    protected String doInBackground(Context... args) {
      long start = System.currentTimeMillis();
      LinkedList<String> pageContent;
      try {
        // Try querying the Twitter Search API for today's twit
        SimpleTwitterHelper.prepareUserAgent(args[0]);
        pageContent = SimpleTwitterHelper.getPageContent();
        Log.d("TEST", "results.length() = " + pageContent.size());
        
        for (String s : pageContent) {
          mUpdater.addMessage(s);
        }
        
        // Try querying the Bottle log API for today's bottle
        SimpleBottleHelper.prepareUserAgent(args[0]);
        pageContent = SimpleBottleHelper.getPageContent();
        Log.d("TEST", "bottle.length() = " + pageContent.size());
        
        for (String s : pageContent) {
          mUpdater.addMessage(s);
        }
      } catch (ApiException e) {
        Log.e("TEST", "Couldn't contact API", e);
      } catch (ParseException e) {
        Log.e("TEST", "Couldn't parse API response", e);
        //publishProgress(e.toString());
      }

      Log.d("TEST", "time = " + (System.currentTimeMillis() - start) + " [ms]");
      return "End of conversions";
    }

    @Override
    protected void onProgressUpdate(String... args) {
      //mEditText.append(args[0]);
    }

    @Override
    protected void onPostExecute(String result) {
      //mEditText.append(result);
    }

  }
}
