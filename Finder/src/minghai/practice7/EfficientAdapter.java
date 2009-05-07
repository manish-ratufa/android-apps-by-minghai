package minghai.practice7;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EfficientAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Bitmap mFolderIcon;
    private Bitmap mTextIcon;
    private Bitmap mImageIcon;
    private Bitmap mUnknownIcon;
    private Bitmap mVideoIcon;
    private Bitmap mAudioIcon;
    private File[] files;
    private String[] types;
	private HashMap<String, String> typeTable = new HashMap<String, String>();
	
    public EfficientAdapter(Context context, File[] files) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        
        this.files = files;
        this.types = new String[files.length];
        
        typeTable.put("txt", "text/plain");
        typeTable.put("jpg", "image/jpeg");
        typeTable.put("mp3", "audio/mpeg");
        typeTable.put("mp4", "video/mp4");
        typeTable.put("ogg", "audio/vorbis");
        
        for (int i = 0; i < files.length; i++) {
        	File f = files[i];
        	
        	if (f.isDirectory()) {
        		types[i] = "text/directory";
        		continue;
        	}
        	
    		String fname = f.getName();
    		int dot = fname.lastIndexOf('.');
    		if (dot != -1 && dot < fname.length() - 1) { // If there's extension
    			String extension = fname.substring(dot + 1);
    			String type = typeTable.get(extension);
    			types[i] = type;
    		} else {
    			types[i] = null;
    		}
        }

        // Icons bound to the rows.
        mFolderIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder_48x48);
        mTextIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.document_48x48);
        mImageIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.atlantikdesigner_48x48);
        mUnknownIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_48x48);
        mVideoIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_48x48);
        mAudioIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.sound_48x48);
    }

    /**
     * The number of items in the list is determined by the number of speeches
     * in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return files.length;
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficent to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        return position;
    }

    /**
     * Use the array index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.firstLine);
            holder.name.setBackgroundColor(Color.BLUE);

            holder.desc = (TextView) convertView.findViewById(R.id.secondLine);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        File file = files[position];
        Bitmap b;
        if (file.isDirectory()) {
        	b = mFolderIcon;
        } else {
        	String kind = null;
        	String type = types[position];
        	if (type != null) kind = type.substring(0, type.indexOf('/'));
        	Log.d("TEST", "kind: " + kind);
        	
        	if ("text".equals(kind)) {
        		b = mTextIcon;
        	} else if ("image".equals(kind)) {
        		b = mImageIcon;
        	} else if ("audio".equals(kind)) {
        		b = mAudioIcon;
        	} else if ("video".equals(kind)) {
        		b = mVideoIcon;
        	} else {
        		b = mUnknownIcon;
        	}
        }
        holder.icon.setImageBitmap(b);
        holder.name.setText(file.getName());
        holder.desc.setVisibility(View.GONE);

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView desc;
        ImageView icon;
    }

	public String getType(int position) {
		if (position < 0 || position >= types.length) return null;
		return types[position];
	}
}