package com.navinfo.dataservice.engine.fcc.tips;

public class TipsSelectorUtils {
	
	public static String convertElecEyeKind(int kind){
		
		switch(kind){
		
		case 1:return "限速";
		case 13:return "非机动车道";
		case 15:return "公交车道";
		case 16:return "禁止左右转";
		case 20:return "区间测速开始";
		case 21:return "区间测速结束";
		}
		
		return null;
	}
	
	

	public static String convertElecEyeLocation(int loc){
		switch(loc){
		case 0:return "未调查";
		case 1:return "左";
		case 2:return "右";
		case 4:return "上";
		}
		
		return null;
	}
	
	public static String convertUsageFeeType(int tp){
		switch(tp){
		case 2:return "桥";
		case 3:return "隧道";
		}
		return null;
	}
	
	public static String convertUsageFeeVehicleType(int vt){
		switch(vt){
		case 1:return "客车";
		case 2:return "配送卡车";
		case 3:return "运输卡车";
		case 5:return "出租车";
		case 6:return "公交车";
		}
		
		return null;
	}
}
