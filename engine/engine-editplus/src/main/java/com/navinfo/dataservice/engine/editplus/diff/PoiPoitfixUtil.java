package com.navinfo.dataservice.engine.editplus.diff;

/**
 * 
 * @author wangxiaojian
 * 后缀意群过滤辅助类
 */
public class PoiPoitfixUtil {
	public static Long getIndex(String t) {
		Long value=(long) -1;
		if(t==null)
		return value;

		if(t.length()>=4){
			value=PoiPoitfixConstant.postfixMap.get(t.substring(t.length()-4));
			if(value!=null)
				return value;
		}
		if(t.length()>=3){
			value=PoiPoitfixConstant.postfixMap.get(t.substring(t.length()-3));
			if(value!=null)
				return value;
		}
		if(t.length()>=2){
			value=PoiPoitfixConstant.postfixMap.get(t.substring(t.length()-2));
			if(value!=null)
				return value;
		}
		return -1L;
	}
	
public static boolean checkName(String s1,String s2)
{
	Long value1,value2;
	value1=getIndex(s1);
	value2=getIndex(s2);
	if(value1==-1L||value2==-1L)
		return true;
	return value1.equals(value2);
}
	
	public static void main(String[] args) {
		System.out.println(getIndex("广场1"));
		System.out.println(getIndex("万达广场"));
		System.out.println(getIndex("博泰嘉华大厦"));
		System.out.println(getIndex("电影城"));
		System.out.println(getIndex(" 电影城"));
		System.out.println(getIndex("周黑鸭连锁店"));
		System.out.println(getIndex("1殡仪馆"));
		System.out.println(checkName("1殡仪馆"," 殡仪馆"));
		System.out.println(checkName("1殡仪馆","周黑鸭"));
		System.out.println(checkName("1殡仪馆","周黑鸭连锁店"));
		System.out.println(checkName("1殡仪馆"," 123电影城"));
	}

}
