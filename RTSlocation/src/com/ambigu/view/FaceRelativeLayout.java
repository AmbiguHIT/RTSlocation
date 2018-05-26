package com.ambigu.view;

import java.util.ArrayList;
import java.util.List;

import com.ambigu.adapter.FaceAdapter;
import com.ambigu.adapter.ViewPagerAdapter;
import com.ambigu.rtslocation.ChatEmoji;
import com.ambigu.rtslocation.R;
import com.ambigu.util.FaceConversionUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 
 ******************************************
 * @author 廖乃波
 * @文件名称 : FaceRelativeLayout.java
 * @创建时间 : 2013-1-27 下午02:34:17
 * @文件描述 : 带表情的自定义输入框
 ******************************************
 */
public class FaceRelativeLayout extends RelativeLayout implements OnItemClickListener, OnClickListener {

	private Context context;

	/** 表情页的监听事件 */
	private OnCorpusSelectedListener mListener;

	/** 显示表情页的viewpager */
	private ViewPager vp_face;

	/** 表情页界面集合 */
	private ArrayList<View> pageViews;

	/** 游标显示布局 */
	private LinearLayout layout_point;

	/** 游标点集合 */
	private ArrayList<ImageView> pointViews;

	/** 表情集合 */
	private List<List<ChatEmoji>> emojis;

	/** 表情区域 */
	private View view;

	/** 输入框 */
	private EditText et_sendmessage;

	/** 表情数据填充器 */
	private List<FaceAdapter> faceAdapters;

	/** 当前表情页 */
	private int current = 0;

	public FaceRelativeLayout(Context context) {
		super(context);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle, boolean f) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	/**
	 * 表情选择监听
	 * 
	 * 
	 */
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		emojis = FaceConversionUtil.getInstace().emojiLists;
		onCreate();
	}

	private void onCreate() {
		Init_View();
		Init_viewPager();
		Init_Point();
		Init_Data();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_face:
			// 隐藏表情选择框
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.et_sendmessage:
			// 隐藏表情选择框
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			}
			break;

		}
	}

	/**
	 * 隐藏表情选择框
	 */
	public boolean hideFaceView() {
		// 隐藏表情选择框
		if (view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	/**
	 * 初始化控件
	 */
	private void Init_View() {
		vp_face = (ViewPager) findViewById(R.id.vp_contains);
		et_sendmessage = (EditText) findViewById(R.id.et_sendmessage);
		layout_point = (LinearLayout) findViewById(R.id.iv_image);
		et_sendmessage.setOnClickListener(this);
		findViewById(R.id.btn_face).setOnClickListener(this);
		view = findViewById(R.id.ll_facechoose);
		view.setTag(0);
		et_sendmessage.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (start == 0) {
					Log.v("hell0", "213131321");
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.plus);
					((Button) findViewById(R.id.btn_send)).setText("");
				} else {
					Log.v("hell", "213131321");
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.chat_send_btn);
					((Button) findViewById(R.id.btn_send)).setText("发送");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				if (start == 0) {
					Log.v("hell0", "213131321");
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.plus);
					((Button) findViewById(R.id.btn_send)).setText("");
				} else {
					Log.v("hell", "213131321");
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.chat_send_btn);
					((Button) findViewById(R.id.btn_send)).setText("发送");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() == 0) {
					Log.v("hell0", "213131321");
					view.setTag(1);
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.plus);
					((Button) findViewById(R.id.btn_send)).setText("");
					((Button) findViewById(R.id.btn_send)).setTag(1);
				} else {
					Log.v("hell", "213131321");
					view.setTag(0);
					((Button) findViewById(R.id.btn_send)).setBackgroundResource(R.drawable.chat_send_btn);
					((Button) findViewById(R.id.btn_send)).setText("发送");
					((Button) findViewById(R.id.btn_send)).setTag(2);
				}
			}
		});
	}

	/**
	 * 初始化显示表情的viewpager
	 */
	private void Init_viewPager() {
		pageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(context);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView1);

		// 中间添加表情页
		if ((Integer) view.getTag() == 0) {
			faceAdapters = new ArrayList<FaceAdapter>();
			for (int i = 0; i < emojis.size(); i++) {
				GridView view = new GridView(context);
				FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));
				view.setAdapter(adapter);
				faceAdapters.add(adapter);
				view.setOnItemClickListener(this);
				view.setNumColumns(7);
				view.setBackgroundColor(Color.TRANSPARENT);
				view.setHorizontalSpacing(1);
				view.setVerticalSpacing(1);
				view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				view.setCacheColorHint(0);
				view.setPadding(5, 0, 5, 0);
				view.setSelector(new ColorDrawable(Color.TRANSPARENT));
				view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				view.setGravity(Gravity.CENTER);
				pageViews.add(view);
			}

			// 右侧添加空页面
			View nullView2 = new View(context);
			// 设置透明背景
			nullView2.setBackgroundColor(Color.TRANSPARENT);
			pageViews.add(nullView2);
		} else {
			GridView view = new GridView(context);
			Button bt_site = new Button(context);
			bt_site.setBackgroundResource(R.drawable.site_normal);
			bt_site.setClickable(true);
			Button bt_voice = new Button(context);
			bt_voice.setBackgroundResource(R.drawable.dial_voice_normal);
			bt_voice.setClickable(true);
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			view.setNumColumns(4);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setGravity(Gravity.CENTER);
			view.addView(bt_site);
			view.addView(bt_voice);
			bt_site.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.e("bt_site", "site");
				}
			});
			bt_voice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.e("bt_voice", "voice");
				}
			});
			pageViews.add(view);
		}

	}

	/**
	 * 初始化游标
	 */
	private void Init_Point() {

		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			layout_point.addView(imageView, layoutParams);
			if (i == 0 || i == pageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViews.add(imageView);

		}
	}

	/**
	 * 填充数据
	 */
	private void Init_Data() {
		vp_face.setAdapter(new ViewPagerAdapter(pageViews));

		vp_face.setCurrentItem(1);
		current = 0;
		vp_face.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				draw_Point(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViews.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						vp_face.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
						pointViews.get(1).setBackgroundResource(R.drawable.d2);
					} else {
						vp_face.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViews.get(arg0 - 1).setBackgroundResource(R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	/**
	 * 绘制游标背景
	 */
	public void draw_Point(int index) {
		for (int i = 1; i < pointViews.size(); i++) {
			if (index == i) {
				pointViews.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				pointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon) {
			int selection = et_sendmessage.getSelectionStart();
			String text = et_sendmessage.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					et_sendmessage.getText().delete(start, end);
					return;
				}
				et_sendmessage.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = FaceConversionUtil.getInstace().addFace(getContext(), emoji.getId(),
					emoji.getCharacter());
			et_sendmessage.append(spannableString);
		}

	}
}
