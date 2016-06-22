package com.navinfo.dataservice.expcore;

public class ExportConfig {
	
	public final static String MODE_FLEXIBLE="flexible";//定制导出，走db_link,只支持oracle到oracle
	public final static String MODE_FULL_COPY="full_copy";//整库复制，走db_link,只支持oracle到oracle
	public final static String MODE_COPY="copy";
	public final static String MODE_DELETE="delete";
	public final static String MODE_DELETE_COPY="delete_copy";//先在目标库上删除数据，再导入数据

	public final static String CONDITION_BY_MESH="mesh";
	public final static String CONDITION_BY_AREA="area";
	public final static String CONDITION_BY_POLYGON="polygon";

	public final static String FEATURE_ALL="all";
	public final static String FEATURE_POI="poi";
	public final static String FEATURE_LINK="link";
	public final static String FEATURE_FACE="face";
	public final static String FEATURE_CK="ck";
	public final static String FEATURE_GDB="gdb";

	public static final String DATA_INTEGRITY = "dataIntegrity";
	public static final String DATA_NOT_INTEGRITY = "dataNotIntegrity";

	public static final String WHEN_EXIST_IGNORE = "ignore";
	public static final String WHEN_EXIST_OVERWRITE = "overwrite";

}
