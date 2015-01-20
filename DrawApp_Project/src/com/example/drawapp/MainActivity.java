package com.example.drawapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private DrawingView drawView;
	private ImageView imageView;
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, loadBtn,
			exitBtn;
	private float smallBrush, mediumBrush, largeBrush;
	private static final int SELECT_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		drawView = (DrawingView) findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
		currPaint = (ImageButton) paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(
				R.drawable.paint_pressed));

		// instantiate brushes
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		drawView.setBrushSize(mediumBrush);

		// retrieve button from layout
		drawBtn = (ImageButton) findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);

		eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);

		newBtn = (ImageButton) findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);

		saveBtn = (ImageButton) findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);

		loadBtn = (ImageButton) findViewById(R.id.load_btn);
		loadBtn.setOnClickListener(this);

		exitBtn = (ImageButton) findViewById(R.id.exit_btn);
		exitBtn.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void paintClicked(View view) {
		// use chosen color
		if (view != currPaint) {
			// update color
			ImageButton imgView = (ImageButton) view;
			String color = view.getTag().toString();
			drawView.setColor(color);
			imgView.setImageDrawable(getResources().getDrawable(
					R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(
					R.drawable.paint));
			currPaint = (ImageButton) view;
			drawView.setErase(false);
			drawView.setBrushSize(drawView.getLastBrushSize());
		}
	}

	@Override
	public void onClick(View view) {
		// respond to clicks
		if (view.getId() == R.id.draw_btn) {
			// draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);

			// listen on clicks

			// small
			ImageButton smallBtn = (ImageButton) brushDialog
					.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(smallBrush);
					drawView.setLastBrushSize(smallBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			// medium
			ImageButton mediumBtn = (ImageButton) brushDialog
					.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(mediumBrush);
					drawView.setLastBrushSize(mediumBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			// large
			ImageButton largeBtn = (ImageButton) brushDialog
					.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(largeBrush);
					drawView.setLastBrushSize(largeBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			brushDialog.show();
		}

		else if (view.getId() == R.id.erase_btn) {
			// switch to erase - choose size
			drawView.setErase(true);
			drawView.setBrushSize(mediumBrush);
		}

		else if (view.getId() == R.id.new_btn) {
			// new drawing
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog
					.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							drawView.startNew();
							dialog.dismiss();
						}
					});
			newDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			newDialog.show();
		}

		else if (view.getId() == R.id.save_btn) {
			// save drawing
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");
			saveDialog.setMessage("Save drawing to device Gallery?");
			saveDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// save drawing
							drawView.setDrawingCacheEnabled(true);
							String imgSaved = MediaStore.Images.Media
									.insertImage(getContentResolver(), drawView
											.getDrawingCache(), UUID
											.randomUUID().toString() + ".png",
											"drawing");
							if (imgSaved != null) {
								Toast savedToast = Toast.makeText(
										getApplicationContext(),
										"Drawing saved to Gallery!",
										Toast.LENGTH_SHORT);
								savedToast.show();
							} else {
								Toast unsavedToast = Toast.makeText(
										getApplicationContext(),
										"Oops! Image could not be saved.",
										Toast.LENGTH_SHORT);
								unsavedToast.show();
							}
							drawView.destroyDrawingCache();
						}
					});
			saveDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			saveDialog.show();
		}

		else if (view.getId() == R.id.exit_btn) {
			// exit
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("Exit DrawApp");
			newDialog.setMessage("Would you like to exit DrawApp?");
			newDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
							System.exit(0);
						}
					});
			newDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			newDialog.show();
		} else if (view.getId() == R.id.load_btn) {
			// load
			Intent gallery = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(gallery, SELECT_IMAGE);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE && null != data) {
			Uri selectedImage = data.getData();
			String[] path = { MediaStore.Images.Media.DATA };
			
			Cursor cursor = getContentResolver().query(selectedImage,
	                 path, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(path[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			
			Bitmap bitmapImage = BitmapFactory.decodeFile(picturePath);
			Bitmap scaledBitmap = scaleDown(bitmapImage, 460, 600);
			imageView = (ImageView) findViewById(R.id.drawing);
			imageView.setImageBitmap(scaledBitmap);

		}
	}
	
	public static Bitmap scaleDown(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

}
