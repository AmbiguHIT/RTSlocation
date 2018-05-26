package com.ambigu.view;

import com.ambigu.rtslocation.R;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyImageButton extends LinearLayout {

	public MyImageButton(Context context, int imageResId, int textResId) {
		super(context);

		mButtonImage = new ImageView(context);
		mButtonText = new TextView(context);

		setImageResource(imageResId);
		mButtonImage.setPadding(30, 10, 20, 10);

		setText(textResId);
		setTextColor(0x7f040001);
		mButtonText.setGravity(Gravity.CENTER);
		mButtonText.setTextSize(18);

		setClickable(true);
		setFocusable(true);

		setBackgroundResource(R.drawable.main_button_selector);
		setOrientation(LinearLayout.VERTICAL);

		addView(mButtonImage);
		addView(mButtonText);
	}

	// ----------------public method-----------------------------

	public void setImageResource(int resId) {
		mButtonImage.setImageResource(resId);
	}

	public void setText(int resId) {
		mButtonText.setText(resId);

	}

	public void setText(CharSequence buttonText) {
		mButtonText.setText(buttonText);
	}

	public void setTextColor(int color) {
		mButtonText.setTextColor(color);
	}

	public void setTextSize(int resId) {
		mButtonText.setTextSize(resId);

	}

	public void setGravity(int direction) {
		mButtonText.setGravity(direction);
	}

	// ----------------private attribute-----------------------------
	private ImageView mButtonImage = null;
	private TextView mButtonText = null;
}