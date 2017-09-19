package com.navinfo.dataservice.engine.limit.glm.iface;

/**
 * 对象状态
 */
public enum ObjStatus {

	INSERT, DELETE, UPDATE, INITIALIZE;
	private static final String CHI_INSERT = "新增";
	private static final String CHI_UPDATE = "修改";
	private static final String CHI_DELETE = "删除";

	public static String getCHIName(ObjStatus objStatus) {
		if (objStatus == ObjStatus.INSERT) {
			return CHI_INSERT;
		}
		if (objStatus == ObjStatus.UPDATE) {
			return CHI_UPDATE;
		}
		if (objStatus == ObjStatus.DELETE) {
			return CHI_DELETE;
		}
		return null;
	}

}
