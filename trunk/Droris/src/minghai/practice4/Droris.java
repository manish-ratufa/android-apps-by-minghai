package minghai.practice4;

import java.util.LinkedList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Droris extends Activity {
	
	private enum KeyBits {
		LEFT  (1<<0),
		RIGHT (1<<1),
		UP    (1<<2),
		DOWN  (1<<3),
		ROTATE_LEFT (1<<4),
		ROTATE_RIGHT (1<<5),
		HOLD (1<<6);
		
		int bit;
		KeyBits(int b) {
			bit = b;
		}
		
		public int setBit(int b) {
			return bit | b;
		}
		
		public int resetBit(int b) {
			return bit & ~b;
		}
		
		public boolean isOn(int b) {
			return (bit & b) > 0;
		}
	}
	
	private class FieldView extends SurfaceView  implements SurfaceHolder.Callback {

		final private class Matrix {
			public int[][] m;
			
			public Matrix(int[][] arg) {
				this.m = arg;
			}
		}
		
		final private class Block {
			public Matrix[] rotation;
			public int color;
			private int direction = 0;
			public int initX;
			public int initY;
			
			private Block() {
				super();
			}

			public Block(int[][][] rotation, int color, int initX, int initY) {
				int c = rotation.length;
				this.rotation = new Matrix[c];
				for (int i = 0; i < c; i++) {
					this.rotation[i] = new Matrix(rotation[i]);
				}
				this.color = color;
				this.initX = initX;
				this.initY = initY;
			}

			public Block clone() {
				Block b = new Block();
				b.rotation = this.rotation;
				b.color = this.color;
				b.initX = this.initX;
				b.initY = this.initY;
				b.direction = 0;
				return b;
			}
			
			public Matrix getMatrix() {
				return rotation[direction];
			}
			
			public void rotateLeft() {
				if (++direction >= rotation.length) {
					direction = 0;
				}
			}

			public void rotateRight() {
				if (--direction < 0) {
					direction = rotation.length - 1;
				}
			}
		}
		
		int[][][] orange = {
				{
					{0,0,0},
					{1,1,1},
					{1,0,0}
				},
				{
					{0,1,0},
					{0,1,0},
					{0,1,1}
				},
				{
					{0,0,0},
					{0,0,1},
					{1,1,1}
				},
				{
					{1,1,0},
					{0,1,0},
					{0,1,0}
				}
		};
		Block orangeBlock = new Block(orange, 0xFFFFB000, 3, -1);
		
		int[][][] blue = {
				{
					{0,0,0},
					{2,2,2},
					{0,0,2}
				},
				{
					{0,2,2},
					{0,2,0},
					{0,2,0}
				},
				{
					{0,0,0},
					{2,0,0},
					{2,2,2}
				},
				{
					{0,2,0},
					{0,2,0},
					{2,2,0}
				}
		};
		Block blueBlock = new Block(blue, Color.BLUE, 3, -1);
		
		int[][][] yellow = {
				{
					{3,3},
					{3,3}
				}
		};
		Block yellowBlock = new Block(yellow, Color.YELLOW, 4, 0);
		
		int[][][] cyan = {
				{
					{0,0,0},
					{4,4,4},
					{0,4,0}
				},
				{
					{0,4,0},
					{0,4,4},
					{0,4,0}
				},
				{
					{0,0,0},
					{0,4,0},
					{4,4,4}
				},
				{
					{0,4,0},
					{4,4,0},
					{0,4,0}
				}
		};
		Block cyanBlock = new Block(cyan, Color.CYAN, 3, -1);
		
		int[][][] green = {
				{
					{0,0,0},
					{5,5,0},
					{0,5,5}
				},
				{
					{0,0,5},
					{0,5,5},
					{0,5,0}
				}
		};
		Block greenBlock = new Block(green, Color.GREEN, 3, -1);

		int[][][] magenta = {
				{
					{0,0,0},
					{0,6,6},
					{6,6,0}
				},
				{
					{6,0,0},
					{6,6,0},
					{0,6,0}
				}
		};
		Block magentaBlock = new Block(magenta, Color.MAGENTA, 3, -1);
		
		int[][][] red = {
				{
					{0,0,0,0},
					{7,7,7,7},
					{0,0,0,0},
					{0,0,0,0}
				},
				{
					{0,0,7,0},
					{0,0,7,0},
					{0,0,7,0},
					{0,0,7,0}
				}
		};
		Block redBlock = new Block(red, Color.RED, 3, -1);
		
		private final Random mRand = new Random(System.currentTimeMillis());
		
		// 使用される全てのブロック
		// ここからランダムに選択し、cloneにてコピーした新しいブロックインスタンスを用いる
		LinkedList<Block> blocks;

		// 現在画面上のブロック
		Block block;
		
		int posx, posy;
		static final private int FRAME_SIZE = 5;
		static final private int BLOCK_SIZE = 16;
		static final private int mapWidth  = 10;
		static final private int mapHeight = 20;
		int[][] map = new int[mapHeight][];
		private SurfaceHolder mSurfaceHolder;
		private int keyState;
		private int lineCounter = 0;
		private int gravity = 1;
		private long lastDropTime;
		private long lastMoveTime;

		class MyThread extends Thread {
	        private boolean mRun = true;
	        private int frameCount = 0;
	        private final long startTime = System.currentTimeMillis();

	        private int fps;

			@Override
	        public void run() {
		        lastDropTime = System.currentTimeMillis();
		        lastMoveTime = lastDropTime;
				while (mRun) {
	                Canvas c = null;
	                long currentTime = System.currentTimeMillis();
					frameCount++; 
	                
	                if (currentTime - lastMoveTime > 100) {
	                	doMove();
	                	lastMoveTime = currentTime;
	                }
	                
	                fps = (int)(frameCount * 1000 / (currentTime - startTime));
	                
	                if (currentTime - lastDropTime > 500) {
	                	doDrop();
	                	lastDropTime = currentTime;
	                	//lastMoveTime = currentTime;
	                }

	                try {
	                    c = mSurfaceHolder.lockCanvas();

	                    synchronized (mSurfaceHolder) {
	                        doDraw(c);
	                    }
	                } finally {
	                    // do this in a finally so that if an exception is thrown
	                    // during the above, we don't leave the Surface in an
	                    // inconsistent state
	                    if (c != null) {
	                        mSurfaceHolder.unlockCanvasAndPost(c);
	                    }
	                }
	            }
	        }
			
			public void quit() {
				mRun = false;
			}
			
			public int getFPS() {
				return fps;
			}
		};
		private MyThread mThread = new MyThread();

		private void doDrop() {
            if (!check(block.getMatrix().m, posx, posy + 1)) {
                mergeMatrix(block.getMatrix().m, posx, posy);
                clearRows();
                
            	Block tmp = blocks.remove(mRand.nextInt(blocks.size()));
                futureBlocks.add(tmp);
                blocks.add(block.clone());
                
                block = futureBlocks.removeFirst();
                posx = block.initX;
                posy = block.initY;
                
                if (map[0][5] != 0) {
                	Message m = mHandler.obtainMessage();
                	mHandler.sendMessage(m);
                	mThread.quit();
                }
                
                return; // End if merged
            }
            int tg = gravity - 1;
            posy++;
            
            while (tg-- > 0) {
                if (check(block.getMatrix().m, posx, posy + 1)) {
                	posy++;
                } else {
                	return;
                }
            }
		}
		
		private LinkedList<Block> futureBlocks;
		
		public FieldView(Context context) {
			super(context);
			
			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this);
			mSurfaceHolder.setSizeFromLayout();
			
	        setFocusable(true);
	        setFocusableInTouchMode(true);
	        requestFocus();
		}
		
		public void initGame() {
			mThread = new MyThread();
			
            for (int y = 0; y < mapHeight; y++) {
                map[y] = new int[mapWidth];
                for (int x = 0; x < mapWidth; x++) {
                    map[y][x] = 0;
                }
            }
            
			blocks = new LinkedList<Block>();
			blocks.add(orangeBlock);
			blocks.add(blueBlock);
			blocks.add(yellowBlock);
			blocks.add(cyanBlock);
			blocks.add(greenBlock);
			blocks.add(magentaBlock);
			blocks.add(redBlock);

			Block tmp = blocks.remove(mRand.nextInt(blocks.size()));
            block = tmp.clone();
            posx = block.initX;
            posy = block.initY;
            futureBlocks = new LinkedList<Block>();
            for (int i = 0; i < 3; i++) {
            	Block tmp2 = blocks.remove(mRand.nextInt(blocks.size()));
            	futureBlocks.add(tmp2);
            }
            
            lineCounter = 0;
            gravity = 1;
		}

		private void commonPaint(Canvas canvas, ShapeDrawable rect,
				int[][] matrix, int offsetx, int offsety) {
			int h = matrix.length;
	    	int w = matrix[0].length;

            for (int y = 0; y < h; y ++) {
                for (int x = 0; x < w; x ++) {
                	int k = matrix[y][x];
                    if (k != 0) {
                    	int color;
                    	switch (k & 0x07) {
                    	case 1:
                    		color = orangeBlock.color;
                    		break;
                    	case 2:
                    		color = blueBlock.color;
                    		break;
                    	case 3:
                    		color = yellowBlock.color;
                    		break;
                    	case 4:
                    		color = cyanBlock.color;
                    		break;
                    	case 5:
                    		color = greenBlock.color;
                    		break;
                    	case 6:
                    		color = magentaBlock.color;
                    		break;
                    	case 7:
                    		color = redBlock.color;
                    		break;
                    	default:
                    		color = 0;
                    		break;
                    	}
                    	if ((k & 0x10) != 0) {
                    		int red = Color.red(color);
                    		int blue = Color.blue(color);
                    		int green = Color.green(color);
                    		color = Color.argb(0x80, red, green, blue);
                    	}
                    	Paint p = rect.getPaint();
                    	p.setAntiAlias(false);
                    	rect.getPaint().setColor(color);
                    	int px = (x + offsetx) * BLOCK_SIZE;
                    	int py = (y + offsety) * BLOCK_SIZE;
            	    	rect.setBounds(px, py, px + BLOCK_SIZE, py + BLOCK_SIZE);
            	    	rect.draw(canvas);
                    }
                }
            }
		}
        
        private void paintMatrix(Canvas canvas, int[][] matrix, int offsetx, int offsety, int color) {
	    	ShapeDrawable rect = new ShapeDrawable(new RectShape());
	    	//rect.getPaint().setColor(color);
	    	
	    	commonPaint(canvas, rect, matrix, offsetx, offsety);
        }

        private void paintShadow(Canvas canvas, int[][] matrix, int offsetx, int offsety, int color) {
            int y = posy;
            while (check(matrix, posx, y)) { y++; }
            if (y > 0) y = y - 1;
            
            if (y == posy) return; // ブロックと影が重なったら描かない
            
	    	ShapeDrawable rect = new ShapeDrawable(new RectShape());
	    	Paint p = rect.getPaint();
	    	p.setStyle(Style.STROKE);
	    	p.setStrokeWidth(5);
	    	int c = color & 0x7FFFFFFF;
	    	p.setColor(c);
	    	p.setAntiAlias(false);
	    	
            commonPaint(canvas, rect, matrix, offsetx, y);
        }

		// ブロックがマップ上のその位置に存在できるかどうかの確認
        // できるならtrue, できないならfalse
        boolean check(int[][] block, int offsetx, int offsety) {
        	int blockWidth = block[0].length;
        	int blockHeight = block.length;

        	// ブロックの全ての点を舐める
            for (int y = 0; y < blockHeight; y ++) {
            	int ry = y + offsety;
            	// ブロックは上だけ画面外でも良い
            	if (ry < 0) continue;
                for (int x = 0; x < blockWidth; x ++) {
                	int b = block[y][x];
                	if (b == 0) continue;
                	// ここから下はブロックに点有り
                	
                	// ブロックの点が画面外にあるならアウト
                	if (ry >= mapHeight) return false;
                	
                	int rx = x + offsetx;
                	if (rx < 0 || rx >= mapWidth) {
                		return false;
                	}
                	// ブロックの点が画面内の瓦礫と重なるならアウト
                	int m = map[ry][rx];
                    if (m != 0) { 
                        return false;
                    }
                }
            }
            return true;
        }

        void mergeMatrix(int[][] block, int offsetx, int offsety) {
            for (int y = 0; y < block.length; y ++) {
            	int ry = y + offsety;
            	if (ry < 0) continue;
                for (int x = 0; x < block[0].length; x ++) {
                    if (block[y][x] != 0) {
                        map[ry][offsetx + x] = block[y][x] | 0x10;
                    }
                }
            }
        }

        void clearRows() {
        	// 埋まった行は消す。nullで一旦マーキング
            for (int y = 0; y < mapHeight; y ++) {
                boolean full = true;
                for (int x = 0; x < mapWidth; x ++) {
                    if (map[y][x] == 0) {
                        full = false;
                        break;
                    }
                }
                
                if (full) map[y] = null;
            }
            
            // 新しいmapにnull以外の行を詰めてコピーする
            int[][] newMap = new int[mapHeight][];
            int y2 = mapHeight - 1;
            for (int y = mapHeight - 1; y >= 0; y--) {
            	if (map[y] == null) {
            		continue;
            	} else {
            		newMap[y2--] = map[y];
            	}
            }
            
            // 消去した行が無い場合
            if (y2 == -1) {
            	map = newMap;
            	return;
            }
            
            //重力は50行毎に強くなる
            lineCounter += y2 + 1;
            gravity = (int)(lineCounter / 50) + 1;
            
            // 消えた行数分新しい行を追加する
            for (int i = 0; i <= y2; i++) {
            	int[] newRow = new int[mapWidth];
                for (int j = 0; j < mapWidth; j++) {
                    newRow[j] = 0;
                }
                newMap[i] = newRow;
            }
            map = newMap;
        }

	    /**
	     * Draws the 2D layer.
	     */
	    void doDraw(Canvas canvas) {
	    	// future blocks
	    	canvas.drawColor(0xEFFFFFFF);
	    	Paint t = new Paint();
	    	canvas.drawText("FPS: " + mThread.getFPS(), 250, 20, t);
	    	canvas.drawText("Line: " + lineCounter, 250, 300, t);
	    	canvas.drawText("Gravity: " + gravity, 234, 310, t);
	    	canvas.save();
	    	canvas.translate(FRAME_SIZE, BLOCK_SIZE * 2);
	    	paintNextBlock(canvas);
	    	canvas.restore();
	    	
	    	// field
	    	canvas.translate(0, 70);
	    	ShapeDrawable rect = new ShapeDrawable(new RectShape());
	    	Paint p = rect.getPaint();
	    	final int field_width = mapWidth * BLOCK_SIZE;
	    	final int field_height = mapHeight * BLOCK_SIZE;
	    	rect.setBounds(0, 0, field_width + FRAME_SIZE * 2, field_height + FRAME_SIZE * 2);
	    	p.setColor(0xFF000000);
	    	rect.draw(canvas);
	    	canvas.translate(FRAME_SIZE, FRAME_SIZE);
	    	rect.setBounds(0, 0, field_width, field_height);
	    	p.setColor(0xFFCCCCCC);
	    	rect.draw(canvas);
	    	
	    	int[][] tetra = block.getMatrix().m;
            paintMatrix(canvas, tetra, posx, posy, block.color);
            paintMatrix(canvas, map, 0, 0, 0xFF808080);
	    	paintShadow(canvas, tetra, posx, posy, block.color);
	    }

        private void paintNextBlock(Canvas canvas) {
        	Block b = futureBlocks.getFirst();
        	int[][] m = b.getMatrix().m;
    		int last_width = m[0].length;
        	paintMatrix(canvas, m, b.initX, b.initY, b.color);
        	
        	canvas.translate(BLOCK_SIZE * (b.initX + last_width + 0.5f), BLOCK_SIZE);
    		canvas.scale(0.5f, 0.5f);
    		
        	for (int i = 1; i < futureBlocks.size(); i++) {
        		b = futureBlocks.get(i);

        		paintMatrix(canvas, b.getMatrix().m, 0, b.initY, b.color);

        		m = b.getMatrix().m;
        		last_width = m[0].length + 1; // 空白を１個空ける
        		
        		if (last_width == 2) last_width++;
        		canvas.translate(BLOCK_SIZE * last_width, 0);
        	}
		}

		@Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
        	switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_Z:
            	keyState = KeyBits.ROTATE_LEFT.setBit(keyState);
            	break;
            case KeyEvent.KEYCODE_X:
            	keyState = KeyBits.ROTATE_RIGHT.setBit(keyState);
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            	keyState = KeyBits.RIGHT.setBit(keyState);
            	break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            	keyState = KeyBits.LEFT.setBit(keyState);
               	break;
            case KeyEvent.KEYCODE_DPAD_UP:
            	keyState = KeyBits.UP.setBit(keyState);
            	break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            	keyState = KeyBits.DOWN.setBit(keyState);
            	break;
            default:
            	return false;
        	}
        	return true;
        }
		
	    @Override
	    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        	switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_Z:
            	//keyState = KeyBits.ROTATE_LEFT.resetBit(keyState);
            	break;
            case KeyEvent.KEYCODE_X:
            	//keyState = KeyBits.ROTATE_RIGHT.resetBit(keyState);
            	break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            	keyState = KeyBits.RIGHT.resetBit(keyState);
            	break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            	keyState = KeyBits.LEFT.resetBit(keyState);
               	break;
            case KeyEvent.KEYCODE_DPAD_UP:
            	//keyState = KeyBits.UP.resetBit(keyState);
            	break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            	keyState = KeyBits.DOWN.resetBit(keyState);
            	break;
            default:
            	return false;
        	}
        	return true;
	    }
		
		public void doMove() {	
			// 回転補正：　回転後に±１の範囲で補正を行う。壁蹴り。回し入れ。
			// 右優先、陰謀有り
            if (KeyBits.ROTATE_LEFT.isOn(keyState)) {
            	block.rotateLeft();
                if (!check(block.getMatrix().m, posx, posy)) {
                	if (check(block.getMatrix().m, posx + 1, posy)) { // 陰謀
                		posx++;
                	} else if (check(block.getMatrix().m, posx - 1, posy)) {
                		posx--;
                	} else {
                		block.rotateRight();
                	}
                }
                keyState = KeyBits.ROTATE_LEFT.resetBit(keyState);
            }
            
            if (KeyBits.ROTATE_RIGHT.isOn(keyState)) {
            	block.rotateRight();
                if (!check(block.getMatrix().m, posx, posy)) {
                	if (check(block.getMatrix().m, posx + 1, posy)) { // 陰謀
                		posx++;
                	} else if (check(block.getMatrix().m, posx - 1, posy)) {
                		posx--;
                	} else {
                		block.rotateLeft();
                	}
                }
                keyState = KeyBits.ROTATE_RIGHT.resetBit(keyState);
            }

            if (KeyBits.RIGHT.isOn(keyState)) {
                if (check(block.getMatrix().m, posx + 1, posy)) {
                    posx = posx + 1;
                }
            }

            if (KeyBits.LEFT.isOn(keyState)) {
                if (check(block.getMatrix().m, posx - 1, posy)) {
                    posx = posx - 1;
                }
            }

            if (KeyBits.UP.isOn(keyState)) {
                int y = posy;
                while (check(block.getMatrix().m, posx, y)) { y++; }
                if (y > 0) posy = y - 1;
                keyState = KeyBits.UP.resetBit(keyState);
                doDrop();
            }

            if (KeyBits.DOWN.isOn(keyState)) {
            	if (check(block.getMatrix().m, posx, posy + 1)) {
            		posy++;
            	}
            }
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
	
	Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            //mStatusText.setVisibility(m.getData().getInt("viz"));
            mStatusText.setVisibility(View.VISIBLE);
            //mStatusText.setText(m.getData().getString("text"));
        }
	};
	
	FieldView mFieldView;
	TextView mStatusText;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		FrameLayout fl = new FrameLayout(this);
		mFieldView = new FieldView(this);
		fl.addView(mFieldView);
		RelativeLayout rl = new RelativeLayout(this);
		//rl.setLayoutParams(new ViewGroup.LayoutParams(
		//		ViewGroup.LayoutParams.FILL_PARENT,
		//		ViewGroup.LayoutParams.FILL_PARENT));
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
	public void onResume() {
		super.onResume();
		mFieldView.initGame();
		Looper.myQueue().addIdleHandler(new Idler());
	}
	
	@Override
	public void onPause() {
        super.onPause();
        mFieldView.mThread.quit();
    }

    @Override
	public void onStop() {
        super.onStop();
        mFieldView.mThread.quit();
    }


	// Allow the activity to go idle before its animation starts
	class Idler implements MessageQueue.IdleHandler {
		public Idler() {
			super();
		}

		public final boolean queueIdle() {
			return false;
		}
	}
}
