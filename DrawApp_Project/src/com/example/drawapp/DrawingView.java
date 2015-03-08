package com.example.drawapp;

import java.util.ArrayList;

import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

@SuppressLint("ClickableViewAccessibility")
public class DrawingView extends View {

	// drawing path
	private Path drawPath;
	// drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	// initial color
	private int paintColor = 0xFF660000;
	// canvas
	private Canvas drawCanvas;
	// canvas bitmap
	private Bitmap canvasBitmap;
	// brush size & last brush size used
	private float brushSize, lastBrushSize;
	// erase
	private boolean erase = false;
    // path
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 8;
    // options
    //private BlurMaskFilter drawBlur;
    //private static final float mRadius = 1/10;
    // undo and redo
    public ArrayList<Path> paths = new ArrayList<Path>(3);
    public ArrayList<Path> undonePaths = new ArrayList<Path>(3);
    public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);

        setupDrawing();
	}

	private void setupDrawing() {

		// set initial brush size
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;

		// get drawing area setup for interaction
		drawPath = new Path();
		drawPaint = new Paint();
		// set color
		drawPaint.setColor(paintColor);

		// set properties
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setPathEffect(new CornerPathEffect(10));
        drawPaint.setDither(true);
        // set properties - Blur-Mask
    //    drawBlur = new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL);
    //    drawPaint.setMaskFilter(drawBlur);

		canvasPaint = new Paint(Paint.DITHER_FLAG);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// view given size
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        for (Path p : paths) {
            canvas.drawPath(p, drawPaint);
        }
		canvas.drawPath(drawPath, drawPaint);
	}

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            //drawPath.quadTo(mX, mY, (x + dx) / 2, (y + dy) / 2);
            //drawPath.cubicTo((mX + dx) / 5 , (mY + dy) / 5 , (mX - dx) / 5, (mY - dy) / 5, mX, mY );
            mX = x;
            mY = y;
        }
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// detect user touch
		float touchX = event.getX();
		float touchY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
            undonePaths.clear();
            drawPath.reset();
			drawPath.moveTo(touchX, touchY);
            mX = touchX;
            mY = touchY;
            break;
		case MotionEvent.ACTION_MOVE:
            touch_move(touchX, touchY);
            break;
		case MotionEvent.ACTION_UP:
            drawPath.lineTo(touchX, touchY);
			drawCanvas.drawPath(drawPath, drawPaint);
            paths.add(drawPath);
            drawPath = new Path();
			drawPath.reset();
            break;
		default:
			return false;
		}
		invalidate();
		return true;

	}

	public void setColor(String newColor) {
		// set color
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}

	public void setBrushSize(float newSize) {
		// update size
		float pixelAmount = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, newSize, getResources()
						.getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize) {
		lastBrushSize = lastSize;
	}

	public float getLastBrushSize() {
		return lastBrushSize;
	}

	public void setErase(boolean isErase) {
		// set erase true or false
		erase = isErase;
		if (erase)
			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else
			drawPaint.setXfermode(null);
	}
	
	public void startNew(){
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        paths.clear();
        undonePaths.clear();
	    invalidate();
	}

    public void loadBitmap(String picturePath){
        Bitmap bitmapImage = BitmapFactory.decodeFile(picturePath);
        Bitmap scaledBitmap = scaleDown(bitmapImage, 460, 600);
        drawCanvas.drawBitmap(scaledBitmap,0,0,canvasPaint);
        invalidate();
    }

    private static Bitmap scaleDown(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

}
