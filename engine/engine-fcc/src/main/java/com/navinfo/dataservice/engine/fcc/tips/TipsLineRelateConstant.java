package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: TipsStatConstant.java
 * @author y
 * @date 2017-4-19 下午7:18:41
 * @Description: copy  from robot
 *  
 */
public class TipsLineRelateConstant {
	
	 public static final List<String> simpleF = new ArrayList<>(); //f.id
	    public static final List<String> f_array_Id = new ArrayList<>(); //[f_array].id
	    public static final List<String> f_array_F = new ArrayList<>(); // [f_array].f.id (f唯一是对象)
    	
	    public static final List<String> filterTipsType = new ArrayList<>();

	    static{
	    	
	    	//f.id
	    	simpleF.add("1201"); //种别
	    	simpleF.add("1202"); //车道数
	    	simpleF.add("1203"); //（道路方向）
	    	simpleF.add("1205"); //SA
	    	simpleF.add("1206"); //PA
	    	simpleF.add("1207"); //匝道
	    	simpleF.add("1211"); //IC JCT
	    	simpleF.add("1101"); // 点限速 
	    	simpleF.add("1702"); // 24. 铁路道口 f.id
	    	
	    	
	    	//[f_array].id 
	    	f_array_Id.add("1116"); //立交
	    	f_array_Id.add("1501"); //上下线分离 [f_array].id 
	    	f_array_Id.add("1507");  //17.步行街 [f_array].id 
	    	f_array_Id.add("1508");// 18.公交专用道 [f_array].id 
	    	f_array_Id.add("1510"); // 19. 桥 [f_array].id 
	    	f_array_Id.add("1511"); // 20. 隧道 [f_array].id 
	    	f_array_Id.add("1514"); // 21.施工 [f_array].id 
	    	f_array_Id.add("1601");// 22. 环岛 [f_array].id 
	    	f_array_Id.add("1604"); // 23. 区域内道路 [f_array].id 
	    	
	    	
	   /* 	
	    	
	    	
	    	//[f_array].f.id
	    	f_array_F.add("1102"); //红绿灯
	    	
	    	
	    	
			case "1107":// 12.收费站 in.id+out.id ??
				return updateTollgateTips();
				return updateSpeedLimitTips();
			case "1301":// 14. 车道信息   复杂的----？？
				return updateRdLaneTips();
			case "1302":// 15. 普通交限  复杂的----？？
				return updateRestrictionTips();
				return updateUpDownSeparateLine();
				return updateWalkStreetTips();*/
	    }

}
