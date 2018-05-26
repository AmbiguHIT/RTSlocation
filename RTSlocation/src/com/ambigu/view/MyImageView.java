// package com.ambigu.view;
//
// import java.io.InputStream;
//
// import android.content.Context;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.Matrix;
// import android.view.LayoutInflater;
// import android.widget.ImageView;
// import android.widget.LinearLayout;
// import android.widget.TextView;
//
// public class MyImageView extends LinearLayout {
// private ImageView iv_icon;
// private TextView tv_text;
//
// public MyImageView(Context context, Bitmap bm, String text_text) {
// super(context);
//
// BitmapFactory.Options options = new BitmapFactory.Options();
// options.inJustDecodeBounds = false;
// options.inSampleSize = 1; // width��hight��Ϊԭ����5��һ
// // bmp=narrowImage(bmp);
// iv_icon.setImageBitmap(bm);
// tv_text.setText(text_text);
//
// setClickable(true);
// setFocusable(true);
// setBackgroundResource(R.drawable.main_button_selector);
// }
//
// public MyImageView(Context context, int resId, String text_text) {
// super(context);
// // ���벼��
// LayoutInflater.from(context).inflate(R.layout.self_view_notes, this, true);
// iv_icon = (ImageView) findViewById(R.id.icon);
// tv_text = (TextView) findViewById(R.id.text);
//
// InputStream is = this.getResources().openRawResource(resId);
// BitmapFactory.Options options = new BitmapFactory.Options();
// options.inJustDecodeBounds = false;
// options.inSampleSize = 1; // width��hight��Ϊԭ����5��һ
// Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
// // bmp=narrowImage(bmp);
// iv_icon.setImageBitmap(bmp);
// tv_text.setText(text_text);
//
// setClickable(true);
// setFocusable(true);
// setBackgroundResource(R.drawable.main_button_selector);
// }
//
// public Bitmap narrowImage(Bitmap bmp) {
// // ��������Ҫ��С�ı���
// float scaleWidth = 150.0f;
// float scaleHeight = 50.0f;
//
// // ����resize���Bitmap����
// Matrix matrix = new Matrix();
// matrix.postScale(scaleWidth, scaleHeight);
// Bitmap resizeBmp = null;
// if (bmp != null)
// resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
// matrix, true);
// else
// resizeBmp = null;
// return resizeBmp;
// }
//
// }
