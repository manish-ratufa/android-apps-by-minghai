package minghai.practice10;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class DicMaker extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/sdcard/skk_dict.db", null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
        
    	FileReader     fr = null;
    	BufferedReader br = null;
		try {
			fr = new FileReader("/sdcard/skki1_0u.dic.utf8");
			br = new BufferedReader(fr);

			int c = 0;
			ContentValues cv = new ContentValues();
			Log.d("TEST", "Ç±ÇÒÇ…ÇøÇÕÅAÇ±ÇÒÇ…ÇøÇÕÅI");
			for (String line = br.readLine(); line != null; line = br.readLine()) {

				if (line.startsWith(";;")) continue;
				
				int idx = line.indexOf(' ');
				if (idx == -1) continue;
				String key = line.substring(0, idx);
				String value = line.substring(idx + 1, line.length());
//				if (key.startsWith("a")) Log.d("TEST", "key = " + key + " value = " + line.substring(idx + 1, line.length()));
				cv.clear();
				cv.put("key", key);
				cv.put("value", value);
				db.insert("dictionary", "null", cv);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (fr != null)
				try {fr.close();} catch (IOException e) {}
			if (br != null)
				try {br.close();} catch (IOException e) {}
		}

	  	Log.d("TEST", "END  :" + System.currentTimeMillis());
    }
}