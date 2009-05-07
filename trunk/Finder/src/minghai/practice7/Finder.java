package minghai.practice7;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

public class Finder extends ListActivity {

	private File   cdr;
	private File[] files;
	private EfficientAdapter ea;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Uri uri;
        
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default directory.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            uri = Uri.parse("file://" + "/");
        } else {
        	uri = intent.getData();
        }
        
        cdr = new File(uri.getPath());
        files = cdr.listFiles();
        Arrays.sort(files);

        // Print current directory in title field
        setTitle(uri.getPath());
        
        ea = new EfficientAdapter(this, files);
        setListAdapter(ea);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	File file = files[position];
    	Log.d("TEST", "path: " + file.getPath());
    	if (file.isDirectory()) {
    		Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.setDataAndType(Uri.parse("file://" + file.getPath()), "text/directory");
    		Log.d("TEST", "intent: " + intent);
    		startActivity(intent);
    	} else {
    		String type = ea.getType(position);
    		if (type != null) {
    			Intent intent = new Intent(Intent.ACTION_VIEW);
    			intent.setDataAndType(Uri.parse("file://" + file.getPath()), type);
    			startActivity(intent);
    		} else {
    			// If it gets to here, it must be unknown file type.
    			Toast.makeText(getApplication(), "Unknown file type",
    					Toast.LENGTH_LONG).show();
    		}
    	}
	}
    
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // This is the selected item.
        	int position = getSelectedItemPosition();
        	if (position == -1) return true; // No one selected
        	
            String type = ea.getType(position);
            Uri uri = Uri.parse("file://" + files[position].getPath());

            // Build menu...  always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_VIEW);
            specifics[0].setDataAndType(uri, type);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                    items);

            // Give a shortcut to the edit action.
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
	}

}