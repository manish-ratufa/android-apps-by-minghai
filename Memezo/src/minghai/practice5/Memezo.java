package minghai.practice5;

import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Memezo extends Activity {

	LinkedList<Integer> clusterList = new LinkedList<Integer>();
    TreeMap<Integer, int[]> clusterMap = new TreeMap<Integer, int[]>();
    static final private int MAZE_WIDTH  = 35;
    static final private int MAZE_HEIGHT = 50;
    private Cell[] cells = new Cell[MAZE_WIDTH * MAZE_HEIGHT];
    
    private SurfaceHolder mSurfaceHolder;
    private MyThread mThread;
    private final Random mRand = new Random(System.currentTimeMillis());
    private FieldView mFieldView;
    private TextView mStatusText;
	private int mScreenWidth;
	private int mScreenHeight;
    
    private class Cell {
        public boolean right_side = true;
        public boolean below_side = true;
        public int cluster_number;
        public boolean isSameCluster(int num) {
            return cluster_number == num;
        }
    }
    
    enum Direction {
        ABOVE, BELOW, RIGHT, LEFT;
    }
    
    class MyThread extends Thread {
        private boolean mRun = true;
        private Canvas c;
        
        @Override
        public void run() {
            Log.d("TEST", "mScreenWidth = " + mScreenWidth);
            Log.d("TEST", "mScreenHeight = " + mScreenHeight);          
            Bitmap bm = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
            
            while (mRun) {
                Canvas c = null;

                //long currentTime = System.currentTimeMillis();
                c = mSurfaceHolder.lockCanvas(new Rect(0, 0, mScreenWidth, mScreenHeight));
                synchronized (mSurfaceHolder) {
                    doDraw(c);
                }
                mSurfaceHolder.unlockCanvasAndPost(c);
                Log.d("TEST", "First onDraw finished");
                // 2nd
                c = mSurfaceHolder.lockCanvas(new Rect(0, 0, mScreenWidth, mScreenHeight));
                synchronized (mSurfaceHolder) {
                    doDraw(c);
                }
                mSurfaceHolder.unlockCanvasAndPost(c);
                
                for (int size = clusterList.size(); size > 1; size = clusterList.size()) {
                    int i = clusterList.get(mRand.nextInt(size - 1) + 1);  // This is cell address
                    while (true) {                
                        int next = make_maze(i);

                        if (next == -1) break;
                        i = next;
                    }
                }
                //clusterList.clear();
                //clusterMap.clear();
                mRun = false;
                
                Canvas rc = mSurfaceHolder.lockCanvas();
                rc.drawBitmap(bm, 0, 0, null);
                mSurfaceHolder.unlockCanvasAndPost(rc);

            }
        }
        

        /*
         * ランダムにセルをclusterLlstから選ぶ
         * セルの上下左右と繋ぐ
         * クラスターを連結する。結果的にclisterList、clusterMapのsizeが縮む
         * クラスターサイズが1になるまで繰り返す
         */
        public int make_maze(int i) {

            //Log.d("TEST", "i = " + i);
            Cell cell = cells[i];
            boolean canAbove;
            boolean canBelow;
            boolean canRight;
            boolean canLeft;

            int ridx = i + 1;
            int lidx = i - 1;
            int aidx = i - MAZE_WIDTH;
            int bidx = i + MAZE_WIDTH;

            // WATCH OUT: if you forget to check isWall before, cells[idx] might throw IndexOutOfBounds
            int ccn = cell.cluster_number; // Current Cluster Number
            canRight = !rightIsWall(i) && !cells[ridx].isSameCluster(ccn);
            canLeft  = !leftIsWall(i)  && !cells[lidx].isSameCluster(ccn);
            canAbove = !aboveIsWall(i) && !cells[aidx].isSameCluster(ccn);
            canBelow = !belowIsWall(i) && !cells[bidx].isSameCluster(ccn);

            LinkedList<Direction> directionsToGo = new LinkedList<Direction>();
            //directionsToGo.clear();
            if (canRight)  directionsToGo.add(Direction.RIGHT);
            if (canLeft)   directionsToGo.add(Direction.LEFT);
            if (canAbove)  directionsToGo.add(Direction.ABOVE);
            if (canBelow)  directionsToGo.add(Direction.BELOW);

            int s = directionsToGo.size();
            if (s == 0) {
                //Log.d("TEST", "SIZE IS ZERO!!!");
                return -1;
            }
            Direction d = directionsToGo.get(mRand.nextInt(s));
            switch (d) {
            case RIGHT:
                cell.right_side = false;
                concat(ccn, cells[ridx].cluster_number);
                drawOneCell(i);
                drawOneCell(ridx);
                i = ridx;
                break;
            case LEFT:
                Cell leftCell = cells[lidx];
                leftCell.right_side = false;
                concat(ccn, leftCell.cluster_number);
                drawOneCell(i);
                drawOneCell(lidx);
                i = lidx;
                break;
            case ABOVE:
                Cell aboveCell = cells[aidx];
                aboveCell.below_side = false;
                concat(ccn, aboveCell.cluster_number);
                drawOneCell(i);
                drawOneCell(aidx);
                i = aidx;
                break;
            case BELOW:
                cell.below_side = false;
                concat(ccn, cells[bidx].cluster_number);
                drawOneCell(i);
                drawOneCell(bidx);
                i = bidx;
                break;
            }

            return i;
        }
        
        private int FRAME_SIZE = 5;
        
        private void drawOneCell(int i) {
            	
            try {
                int cellWidth  = mScreenWidth  / MAZE_WIDTH;
                int cellHeight = mScreenHeight / MAZE_HEIGHT;

                if (cellWidth > cellHeight) {
                    cellWidth = cellHeight;
                } else {
                    cellHeight = cellWidth;
                }

                final int field_width  = MAZE_WIDTH  * cellWidth;
                final int field_height = MAZE_HEIGHT * cellHeight;
                
                int fx = (mScreenWidth  - field_width)  / 2;
                int fy = (mScreenHeight - field_height) / 2;
                //c.translate(fx, fy);

                int x = (i % MAZE_WIDTH) * cellWidth;
                int y = (i / MAZE_WIDTH) * cellHeight;
                
                Rect dirty = new Rect(fx + x, fy + y, fx + x + cellWidth, fy + y + cellHeight);
                c = mSurfaceHolder.lockCanvas(dirty);
                //Log.d("TEST", "width  = " + c.getWidth());
                //Log.d("TEST", "Height = " + c.getHeight());
                c.save();
                c.translate(fx + x, fy + y);

                synchronized (mSurfaceHolder) {

                    //c.drawColor(0xFFFFFFFF);
                    
                    ShapeDrawable rect = new ShapeDrawable(new RectShape());
                    Paint p = rect.getPaint();
                    p.setAntiAlias(false);
                    
                    rect.setBounds(0, 0, cellWidth, cellHeight);
                    p.setColor(0xFFFFFFFF);
                    rect.draw(c);
                    
                    p = new Paint();
                    p.setAntiAlias(false);

                    if (cells[i].right_side) {
                        c.drawLine(cellWidth - 1, 0, cellWidth - 1, cellHeight, p);
                    }

                    if (cells[i].below_side) {
                        c.drawLine(0, cellHeight - 1, cellWidth, cellHeight - 1, p);
                    }
                }
                c.restore();
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
        
        private void doDraw(Canvas c) {
        	c.save();
            c.drawColor(0xFFFFFFFF);
            mScreenWidth = c.getWidth();
            int screenHeight = c.getHeight();
            //Log.d("TEST", "screenWidth = " + screenWidth);
            //Log.d("TEST", "screenHeight = " + screenHeight);
            
            int cellWidth = mScreenWidth / MAZE_WIDTH;
            int cellHeight = screenHeight / MAZE_HEIGHT;
            //Log.d("TEST", "cellWidth = " + cellWidth);
            //Log.d("TEST", "cellHeight = " + cellHeight);
            
            if (cellWidth > cellHeight) {
                cellWidth = cellHeight;
            } else {
                cellHeight = cellWidth;
            }
            
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            Paint p = rect.getPaint();
            p.setAntiAlias(false);
            final int field_width = MAZE_WIDTH * cellWidth;
            final int field_height = MAZE_HEIGHT * cellHeight;
            //Log.d("TEST", "fieldWidth = " + field_width);
            //Log.d("TEST", "fieldHeight = " + field_height);
            
            int fx = (mScreenWidth - field_width) / 2 - FRAME_SIZE;
            int fy = (screenHeight - field_height) / 2 - FRAME_SIZE;
            c.translate(fx, fy);
            
            rect.setBounds(0, 0, field_width + FRAME_SIZE * 2, field_height + FRAME_SIZE * 2);
            p.setColor(0xFF000000);
            rect.draw(c);
            c.translate(FRAME_SIZE, FRAME_SIZE);
            rect.setBounds(0, 0, field_width, field_height);
            p.setColor(0xFFFFFFFF);
            rect.draw(c);
     
            p.setColor(0xFF000000);
            //Log.d("TEST", "flags = " + p.getFlags());
            for (int i = 0; i < cells.length; i++) {
                int x = (i % MAZE_WIDTH) * cellWidth;
                int y = (i / MAZE_WIDTH) * cellHeight;
                
                //rect.setBounds(x, y, x + cellWidth, x + cellHeight);
                
                if (cells[i].right_side) {
                	int nx = x + cellWidth - 1;
                    c.drawLine(nx, y, nx, y + cellHeight, p);
                }
                
                if (cells[i].below_side) {
                	int ny = y + cellHeight - 1;
                    c.drawLine(x, ny, x + cellWidth, ny, p);
                }
            }
            c.restore();
        }

        public void quit() {
            mRun = false;
        }
    }
    
    private class FieldView extends SurfaceView  implements SurfaceHolder.Callback {
        public FieldView(Context context) {
            super(context);
            
            mSurfaceHolder = getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setSizeFromLayout();
            
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }
        
        public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
            surfaceholder.setFixedSize(j,k);
        }

        public void surfaceCreated(SurfaceHolder surfaceholder) {
            // start the thread here so that we don't busy-wait in run()
            // waiting for the surface to be created
            mThread.start();
        }

        public void surfaceDestroyed(SurfaceHolder surfaceholder) {
            // we have to tell thread to shut down & wait for it to finish, or else
            // it might touch the Surface after we return and explode
            boolean retry = true;
            mThread.quit();
            while (retry) {
                try {
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout fl = new FrameLayout(this);
        mFieldView = new FieldView(this);
        fl.addView(mFieldView);
        RelativeLayout rl = new RelativeLayout(this);

        mStatusText = new TextView(this);
        mStatusText.setId(1);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        
        mStatusText.setGravity(Gravity.CENTER_HORIZONTAL);
        mStatusText.setTextSize(40);
        mStatusText.setText("GAME OVER");
        mStatusText.setTextColor(Color.BLACK);
        mStatusText.setVisibility(View.INVISIBLE);
        rl.addView(mStatusText, lp);
        fl.addView(rl);
        setContentView(fl);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mThread = new MyThread();
        
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell();
            cells[i].cluster_number = i;
            int[] tmp = {i};
            clusterList.add(i);
            clusterMap.put(i, tmp);
        }
        
        //make_maze();
        
        // Text Maze
/*        for (int i = 0; i < MAZE_HEIGHT; i++ ) {
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            for (int j = 0; j < MAZE_WIDTH; j++) {
                Cell c = cells[i * MAZE_WIDTH + j];
                if (c.cluster_number < 100) line1.append(0);
                if (c.cluster_number < 10) line1.append(0);
                line1.append(c.cluster_number);
                line1.append(c.right_side ? "|" : " ");
                line2.append(c.below_side ? "----" : "    ");
            }
            Log.d("TEST", line1.toString());
            Log.d("TEST", line2.toString());
        }*/
    }
    
    static private boolean leftIsWall(int i) {
        return (i % MAZE_WIDTH) == 0;
    }

    static private boolean aboveIsWall(int i) {
        return (i / MAZE_WIDTH) == 0;
    }
    static public boolean rightIsWall(int number) {
        return (number % MAZE_WIDTH) == (MAZE_WIDTH - 1);
    }
    
    static public boolean belowIsWall(int number) {
        return (number / MAZE_WIDTH) == (MAZE_HEIGHT - 1);
    }

    private void concat(int i, int j) {
        // swap if i > j so that the last cluster always be 0
        if (i > j) {
            int tmp = i;
            i = j;
            j = tmp;
        }
        //Log.d("TEST", "young = " + i);
        //Log.d("TEST", "old   = " + j);
        int[] young = clusterMap.get(i);
        int[] old   = clusterMap.remove(j);
        clusterList.remove(new Integer(j));

        int[] nuevo = new int[young.length + old.length];
        System.arraycopy(young, 0, nuevo, 0, young.length);
        System.arraycopy(old, 0, nuevo, young.length, old.length);
        for (int k = 0; k < old.length; k++) {
            cells[old[k]].cluster_number = i;
        }
        clusterMap.put(i, nuevo);
    }
    
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mScreenWidth = mFieldView.getWidth();
		mScreenHeight = mFieldView.getHeight();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mScreenWidth = mFieldView.getWidth();
		mScreenHeight = mFieldView.getHeight();
	}

}
