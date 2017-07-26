package com.navinfo.dataservice.dao.fcc;

/** 
 * @ClassName: TipsWorkStatus.java
 * @author y
 * @date 2017-5-26 下午1:53:35
 * @Description: TODO
 *  
 */
public  class TipsWorkStatus {

    public static int PREPARED_WORKING = 0;//待作业

    public static int WORK_HAS_PROBLEM = 1; //有问题待确认

    public static int WORK_HAS_FINISHED = 2;//已作业

    public static int PREPARED_CHECKING = 4;//待质检

    public static int CHECK_HAS_FINISHED = 5;//已质检

    public static int CHECK_HAS_PROBLEM = 6;//日编质检有问题待确认

    public static int ALL = 9;//全部

    public static int CHECK_ALL = 10;//质检全部

    public static int TIPS_IN_TASK = 11;//质检全部
}
