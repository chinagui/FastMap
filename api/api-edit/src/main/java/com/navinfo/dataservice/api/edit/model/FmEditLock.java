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
	public final static int TYPE_COMMIT = 5;//日库上表示日库->月库、月库上表示月库->GDB+
	public final static int TYPE_RELEASE = 6;
	public final static int TYPE_EDIT_POI_BASE_RELEASE =7;//POI行编提交锁
	public static final int TYPE_DEFAULT = 0;
	
	public final static int LOCK_OBJ_POI=1;
	public final static int LOCK_OBJ_ROAD=2;
	public final static int LOCK_OBJ_ALL=0;
	
	public final static String DB_TYPE_DAY="DAY";
	public final static String DB_TYPE_MONTH="MONTH";
	
	protected int lockSeq;
	protected String dbType;
	public FmEditLock(int lockSeq,String dbType){
		this.lockSeq=lockSeq;
		this.dbType=dbType;
	}
	public int getLockSeq() {
		return lockSeq;
	}
	public void setLockSeq(int lockSeq) {
		this.lockSeq = lockSeq;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
}
