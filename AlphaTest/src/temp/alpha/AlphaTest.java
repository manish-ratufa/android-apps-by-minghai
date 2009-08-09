package temp.alpha;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

public class AlphaTest extends Activity {
  static String DOWNLOAD_DIR = "/sdcard/download";
  static String GHOST_DIR = "/sdcard/nisesakura/ghost";
  static String PLUS_ALPHA = "/sdcard/nisesakura/plusAlpha/";
  private EditText mEditText;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mEditText = new EditText(this);
    setContentView(mEditText);
  }

  @Override
  protected void onStart() {
    super.onStart();

    new AlphaTask().execute(GHOST_DIR);
  }

  private class AlphaTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... args) {
      File dir = new File(args[0]);
      if (!dir.isDirectory())
        return "Directory not found: " + args[0];

      long start = System.currentTimeMillis();

      File[] files = dir.listFiles();
      for (File f : files) {
        String name = f.getName();
        if (!name.endsWith(".png"))
          continue;

        InputStream is;
        try {
          is = new FileInputStream(f.getPath());
          Bitmap b = BitmapFactory.decodeStream(is);
          int w = b.getWidth();
          int h = b.getHeight();
          int size = w * h;
          int[] buf = new int[size];
          Bitmap b2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
          b.getPixels(buf, 0, w, 0, 0, w, h);
          int c = buf[0];
          for (int i = 0; i < size; i++) {
            // Log.d("TEST", "i = " + i + " buf[i] = " + buf[i]);
            if (c == buf[i])
              buf[i] = 0;
          }
          b2.setPixels(buf, 0, w, 0, 0, w, h);
          FileOutputStream fos = new FileOutputStream(PLUS_ALPHA + name);
          b2.compress(Bitmap.CompressFormat.PNG, 100, fos);
          publishProgress("Converted: " + f.getPath() + "\n");
        } catch (FileNotFoundException e) {
          publishProgress(e.toString());
        }
      }

      Log.d("TEST", "time = " + (System.currentTimeMillis() - start) + " [ms]");
      return "End of conversions";
    }

    @Override
    protected void onProgressUpdate(String... args) {
      mEditText.append(args[0]);
    }

    @Override
    protected void onPostExecute(String result) {
      mEditText.append(result);
    }

  }
}