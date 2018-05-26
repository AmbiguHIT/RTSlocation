package com.ambigu.view;

import com.ambigu.rtslocation.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

public class Progress_load {
	private FrameLayout layout;
	private Context context;
	private PopupWindow window;
	private ImageView img_loading;

	private AnimationDrawable AniDraw;

	public Progress_load(FrameLayout layout, Context context) {
		this.layout = layout;
		this.context = context;
	}

	/**
	 * 显示popupWindow
	 */
	public void showPopwindow() {
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.progress_hud, null);
		img_loading = (ImageView) view.findViewById(R.id.spinnerImageView);
		img_loading.setBackgroundResource(R.anim.spinner);
		AniDraw = (AnimationDrawable) img_loading.getBackground();
		AniDraw.start();
		// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

		window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);

		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);

		// 必须要给调用这个方法，否则点击popWindow以外部分，popWindow不会消失
		// window.setBackgroundDrawable(new BitmapDrawable());

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xe0ffffff);
		window.setBackgroundDrawable(dw);

		// 设置popWindow的显示和消失动画
		window.setAnimationStyle(R.style.ProgressStyle);
		// 在底部显示
		if (layout != null) {
			window.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
		} else {
		}

	}

	public void dismissWindow() {
		if (window != null)
			window.dismiss();

	}
}
