/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;

public class NiseSakuraWidgetConfigure extends Activity {
  /**
   * The configuration screen for the ExampleAppWidgetProvider widget sample.
   */
  static final String TAG = "ExampleAppWidgetConfigure";

  private static final String PREFS_NAME = "minghai.nisesakura.NiseSakuraWidget";
  private static final String SAKURA_MESSAGE_KEY = "sakura_message_";
  private static final String  UNYUU_MESSAGE_KEY = "unyuu_message_";  

  int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
  EditText mSakuraEditText;
  EditText mUnyuuEditText;
  
  View.OnClickListener mOnClickListener = new View.OnClickListener() {
    public void onClick(View v) {
      // When the button is clicked, save the string in our prefs and return
      // that they
      // clicked OK.
      String sakura_message = mSakuraEditText.getText().toString();
      String unyuu_message  = mUnyuuEditText .getText().toString();
      saveSakuraMessage(NiseSakuraWidgetConfigure.this, mAppWidgetId, sakura_message);
      saveUnyuuMessage(NiseSakuraWidgetConfigure.this, mAppWidgetId, unyuu_message);

      Context context = NiseSakuraWidgetConfigure.this;
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_message);
      views.setTextViewText(R.id.sakura_message, sakura_message);
      views.setTextViewText(R.id.unyuu_message,  unyuu_message);
      
      // Create an Intent to launch ExampleActivity
      Intent intent = new Intent(context, NiseSakuraWidgetConfigure.class);
      Bundle b = new Bundle();
      b.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
      //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i);
      intent.putExtras(b);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

      // Get the layout for the App Widget and attach an on-click listener to the button
      views.setOnClickPendingIntent(R.id.unyuu, pendingIntent);
      appWidgetManager.updateAppWidget(mAppWidgetId, views);
      
      // Make sure we pass back the original appWidgetId
      Intent resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
      setResult(RESULT_OK, resultValue);
      finish();
    }
  };

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    // Set the result to CANCELED. This will cause the widget host to cancel
    // out of the widget placement if they press the back button.
    setResult(RESULT_CANCELED);

    // Set the view layout resource to use.
    setContentView(R.layout.appwidget_configure);

    // Find the EditText
    mSakuraEditText = (EditText) findViewById(R.id.sakura_edittext);
    mUnyuuEditText  = (EditText) findViewById(R.id.unyuu_edittext);

    // Bind the action for the save button.
    Log.d("TEST", "id = " + findViewById(R.id.save_button) + " mOnClickListener  = " + mOnClickListener);
    findViewById(R.id.save_button).setOnClickListener(mOnClickListener);

    // Find the widget id from the intent.
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // If they gave us an intent without the widget id, just bail.
    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      Log.d("LUNA", "Exec Configure failed!");
      finish();
    }

    mSakuraEditText.setText(loadSakuraMessage(this, mAppWidgetId));
    mUnyuuEditText. setText(loadUnyuuMessage (this, mAppWidgetId));
  }

  // Write the prefix to the SharedPreferences object for this widget
  static void saveSakuraMessage(Context context, int appWidgetId, String text) {
    SharedPreferences.Editor prefs = context
        .getSharedPreferences(PREFS_NAME, 0).edit();
    prefs.putString(SAKURA_MESSAGE_KEY + appWidgetId, text);
    prefs.commit();
  }

  // Read the prefix from the SharedPreferences object for this widget.
  // If there is no preference saved, get the default from a resource
  static String loadSakuraMessage(Context context, int appWidgetId) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    String prefix = prefs.getString(SAKURA_MESSAGE_KEY + appWidgetId, null);
    if (prefix != null) {
      return prefix;
    } else {
      return "逝ってヨシ！";
    }
  }
  

  // Write the prefix to the SharedPreferences object for this widget
  static void saveUnyuuMessage(Context context, int appWidgetId, String text) {
    SharedPreferences.Editor prefs = context
        .getSharedPreferences(PREFS_NAME, 0).edit();
    prefs.putString(UNYUU_MESSAGE_KEY + appWidgetId, text);
    prefs.commit();
  }

  // Read the prefix from the SharedPreferences object for this widget.
  // If there is no preference saved, get the default from a resource
  static String loadUnyuuMessage(Context context, int appWidgetId) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    String prefix = prefs.getString(UNYUU_MESSAGE_KEY + appWidgetId, null);
    if (prefix != null) {
      return prefix;
    } else {
      return "逝ってヨシ！";
    }
  }

  static void deleteTitlePref(Context context, int appWidgetId) {
  }

  static void loadAllTitlePrefs(Context context,
      ArrayList<Integer> appWidgetIds, ArrayList<String> texts) {
  }
}
