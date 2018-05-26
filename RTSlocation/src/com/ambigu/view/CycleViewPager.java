package com.ambigu.view;

import java.util.ArrayList;
import java.util.List;

import com.ambigu.rtslocation.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * ʵ�ֿ�ѭ�������ֲ���viewpager
 */
@SuppressLint("NewApi")
public class CycleViewPager extends Fragment implements OnPageChangeListener {

	private List<ImageView> imageViews = new ArrayList<ImageView>();
	private ImageView[] indicators;
	private FrameLayout viewPagerFragmentLayout;
	private LinearLayout indicatorLayout; // ָʾ��
	private BaseViewPager viewPager;
	private BaseViewPager parentViewPager;
	private ViewPagerAdapter adapter;
	private CycleViewPagerHandler handler;
	private int time = 5000; // Ĭ���ֲ�ʱ��
	private int currentPosition = 0; // �ֲ���ǰλ��
	private boolean isScrolling = false; // �������Ƿ������
	private boolean isCycle = false; // �Ƿ�ѭ��
	private boolean isWheel = false; // �Ƿ��ֲ�
	private long releaseTime = 0; // ��ָ�ɿ���ҳ�治����ʱ�䣬��ֹ�ֻ��ɿ����ʱ������л�
	private int WHEEL = 100; // ת��
	private int WHEEL_WAIT = 101; // �ȴ�
	private ImageCycleViewListener mImageCycleViewListener;
	private List<ADInfo> infos;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_cycle_viewpager_contet, null);

		viewPager = (BaseViewPager) view.findViewById(R.id.viewPager);
		indicatorLayout = (LinearLayout) view.findViewById(R.id.layout_viewpager_indicator);

		viewPagerFragmentLayout = (FrameLayout) view.findViewById(R.id.layout_viewager_content);

		handler = new CycleViewPagerHandler(getActivity()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == WHEEL && imageViews.size() != 0) {
					if (!isScrolling) {
						int max = imageViews.size() + 1;
						int position = (currentPosition + 1) % imageViews.size();
						viewPager.setCurrentItem(position, true);
						if (position == max) { // ���һҳʱ�ص���һҳ
							viewPager.setCurrentItem(1, false);
						}
					}

					releaseTime = System.currentTimeMillis();
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, time);
					return;
				}
				if (msg.what == WHEEL_WAIT && imageViews.size() != 0) {
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, time);
				}
			}
		};

		return view;
	}

	public void setData(List<ImageView> views, List<ADInfo> list, ImageCycleViewListener listener) {
		setData(views, list, listener, 0);
	}

	/**
	 * ��ʼ��viewpager
	 * 
	 * @param views
	 *            Ҫ��ʾ��views
	 * @param showPosition
	 *            Ĭ����ʾλ��
	 */
	public void setData(List<ImageView> views, List<ADInfo> list, ImageCycleViewListener listener, int showPosition) {
		mImageCycleViewListener = listener;
		infos = list;
		this.imageViews.clear();

		if (views.size() == 0) {
			viewPagerFragmentLayout.setVisibility(View.GONE);
			return;
		}

		for (ImageView item : views) {
			this.imageViews.add(item);
		}

		int ivSize = views.size();

		// ����ָʾ��
		indicators = new ImageView[ivSize];
		if (isCycle)
			indicators = new ImageView[ivSize - 2];
		indicatorLayout.removeAllViews();
		for (int i = 0; i < indicators.length; i++) {
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_cycle_viewpager_indicator, null);
			indicators[i] = (ImageView) view.findViewById(R.id.image_indicator);
			indicatorLayout.addView(view);
		}

		adapter = new ViewPagerAdapter();

		// Ĭ��ָ���һ��·�viewPager.setCurrentItem���������¼���ָʾ��ָ��
		setIndicator(0);

		viewPager.setOffscreenPageLimit(3);
		viewPager.setOnPageChangeListener(this);
		viewPager.setAdapter(adapter);
		if (showPosition < 0 || showPosition >= views.size())
			showPosition = 0;
		if (isCycle) {
			showPosition = showPosition + 1;
		}
		viewPager.setCurrentItem(showPosition);

	}

	/**
	 * ����ָʾ�����У�Ĭ��ָʾ�����ҷ�
	 */
	public void setIndicatorCenter() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		indicatorLayout.setLayoutParams(params);
	}

	/**
	 * �Ƿ�ѭ����Ĭ�ϲ�����������ǰ���뽫views����ǰ��������������һ����ͼ������ѭ��
	 * 
	 * @param isCycle
	 *            �Ƿ�ѭ��
	 */
	public void setCycle(boolean isCycle) {
		this.isCycle = isCycle;
	}

	/**
	 * �Ƿ���ѭ��״̬
	 * 
	 * @return
	 */
	public boolean isCycle() {
		return isCycle;
	}

	/**
	 * �����Ƿ��ֲ���Ĭ�ϲ��ֲ�,�ֲ�һ����ѭ����
	 * 
	 * @param isWheel
	 */
	public void setWheel(boolean isWheel) {
		this.isWheel = isWheel;
		isCycle = true;
		if (isWheel) {
			handler.postDelayed(runnable, time);
		}
	}

	/**
	 * �Ƿ����ֲ�״̬
	 * 
	 * @return
	 */
	public boolean isWheel() {
		return isWheel;
	}

	final Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (getActivity() != null && !getActivity().isFinishing() && isWheel) {
				long now = System.currentTimeMillis();
				// �����һ�λ���ʱ���뱾��֮���Ƿ��д���(�ֻ���)�������еĻ��ȴ��´��ֲ�
				if (now - releaseTime > time - 500) {
					handler.sendEmptyMessage(WHEEL);
				} else {
					handler.sendEmptyMessage(WHEEL_WAIT);
				}
			}
		}
	};

	/**
	 * �ͷ�ָʾ���߶ȣ���������֮ǰָʾ���������˸߶ȣ��˴��ͷ�
	 */
	public void releaseHeight() {
		getView().getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
		refreshData();
	}

	/**
	 * �����ֲ���ͣʱ�䣬��û�������л�����һ����ͼ.Ĭ��5000ms
	 * 
	 * @param time
	 *            ����Ϊ��λ
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * ˢ�����ݣ����ⲿ��ͼ���º�֪ͨˢ������
	 */
	public void refreshData() {
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}

	/**
	 * ����CycleViewPager
	 */
	public void hide() {
		viewPagerFragmentLayout.setVisibility(View.GONE);
	}

	/**
	 * �������õ�viewpager
	 * 
	 * @return viewPager
	 */
	public BaseViewPager getViewPager() {
		return viewPager;
	}

	/**
	 * ҳ�������� ���ض�Ӧ��view
	 * 
	 * @author Yuedong Li
	 * 
	 */
	private class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public View instantiateItem(ViewGroup container, final int position) {
			ImageView v = imageViews.get(position);
			if (mImageCycleViewListener != null) {
				v.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mImageCycleViewListener.onImageClick(infos.get(currentPosition - 1), currentPosition, v);
					}
				});
			}
			container.addView(v);
			return v;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (arg0 == 1) { // viewPager�ڹ���
			isScrolling = true;
			return;
		} else if (arg0 == 0) { // viewPager��������
			if (parentViewPager != null)
				parentViewPager.setScrollable(true);

			releaseTime = System.currentTimeMillis();

			viewPager.setCurrentItem(currentPosition, false);

		}
		isScrolling = false;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		int max = imageViews.size() - 1;
		int position = arg0;
		currentPosition = arg0;
		if (isCycle) {
			if (arg0 == 0) {
				currentPosition = max - 1;
			} else if (arg0 == max) {
				currentPosition = 1;
			}
			position = currentPosition - 1;
		}
		setIndicator(position);
	}

	/**
	 * ����viewpager�Ƿ���Թ���
	 * 
	 * @param enable
	 */
	public void setScrollable(boolean enable) {
		viewPager.setScrollable(enable);
	}

	/**
	 * ���ص�ǰλ��,ѭ��ʱ��Ҫע�ⷵ�ص�position����֮ǰ��views��ǰ������󷽼������ͼ������ǰҳ����ͼ��views���ϵ�λ��
	 * 
	 * @return
	 */
	public int getCurrentPostion() {
		return currentPosition;
	}

	/**
	 * ����ָʾ��
	 * 
	 * @param selectedPosition
	 *            Ĭ��ָʾ��λ��
	 */
	private void setIndicator(int selectedPosition) {
		for (int i = 0; i < indicators.length; i++) {
			indicators[i].setBackgroundResource(R.drawable.icon_point);
		}
		if (indicators.length > selectedPosition)
			indicators[selectedPosition].setBackgroundResource(R.drawable.icon_point_pre);
	}

	/**
	 * �����ǰҳ��Ƕ������һ��viewPager�У�Ϊ���ڽ��й���ʱ��ϸ�ViewPager����������
	 * ��ֹ��ViewPager�����¼�
	 * ��ViewPager��Ҫʵ��ParentViewPager�е�setScrollable����
	 */
	public void disableParentViewPagerTouchEvent(BaseViewPager parentViewPager) {
		if (parentViewPager != null)
			parentViewPager.setScrollable(false);
	}

	/**
	 * �ֲ��ؼ��ļ����¼�
	 * 
	 * @author minking
	 */
	public static interface ImageCycleViewListener {

		/**
		 * ����ͼƬ�¼�
		 * 
		 * @param position
		 * @param imageView
		 */
		public void onImageClick(ADInfo info, int postion, View imageView);
	}
}