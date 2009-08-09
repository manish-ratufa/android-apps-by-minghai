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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class NiseSakuraWidgetUpdater {
  
  static private NiseSakuraWidgetUpdater mUpdater = new NiseSakuraWidgetUpdater();
  
  static private Random rand = new Random(System.currentTimeMillis());
  
  static final private int HANDLE_MESSAGE = 1;
  static final private int STOP_ITERATION = 2;
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case HANDLE_MESSAGE:
        buildUpdate();
        if (mIndex < mCurrentMessage.length()) {
          sendEmptyMessageDelayed(HANDLE_MESSAGE, mWaitTime);
        } else {
          init();
          mCurrentMessage = getFirstMessage();
          if (mCurrentMessage == null)
            sendEmptyMessageDelayed(STOP_ITERATION, 3000);
          else
            sendEmptyMessageDelayed(HANDLE_MESSAGE, 3000);
        }

        break;
      case STOP_ITERATION:
        // 残りを表示して終了
        synchronized (isPlaying) {
          isPlaying = false;
        }

        updateLeftMessagesSize();
      }
    }
  };

  private NiseSakuraWidgetUpdater() {
    super();
  }
  
  static public NiseSakuraWidgetUpdater getInstance() {
    return mUpdater;
  }
  
  private PendingIntent mPendingIntent;
  private ComponentName thisWidget;
  private AppWidgetManager manager;
  
  public synchronized void setContext(Context context) {
    if (mContext == null) {
      mContext = context;

      // Create an Intent to launch ExampleActivity
      Intent intent = new Intent();
      //Bundle b = new Bundle();
      //b.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, i);
      //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i);

      intent.setAction(Intent.ACTION_MEDIA_BUTTON);
      intent.setType("text/sakura_script");
      //intent.putExtra("message", mCurrentMessage);
      //intent.putExtras(b);
      mPendingIntent = PendingIntent.getService(context, 0, intent, 0);

      thisWidget = new ComponentName(mContext, NiseSakuraWidget.class);
      manager = AppWidgetManager.getInstance(mContext);
    }
  }
  
  private ConcurrentLinkedQueue<String> mMessages = new ConcurrentLinkedQueue<String>();
  
  public synchronized void addMessages(String[] messages) {
    for (String s : messages) {
      mMessages.add(s);
    }
  }
  
  public synchronized void addMessage(String message) {
    mMessages.add(message);
    synchronized (isPlaying) {
      if (!isPlaying) updateLeftMessagesSize();
    }
  }
  
  private Boolean isPlaying = false;
  
  public synchronized void play() {
    
    synchronized (isPlaying) {
      if (isPlaying) return;
      isPlaying = true;
    }
    
    init();
    
    mCurrentMessage = getFirstMessage();
    if (mCurrentMessage == null) {
      mCurrentMessage = "";
      mHandler.sendEmptyMessage(STOP_ITERATION);
    } else {
      mHandler.sendEmptyMessage(HANDLE_MESSAGE);
    }
  }
  
  public void playOrPause() {
    synchronized (isPlaying) {
      if (isPlaying) {
        Log.d("TEST", "Stop player");
        mHandler.removeMessages(HANDLE_MESSAGE);
        mHandler.sendEmptyMessage(STOP_ITERATION);
        return;
      }
    }
    play();
  }
  
  protected void forceStop() {
    mHandler.removeMessages(HANDLE_MESSAGE);
    mHandler.removeMessages(STOP_ITERATION);
    isPlaying = false;
    init();
    mMessages.clear();
  }

  private String getFirstMessage() {
    String org = mMessages.poll();
    if (org != null) {
      org = org.replaceAll("%username", "ユーザーさん");
    }
    return org;
  }
  
  private int mIndex = 0;
  private int mSakuraSurface = 0;
  private int mUnyuuSurface  = 10;
  private String mCurrentMessage;
  private StringBuilder mSakuraMsg = new StringBuilder();
  private StringBuilder mUnyuuMsg  = new StringBuilder();
  private Context mContext;
  private long mWaitTime;
  private boolean isSakuraTurn = true;
  private boolean isQuickSession = false;
  private boolean isSyncSession = false;
  
  private void init() {
    mCurrentMessage = "";
    mIndex = 0;
    mSakuraSurface = 0;
    mUnyuuSurface = 10;
    mSakuraMsg.setLength(0);
    mUnyuuMsg.setLength(0);
    isSakuraTurn = true;
    isQuickSession = false;
    isSyncSession = false;
  }

  final private Pattern pnum = Pattern.compile("^\\[\\-?(\\d*)\\]");
  /**
   * Build a widget update to show the current Wiktionary
   * "Word of the day." Will block until the online API returns.
   */
  public void buildUpdate() {
    mWaitTime = 30; // reset to default: 20[msec]
    
    char c1;
    char c2;
    char c3;
    LOOP:
    while (true) {
      try {
        c1 = mCurrentMessage.charAt(mIndex++);

        if (c1 != '\\') {
          appendCharacter(c1);

          if (isQuickSession)
            continue;
          else
            break LOOP;
        }

        c2 = mCurrentMessage.charAt(mIndex++);
        //Log.d("TEST", "c2 = " + c2);
        switch (c2) {
        case 't': // time critical, but do nothing for this program
          break;
        case 'h': // Select Hontai
        case '0':
          if (!isSakuraTurn) {
            isSakuraTurn = true;
            mSakuraMsg.setLength(0);
          }
          break;
        case 'u': // Select Unyuu
        case '1':
          isSakuraTurn = false;
          mUnyuuMsg.setLength(0);
          break;
        case 'b': // baloon. Supports only "-1" to dismiss
          String leftStr = mCurrentMessage.substring(mIndex, mCurrentMessage.length());
          Matcher m = pnum.matcher(leftStr);
          if (m.find()) {
            mIndex += m.group().length();
            if (m.group(1).equals("-1")) {
              clearMessage();
            }
          }
          break;
        case 's': // surface
          leftStr = mCurrentMessage.substring(mIndex, mCurrentMessage.length());
          m = pnum.matcher(leftStr);
          if (m.find()) {
            mIndex += m.group().length();
            String numstr = m.group(1);
            int sn = Integer.parseInt(numstr);
            if (isSakuraTurn)
              mSakuraSurface = sn;
            else
              mUnyuuSurface  = sn;
          }
          
          break;
        case 'e': // えんいー
          mIndex = mCurrentMessage.length();
          break LOOP;
        case 'n': // new line
          appendCharacter('\n');
          break LOOP;
        case 'c': // clear
          clearMessage();
          break;
        case 'w':
          c3 = mCurrentMessage.charAt(mIndex++);
          if (Character.isDigit(c3)) {
            mWaitTime = (c3 - '0') * 50;
            break LOOP;
          } else 
            break;
        case '_':
          c3 = mCurrentMessage.charAt(mIndex++);
          if (c3 == 's') {
            isSyncSession = !isSyncSession;
          } else if (c3 == 'q') {
            isQuickSession = !isQuickSession;
          }
          break;
        case 'U': // URL is just ignored
          mIndex += 2;
          break;          
        default: // any unknown command will be ignored
          break;
        }
      } catch (StringIndexOutOfBoundsException e) {
        break LOOP;
      }
    }
    
    updateRemoteView();
  }

  private void clearMessage() {
    if (isSakuraTurn) {
      mSakuraMsg.setLength(0);
    } else {
      mUnyuuMsg.setLength(0);
    }
  }

  private void updateRemoteView() {
    // RemoteViewsは(少くとも1.5では)使い回しができない。以前の設定がクリアされないため。
    RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_message);
    //rv.setImageViewBitmap(R.id.sakura, getSurfaceBitmap(mSakuraSurface));
    //rv.setImageViewBitmap(R.id.unyuu, getSurfaceBitmap(mUnyuuSurface));

    Resources r = mContext.getResources();
    int resID = r.getIdentifier("surface" + mSakuraSurface, "drawable", "minghai.nisesakura");
    if (resID == 0) resID = R.drawable.surface0;
    rv.setImageViewResource(R.id.sakura, resID);
    resID = r.getIdentifier("surface" + mUnyuuSurface, "drawable", "minghai.nisesakura");
    if (resID == 0) resID = R.drawable.surface10;
    rv.setImageViewResource(R.id.unyuu, resID);
    
    if (mSakuraMsg.length() == 0) {
      rv.setViewVisibility(R.id.baloon0, View.INVISIBLE);
    } else {
      rv.setViewVisibility(R.id.baloon0, View.VISIBLE);
      rv.setTextViewText(R.id.sakura_message, mSakuraMsg.toString());
    }
    if (mUnyuuMsg.length() == 0) {
      rv.setViewVisibility(R.id.baloon1, View.INVISIBLE);
    } else {
      rv.setViewVisibility(R.id.baloon1, View.VISIBLE);
      rv.setTextViewText(R.id.unyuu_message, mUnyuuMsg.toString());
    }
    
    // うにゅうを押すと再開するように
    rv.setOnClickPendingIntent(R.id.unyuu, mPendingIntent);

    // Push update for this widget to the home screen
    manager.updateAppWidget(thisWidget, rv);
  }
  
  // ファイルからBitmapを作成するケース
  // 遅過ぎて使い物にならないので封印
  // RemoteViewsにBitmapを設定するとint[]の全てをIPCしているらしい？
  private Bitmap getSurfaceBitmap(int surfaceid) {
    Log.d("TEST", "surfaceid = " + surfaceid);
    Bitmap b;
    final String path = "/sdcard/nisesakura/plusAlpha/surface";
    final String suffix = ".png";
    File f = new File(path + surfaceid + suffix);
    if (!f.exists()) {
      f = new File(path + 0 + suffix);
    }
    FileInputStream is;
    try {
      is = new FileInputStream(f);
      b = BitmapFactory.decodeStream(is);
    } catch (FileNotFoundException e) {
      if (isSakuraTurn) {
        b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sakura_sound_only);
      } else {
        b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.kero_sound_only);
      }
    }
    
    Log.d("TEST", "b = " + b);
    return b;
  }

  private void appendCharacter(char c1) {
    if (isSyncSession) {
      mSakuraMsg.append(c1);
      mUnyuuMsg.append(c1);
    } else 
    if (isSakuraTurn) {
      mSakuraMsg.append(c1);
    } else {
      mUnyuuMsg.append(c1);
    }
  }

  void updateLeftMessagesSize() {
    init();
    int size = mMessages.size();
    if (size == 0) {
      mCurrentMessage = "\\h\\c\\_q\\s[" + rand.nextInt(9) + "]もうメッセージはありません";
    } else {
      mCurrentMessage = "\\h\\c\\_q\\s[" + rand.nextInt(9) + "]残り" + size + "個のメッセージがあるよ";
    }

    buildUpdate();
  }
}
