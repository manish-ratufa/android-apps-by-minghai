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

package minghai.homeharuka;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class HomeHarukaWidget extends AppWidgetProvider {
  static public HashMap<String, Integer> lines = new HashMap<String, Integer>();
  
  {
    HashMap<String, Integer> h = lines;
    h.put("プロデューサーさん、\n好きです！", 1);
    h.put("プロデューサーさん、\n大好きです！", 13);
    h.put("プロデューサーさん、\nかっこいい！", 5);
    h.put("おつかれさまです、\nプロデューサーさん♪", 6);
    h.put("プロデューサーさんじゃなきゃダメなんです！", 1);
    h.put("さっすが、私のプロデューサーさんです♪", 8);
    h.put("頼りにしてますよ、\nプロデューサーさん！", 11);
    h.put("プロデューサーさん、\n今日もキマってますね！", 6);
    h.put("いつもありがとうございます、\nプロデューサーさん♪", 5);
    h.put("プロデューサーさん、\nこれからもずっと私と居てください！", 13);
    h.put("プロデューサーさんって、ひょっとして天才？", 12);
    h.put("すごーい！\nプロデューサーさん、\n尊敬しちゃいます♪", 9);
    h.put("プロデューサーさん、\n素敵です♪", 5);
    h.put("一緒に頑張りましょう、\nプロデューサーさん！", 8);
    h.put("プロデューサーさんって、\nおしゃれですよねー", 4);
    h.put("プロデューサーさん、\nすごいです！", 5);
    h.put("プロデューサーさんって\nすっごく頭がいいんですね♪", 8);
    h.put("プロデューサーさんのおかげで、ほんと助かってます！", 6);
    h.put("プロデューサー君、\nスーシーでもいく？", 7);
    h.put("もー！プロデューサーさん、\nしっかりしてください！", 3);
    h.put("たるんでるんじゃないですか、プロデューサーさん？", 10);
    h.put("だ、大丈夫ですか！\nプロデューサーさん！", 13);
    h.put("え？な、なんのことですか、プロデューサーさん？", 7);
    h.put("ご、ごめんなさい、\nプロデューサーさん…", 2);
    h.put("え？それってどういうことですか、\nプロデューサーさん？", 12);
    h.put("プロデューサーさん、私…", 1);
    h.put("プロデューサーさん…\nえへへ、何でもないです♪", 4);
    h.put("ふざけないでください、\nプロデューサーさん！", 3);
    h.put("わっ！お、脅かさないでくださいよ、\nプロデューサーさん…", 2);
    h.put("やりましたね、\nプロデューサーさん！", 5);
    h.put("頑張り過ぎないでくださいね、プロデューサーさん", 8);
    h.put("プロデューサーさん、\n無理しないでくださいね", 9);
    h.put("プロデューサーさん…", 1);
    h.put("や、やめてください、\nプロデューサーさん！", 2);
    h.put("ちゃんと聞いてください、\nプロデューサーさん！", 3);
    h.put("もー、仕方ないですね、\nプロデューサーさん", 1);
    h.put("プロデューサーさん、どうしましょう？", 12);
    h.put("楽しみですね、\nプロデューサーさん♪", 6);
    h.put("元気出してください、\nプロデューサーさん", 11);
    h.put("は、恥ずかしいから\nやめてください、\nプロデューサーさん…", 13);
    h.put("応援してます、\nプロデューサーさん♪", 8);
    h.put("あの、プロデューサーさん！\n私としたこと、\n覚えてます…？", 5);
  }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
    }
    
    public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);
            
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, HomeHarukaWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }

        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {
          Object[] keys = (lines.keySet().toArray());
          String message = (String)keys[(int)(System.currentTimeMillis() % keys.length)];
          int i = lines.get(message);
          int resID = getResources().getIdentifier("h" + i, "drawable", "minghai.homeharuka");
          RemoteViews updateViews = null;

          // Didn't find word of day, so show error message
          updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_message);
          updateViews.setTextViewText(R.id.message, message);
          updateViews.setImageViewResource(R.id.haruka, resID);

          return updateViews;
        }
        
        @Override
        public IBinder onBind(Intent intent) {
            // We don't need to bind to this service
            return null;
        }
    }
}
