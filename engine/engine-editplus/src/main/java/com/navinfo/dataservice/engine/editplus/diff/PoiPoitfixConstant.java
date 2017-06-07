package com.navinfo.dataservice.engine.editplus.diff;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author wangxiaojian
 * 名称后缀常量
 */
public class PoiPoitfixConstant {
	
	public static Map<String,Long>  postfixMap= new HashMap<String,Long>();
	static {
				
		postfixMap.put("超市", (long) 1);
		postfixMap.put("商场", (long) 1);
		postfixMap.put("购物中心", (long) 1);
		postfixMap.put("商贸中心", (long) 1);
		postfixMap.put("便利店", (long) 1);
		postfixMap.put("平价店", (long) 1);
		postfixMap.put("旗舰店", (long) 1);
		postfixMap.put("连锁店", (long) 1);
		postfixMap.put("专卖店", (long) 1);
		postfixMap.put("专卖", (long) 1);
		postfixMap.put("专营店", (long) 1);
		postfixMap.put("销售店",1L);
		postfixMap.put("经营部",1L);
		postfixMap.put("批发部",1L);
		postfixMap.put("分店",1L);
		postfixMap.put("商行",1L);
		postfixMap.put("路店",1L);
		postfixMap.put("商店",1L);
		postfixMap.put("服饰",1L);
		postfixMap.put("百货",1L);
		postfixMap.put("品店",1L);
		postfixMap.put("烟酒",1L);
		postfixMap.put("电器",1L);
		postfixMap.put("总店",1L);

		postfixMap.put("咖啡店", (long)3);

		postfixMap.put("书屋", (long)4);
		postfixMap.put("书店", (long)4);
		
		postfixMap.put("停车场", (long)5);
		
		postfixMap.put("学校", (long)6);
		postfixMap.put("大学", (long)6);
		postfixMap.put("中学", (long)6);
		postfixMap.put("高中", (long)6);
		postfixMap.put("小学", (long)6);
		postfixMap.put("幼儿园", (long)6);
		
		postfixMap.put("宾馆", (long)7);
		postfixMap.put("旅馆", (long)7);
		postfixMap.put("旅行社", (long)7);
		postfixMap.put("客栈", (long)7);
		postfixMap.put("度假村", (long)7);
		postfixMap.put("招待所",7L);
		
		postfixMap.put("酒店", (long)8);
		postfixMap.put("饭店", (long)8);
		postfixMap.put("大排档", (long)8);
		postfixMap.put("烤肉", (long)8);
		postfixMap.put("食府", (long)8);
		postfixMap.put("菜馆", (long)8);
		postfixMap.put("海鲜坊", (long)8);
		postfixMap.put("烤肉坊", (long)8);
		postfixMap.put("家常菜", (long)8);
		postfixMap.put("酒楼",8L);

		postfixMap.put("餐厅", (long)9);
		
		postfixMap.put("麻辣香锅", (long)10);
	    postfixMap.put("火锅", (long)10);
		
		postfixMap.put("摄影店", (long)11);
	    postfixMap.put("摄影", (long)11);
	    postfixMap.put("影楼", (long)11);
	
	    postfixMap.put("典当行", (long)12);
	    
	
	    postfixMap.put("ATM", (long)13);
	    postfixMap.put("ＡＴＭ", (long)13);
	    postfixMap.put("ＡＴＭ取款机", (long)13);
	
	    postfixMap.put("分行", (long)14);
	    postfixMap.put("支行", (long)14);
	    postfixMap.put("银行", (long)14);
	  
	    postfixMap.put("公司", (long)15);

	    postfixMap.put("制造厂", (long)16);
	    postfixMap.put("生产车间", (long)16);
		
	    postfixMap.put("公园", (long)17);
	    postfixMap.put("风景区", (long)17);
		
	    postfixMap.put("产业园", (long)18);
	    postfixMap.put("科技园", (long)18);
	    postfixMap.put("孵化器", (long)18);
		
	    postfixMap.put("大楼", (long)19);
	    postfixMap.put("大厦", (long)19);
	    postfixMap.put("号楼",(long)19);
	  
	    postfixMap.put("公交车站", (long)20);
	   
	    postfixMap.put("地铁站", (long)21);
		
	    postfixMap.put("网吧", (long)22);
		
	    postfixMap.put("门诊", (long)23);
	    postfixMap.put("门诊部", (long)23);
	    postfixMap.put("诊所", (long)23);
	
	    postfixMap.put("厕所", (long)24);
	    postfixMap.put("厕", (long)24);
	    
	    postfixMap.put("医院", (long)25);
	    postfixMap.put("诊疗中心", (long)25);
	    postfixMap.put("急救中心", (long)25);
	    postfixMap.put("卫生院",25L);
	    
	    postfixMap.put("影视中心", (long)26);
	    postfixMap.put("电影城", (long)26);
	   
	    postfixMap.put("广场", (long)26);
		
	    postfixMap.put("造型", (long)27);
	    postfixMap.put("美容中心", (long)27);
	    postfixMap.put("理发店", (long)27);
	    postfixMap.put("美容院", (long)27);

	    postfixMap.put("服务中心", (long)28);
	    postfixMap.put("洗浴中心", (long)28);
	    postfixMap.put("洗脚城", (long)28);
	    postfixMap.put("足浴会馆", (long)28);
	    postfixMap.put("SPA", (long)28);
	    postfixMap.put("SPA院", (long)28);
	    postfixMap.put("SPA馆", (long)28);
	    postfixMap.put("生活馆",28L);
		postfixMap.put("养生馆",28L);
	    
	    postfixMap.put("KTV", (long)29);
	    postfixMap.put("歌厅", (long)29);
	    postfixMap.put("舞厅", (long)29);
	    postfixMap.put("ＫＴＶ",29L);

	    postfixMap.put("分局", (long)30);
		
		postfixMap.put("办公室",32L);
		postfixMap.put("写字楼",32L);
		
		postfixMap.put("制药厂",33L);
		
		postfixMap.put("数控设备厂",34L);
		
		postfixMap.put("大药房",34L);
		postfixMap.put("药店",52L);
		
		postfixMap.put("居委会",35L);
		postfixMap.put("委员会",35L);
		postfixMap.put("街道办事处",35L);
		
		postfixMap.put("会所",36L);
		postfixMap.put("休闲中心",36L);
		
		postfixMap.put("健身房",37L);
		
		postfixMap.put("体育",38L);
		postfixMap.put("体育馆",38L);
		postfixMap.put("球中心",38L);
		
		postfixMap.put("殡仪馆",39L);
		
		postfixMap.put("代表处",40L);
		postfixMap.put("驻京办",40L);
		postfixMap.put("联络处",40L);
		
		postfixMap.put("编辑部",41L);
		postfixMap.put("杂志社",41L);
		
		postfixMap.put("事务所",42L);
		
		postfixMap.put("游泳馆",43L);
		
		postfixMap.put("运营中心",44L);
		
		postfixMap.put("高速入口",46L);
		postfixMap.put("高速",46L);
		postfixMap.put("高速出口",46L);
		
		postfixMap.put("加油站",47L);
		
		postfixMap.put("俱乐部",48L);
		
		postfixMap.put("培训中心",49L);
		
		postfixMap.put("营业厅",50L);
		postfixMap.put("营业部",50L);
		postfixMap.put("营业所",50L);
		
		postfixMap.put("务中心",51L);
		
		postfixMap.put("派出所",52L);
		postfixMap.put("警厅",52L);
		
		postfixMap.put("报刊亭",53L);
		
		postfixMap.put("服务站",55L);
		
		postfixMap.put("分理处",56L);
		
		postfixMap.put("工作室",57L);
		
		postfixMap.put("信用社",58L);
		
		postfixMap.put("纪检委",59L);
		
		postfixMap.put("电动车",60L);
		
		postfixMap.put("总代理",61L);
		
		postfixMap.put("服务部",62L);
		
		postfixMap.put("修理厂",63L);
		
		postfixMap.put("政支局",64L);
		
		postfixMap.put("管理局",65L);
		
		postfixMap.put("装饰",66L);
		
		postfixMap.put("市场",67L);
		
		postfixMap.put("小区",68L);
		
		postfixMap.put("陶瓷",69L);
		
		postfixMap.put("协会",70L);
		
		postfixMap.put("政府",71L);
		
		postfixMap.put("广告",72L);
		
		postfixMap.put("房产",73L);
		postfixMap.put("地产",73L);
		
		
	}
	
}
