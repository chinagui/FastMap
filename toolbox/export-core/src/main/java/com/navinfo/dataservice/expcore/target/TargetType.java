package com.navinfo.dataservice.expcore.target;

/** 
 * @ClassName: TargetType 
 * @author Xiao Xiaowen 
 * @date 2015-10-29 下午4:27:30 
 * @Description: TODO
 *  
 */
public enum TargetType {
	ORACLE(1),
	SQLITE(2),
	XMLFILE(11),
	JSONFILE(12);
	private final int value;
	public int getValue(){
		return value;
	}
	TargetType(int value){
		this.value=value;
	}
}
