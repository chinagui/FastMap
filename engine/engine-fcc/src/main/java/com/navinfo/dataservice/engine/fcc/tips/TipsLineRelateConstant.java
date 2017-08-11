package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TipsStatConstant.java
 * @author y
 * @date 2017-4-19 下午7:18:41
 * @Description: tips关联关系配置（tips与link/测线的关联关系）
 * 
 * 注意：没新增一类tips都需要在这里增加
 * 
 */
public class TipsLineRelateConstant {

	
	//注意区分ln和in
	public static  List<String> noneRelate = new ArrayList<>(); // 没有关联关系(没有什么意义，主要是为了保证每种tips都有配置)
	public static  List<String> simpleF = new ArrayList<>(); // f.id
	public static  List<String> simpleIn = new ArrayList<>(); // in.id
	public static  List<String> simpleOut = new ArrayList<>(); //out.id
	public static  List<String> simpleExp = new ArrayList<>(); //exp.id
	public static  List<String> f_array_Id = new ArrayList<>(); // [f_array].id
	public static  List<String> f_array_F = new ArrayList<>(); // [f_array].f.id  (f唯一是对象)
	public static  List<String> complex_1 = new ArrayList<>(); // 复杂关系（in.id+[o_array].out.id）
	public static  List<String> complex_2 = new ArrayList<>(); // 复杂关系-车信（in.id+[o_array].[d_array].out.id(out是对象)）
	public static  List<String> complex_3 = new ArrayList<>(); // 复杂关系-公交车道（[ln].[o_array].id+[f_array].id)
	public static  List<String> complex_4 = new ArrayList<>(); // 复杂关系-（可变导向车道）（f.id+[ln].[o_array].out.id)
	public static  List<String> complex_5 = new ArrayList<>(); // 复杂关系（in.id+out.id)
	public static  List<String> complex_6 = new ArrayList<>(); // 复杂关系in.id+[o_array].[out].id   【out是数组】
	

