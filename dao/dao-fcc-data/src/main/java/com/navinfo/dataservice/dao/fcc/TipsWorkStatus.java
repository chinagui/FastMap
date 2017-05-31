package com.navinfo.dataservice.dao.fcc;

/** 
 * @ClassName: TipsWorkStatus.java
 * @author y
 * @date 2017-5-26 下午1:53:35
 * @Description: TODO
 *  
 */
public  class TipsWorkStatus {

		public static int PREPARED_WORKING = 1;//待作业

		public static int WORK_HAS_PROBLEM = 2; //有问题待确认

		public static int WORK_HAS_FINISHED = 3;//已作业
		
		public static int PREPARED_CHECKING = 4;//待质检
		
		public static int CHECK_FINISHED = 5;//已质检
		
		public static int CHECK_HAS_FINISHED = 6;//已质检
		
		public static int CHECK_HAS_PROBLEM = 7;//日编质检有问题待确认

		public static int ALL = 9;//全部
}
