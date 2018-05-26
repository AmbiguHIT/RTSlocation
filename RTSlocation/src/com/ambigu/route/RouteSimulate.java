package com.ambigu.route;

import java.util.ArrayList;

import com.ambigu.model.DrivingScheme;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import android.content.Context;
import android.widget.Toast;

/**
 * 璺嚎瑙勫垝
 * 
 * @author hgy
 * 
 */
public class RouteSimulate implements OnGetRoutePlanResultListener{

	private BaiduMap bdMap;

	private LatLng startPlace;// 寮�濮嬪湴鐐�
	private LatLng endPlace;// 缁撴潫鍦扮偣
	private LatLng midPlace;// 缁撴潫鍦扮偣
	private RoutePlanSearch routePlanSearch;// 璺緞瑙勫垝鎼滅储鎺ュ彛

	private Context context;
	private boolean isClear=false;
	
	private ArrayList<LatLng> pointList;
	public RouteSimulate(Context context, LatLng startPlace, LatLng endPlace,boolean isClear) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.startPlace = startPlace;
		this.endPlace = endPlace;
		this.isClear=isClear;
	}

	public RouteSimulate(Context context, LatLng startPlace, LatLng endPlace, LatLng mid) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.startPlace = startPlace;
		this.endPlace = endPlace;
		this.midPlace=mid;
	}

	/**
	 * 
	 */
	public void init(MapView mapView, DrivingScheme type) {
		mapView.showZoomControls(true);
		bdMap = mapView.getMap();
		routePlanSearch = RoutePlanSearch.newInstance();
		doClick(type);

		/**
		 * 璺嚎瑙勫垝缁撴灉鍥炶皟
		 */
		routePlanSearch.setOnGetRoutePlanResultListener(this);
	}

	/**
	 * 椹捐溅绾胯矾鏌ヨ 涓嬫媺鑿滃崟鐨勫洓涓睘鎬э細韬查伩鎷ュ牭,鏈�鐭窛绂�,杈冨皯璐圭敤,鏃堕棿浼樺厛
	 */
	private void drivingSearch() {
		DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
		PlanNode st=PlanNode.withLocation(startPlace);
		PlanNode ed=PlanNode.withLocation(endPlace);
		drivingOption.from(st);// 璁剧疆璧风偣
		drivingOption.to(ed);// 璁剧疆缁堢偣
		routePlanSearch.drivingSearch(drivingOption);// 鍙戣捣椹捐溅璺嚎瑙勫垝
	}

	/**
	 * 鎹箻璺嚎鏌ヨ
	 */
	private void transitSearch(int index) {
		TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
		transitOption.city("鍖椾含");// 璁剧疆鎹箻璺嚎瑙勫垝鍩庡競锛岃捣缁堢偣涓殑鍩庡競灏嗕細琚拷鐣�
		transitOption.from(PlanNode.withLocation(startPlace));// 璁剧疆璧风偣
		transitOption.to(PlanNode.withLocation(endPlace));// 璁剧疆缁堢偣
		routePlanSearch.transitSearch(transitOption);
	}

	/**
	 * 姝ヨ璺嚎鏌ヨ
	 */
	private void walkSearch() {
		WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
		walkOption.from(PlanNode.withLocation(startPlace));// 璁剧疆璧风偣
		walkOption.to(PlanNode.withLocation(endPlace));// 璁剧疆缁堢偣
		routePlanSearch.walkingSearch(walkOption);
	}
	
	

	public void doClick(DrivingScheme type) {
		switch (type) {
		case DRIVING:// 椹捐溅
			drivingSearch();
			break;
		case WALKING:// 鎹箻
			walkSearch();
			break;
		default:
			break;
		}
	}
	
	
	 private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
	        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
	            super(baiduMap);
	        }

	        @Override
	        public BitmapDescriptor getStartMarker() {
	            return null;
	        }

	        @Override
	        public BitmapDescriptor getTerminalMarker() {
	            return null;
	        }


	    }


	@Override
	public void onGetBikingRouteResult(BikingRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
		// TODO Auto-generated method stub
		if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(context, "鎶辨瓑锛屾湭鎵惧埌缁撴灉", Toast.LENGTH_SHORT).show();
		}
		if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 璧风粓鐐规垨閫旂粡鐐瑰湴鍧�鏈夊矏涔夛紝閫氳繃浠ヤ笅鎺ュ彛鑾峰彇寤鸿鏌ヨ淇℃伅
			// drivingRouteResult.getSuggestAddrInfo()
			return;
		}
		if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
			DrivingRouteOverlay drivingRouteOverlay = new MyDrivingRouteOverlay(bdMap);
			drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));// 璁剧疆涓�鏉￠┚杞﹁矾绾挎柟妗�

			bdMap.setOnMarkerClickListener(drivingRouteOverlay);
			drivingRouteOverlay.addToMap(isClear);
			//drivingRouteOverlay.zoomToSpan();
			//pointList=drivingRouteOverlay.getPointList();
		}
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<LatLng> getPointList(){
		return pointList;
	}

	@Override
	public void onGetIndoorRouteResult(IndoorRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetMassTransitRouteResult(MassTransitRouteResult arg0) {
		// TODO Auto-generated method stub
		
	}


}
