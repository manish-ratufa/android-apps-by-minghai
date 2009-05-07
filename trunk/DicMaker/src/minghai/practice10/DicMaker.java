package minghai.practice10;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class DicMaker extends Activity {
  static String BTREE_NAME = "skk_dict";
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    RecordManager recman;
    long          recid;
    Tuple         tuple = new Tuple();
    TupleBrowser  browser;
    BTree         tree;
    Properties    props;

    props = new Properties();

    try {
        // open database and setup an object cache
        recman = RecordManagerFactory.createRecordManager("/sdcard/skk_dict_btree", props );

        // try to reload an existing B+Tree
        recid = recman.getNamedObject( BTREE_NAME );
        if ( recid != 0 ) {
            tree = BTree.load( recman, recid );
            System.out.println( "Reloaded existing BTree with " + tree.size()
                                + " famous people." );
        } else {
            // create a new B+Tree data structure and use a StringComparator
            // to order the records based on people's name.
            tree = BTree.createInstance( recman, new StringComparator() );
            recman.setNamedObject( BTREE_NAME, tree.getRecid() );
            System.out.println( "Created a new empty BTree" );
        }

        // insert people with their respective occupation
        System.out.println();
        FileReader fr = null;
        BufferedReader br = null;
        try {
                fr = new FileReader("/sdcard/skki1_0u.dic.utf8");
                br = new BufferedReader(fr);

                int c = 0;
                ContentValues cv = new ContentValues();
                Log.d("TEST", "ファイル入力開始");
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                  
                }

        // make the data persistent in the database
        recman.commit();

        // show list of people with their occupation
        System.out.println();
        System.out.println( "Person                   Occupation       " );
        System.out.println( "------------------       ------------------" );

        // traverse people in order
        browser = tree.browse();
        while ( browser.getNext( tuple ) ) {
            print( tuple );
        }

        // traverse people in reverse order
        System.out.println();
        System.out.println( "Reverse order:" );
        browser = tree.browse( null ); // position browser at end of the list

        while ( browser.getPrevious( tuple ) ) {
            print( tuple );
        }



        // display people whose name start with PREFIX range
        System.out.println();
        System.out.println( "All people whose name start with '" + PREFIX + "':" );

        browser = tree.browse( PREFIX );
        while ( browser.getNext( tuple ) ) {
            String key = (String) tuple.getKey();
            if ( key.startsWith( PREFIX ) ) {
                print( tuple );
            } else {
                break;
            }
        }

    } catch ( Exception except ) {
        except.printStackTrace();
    }
  }
}