	static {
		
		//1.没有关联关系的
		noneRelate.add("2001");//测线
		noneRelate.add("1705");//桥
		noneRelate.add("1901");//道路名
		noneRelate.add("1803");//（挂接）
		noneRelate.add("8001");//（FC）
		noneRelate.add("8005");//（机场功能面）
		
		noneRelate.add("8006");//（Highway道路名）
		noneRelate.add("8010");//（BUA）
		
		noneRelate.add("8007");//（AOI面）
		noneRelate.add("8008");//（AOI代表点）
		noneRelate.add("8009");//（地铁）
		noneRelate.add("8009");//（ADAS测线）
		noneRelate.add("1708");//（ADAS打点）
		noneRelate.add("2201");//（地下通道/过街天桥）
		noneRelate.add("1709");//（点位移）
		

		//2. f.id
		simpleF.add("2101");//（删除道路标记）
		simpleF.add("1201"); // 种别
		simpleF.add("1202"); // 车道数
		
		simpleF.add("1205"); // SA
		simpleF.add("1206"); // PA
		simpleF.add("1207"); // 匝道
		simpleF.add("1208"); // 停车场出入口Link
		//simpleF.add("1209"); // 航线，规格已删除
		simpleF.add("1704"); // 路口名称
		simpleF.add("1214"); // 删除在建属性
		
		simpleF.add("1701"); //1701（障碍物）
		simpleF.add("1702"); // 1702（铁路道口）
		simpleF.add("1706"); // 1706（GPS打点）
		
		simpleF.add("1707"); // 1706（里程桩）
		simpleF.add("1115"); // 1115（车道变化点） inLink、outLink ？？要不要考虑
		simpleF.add("1304");//1304 （禁止穿行）
		simpleF.add("1305");//1304（禁止驶入）
		simpleF.add("1308");//1304（禁止卡车驶入）
		simpleF.add("1203"); // （道路方向）
		
		simpleF.add("1101"); // 点限速
		simpleF.add("1114"); // （卡车限速）
		simpleF.add("1111"); // （条件限速）
		simpleF.add("1113"); // （车道限速）
		simpleF.add("1109"); // （电子眼）
		simpleF.add("1110"); // （卡车限制）
		simpleF.add("1211"); //  （高速连接路）
		
		
		
		
		
		//3.in.id
		simpleIn.add("1103");//（红绿灯方位）
		simpleIn.add("1105");//（危险信息）
		simpleIn.add("1108");//（减速带）
		
		//4.out.id
		simpleOut.add("1106");//（坡度）
		
		//5.复杂关系-in out (in.id+[o_array].out.id)
		complex_1.add("1703");//分叉口提示 (in.id+[o_array].out.id)
		complex_1.add("1407");//（高速分歧） (in.id+[o_array].out.id)
		complex_1.add("1405");//(一般道路方面) (in.id+[o_array].out.id)
		complex_1.add("1406");//(实景图) (in.id+[o_array].out.id)
		
		
		complex_1.add("1401");//(方向看板) (in.id+[o_array].out.id)
		complex_1.add("1402");//（Real Sign） (in.id+[o_array].out.id)
		complex_1.add("1403");//(3D标记)） (in.id+[o_array].out.id)
		complex_1.add("1404");//（提左提右） (in.id+[o_array].out.id)
		complex_1.add("1409");//(普通路口模式图) (in.id+[o_array].out.id)
		complex_1.add("1410");//(高速入口模式图) (in.id+[o_array].out.id)
		
		
		//6.复杂关系-车信（in.id+[o_array].[d_array].out.id(out是对象)）
		complex_2.add("1301");//车信
		

		
		//7.复杂关系-公交车道（[ln].[o_array].id+[f_array].id)
		complex_3.add("1310"); //（公交车道）
		
		//8.复杂关系-可变导向车道（f.id+[ln].[o_array].out.id)
		complex_4.add("1311");//（可变导向车道）
		
		
		complex_5.add("1112");//1112 （可变限速）in.id+out.id
		complex_5.add("1104");//1104 （大门）in.id+out.id
		complex_5.add("1107");//1107 （收费站）in.id+out.id
		complex_5.add("1804");//（提取顺行））in.id+out.id
		
		
		//9.[f_array].id
		f_array_Id.add("1604");//（区域内道路） [f_array].id
		f_array_Id.add("1605");//(POI连接路) [f_array].id
		f_array_Id.add("1606");//(收费开放道路) [f_array].id
		
		f_array_Id.add("1601");//（环岛） [f_array].id
		f_array_Id.add("1602");//(特殊交通类型) [f_array].id
		f_array_Id.add("1602");//（风景路线) [f_array].id
		
		f_array_Id.add("1116");//（立交） [f_array].id
		f_array_Id.add("1518");//（阶梯） [f_array].id
		
		f_array_Id.add("1510"); //  桥 [f_array].id
		f_array_Id.add("1511"); //  隧道 [f_array].id
		f_array_Id.add("1509"); //  跨线立交桥 [f_array].id
		
		f_array_Id.add("1514"); //  施工 [f_array].id
		f_array_Id.add("1515"); //  维修 [f_array].id
		f_array_Id.add("1516"); //  季节性关闭 [f_array].id
		
		f_array_Id.add("1502"); //  1502（路面无覆盖） [f_array].id
		f_array_Id.add("1503"); //  1503（高架路） [f_array].id
		f_array_Id.add("1504"); //  1504(Overpass） [f_array].id
		f_array_Id.add("1505"); //  1505(Underpass) [f_array].id
		f_array_Id.add("1506"); //  1506(私道) [f_array].id
		f_array_Id.add("1508"); //  1508(公交专用道) [f_array].id
		f_array_Id.add("1513"); //  1513(窄道) [f_array].id
		
		f_array_Id.add("1507"); // 1507（步行街）[f_array].id
		f_array_Id.add("1512"); // 1512（辅路）[f_array].id
		f_array_Id.add("1517"); // 1517 （Usage fee）[f_array].id
		f_array_Id.add("1501"); // 1501 （上下线分离）[f_array].id
		f_array_Id.add("1204"); // 1204 （可逆车道）[f_array].id
		
		
		f_array_Id.add("1116"); // 立交
		f_array_Id.add("1501"); // 上下线分离 [f_array].id
		f_array_Id.add("1508");// 18.公交专用道 [f_array].id
		f_array_Id.add("1601");// 22. 环岛 [f_array].id
		f_array_Id.add("1604"); // 23. 区域内道路 [f_array].id
		
		f_array_Id.add("1520"); // 24.在建时间变更 [f_array].id
		
		
		
		//10.in.id+[o_array].[out].id 【out是数组】
		complex_6.add("1302"); //（普通交限标记） in.id+[o_array].[out].id
		complex_6.add("1303"); //（卡车交限标记) in.id+[o_array].[out].id
		complex_6.add("1306"); //（路口语音引导） in.id+[o_array].[out].id
		
		//11.exp.id
		simpleExp.add("1307");//（自然语音引导） exp.id
		
		//12.[f_array].f.id  (f唯一是对象)
		f_array_F.add("1102");//（红绿灯）
		

	}

}
