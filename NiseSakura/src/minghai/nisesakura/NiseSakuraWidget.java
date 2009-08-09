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

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * さくらとうにゅうを表示します。起動は1個だけにしてね！
 * 
 */
public class NiseSakuraWidget extends AppWidgetProvider {
  // onUpdateは最初の1回しか呼ばれない、はず。
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
          int[] appWidgetIds) {
    context.startService(new Intent(context, NiseSakuraWidgetUpdateService.class));
  }
  
  // onDeletedは呼ばれない場合があるバグがあるので利用しません
  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    Log.d("TEST", "NiseSakura.onDeleted()");
    super.onDeleted(context, appWidgetIds);
  }

  @Override
  public void onDisabled(Context context) {
    Log.d("TEST", "NiseSakura.onDisabled()");
    super.onDisabled(context);
  }

  @Override
  public void onEnabled(Context context) {
    Log.d("TEST", "NiseSakura.onEnabled()");
    super.onEnabled(context);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("TEST", "NiseSakura.onReceive(): " + intent);
    super.onReceive(context, intent);
    // さくらがホーム画面から捨てられたらサービスも止める。複数起動を許可したらたぶん修正が必要。
    if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(intent.getAction())) {
      context.stopService(new Intent(context, NiseSakuraWidgetUpdateService.class));
    }
  }
}
