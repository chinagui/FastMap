package com.navinfo.dataservice.api.edit.model;

/** 
 * @ClassName: FmMesh4Lock 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 下午4:51:29 
 * @Description: TODO
 */
public class FmEditLock {
	public final static int TYPE_CHECK = 1;
	public final static int TYPE_BATCH = 2;
	public final static int TYPE_BORROW =3;
	public final static int TYPE_GIVE_BACK = 4;
	public final static int TYPE_DAY2MON = 5;
	public final static int LOCK_OBJ_POI=1;
	public final static int LOCK_OBJ_ROAD=2;
	public final static int LOCK_OBJ_ALL=0;
	
	public final static String DB_TYPE_DAY="DAY";
	public final static String DB_TYPE_MONTH="MONTH";
	public static final int TYPE_DEFAULT = 0;
}
