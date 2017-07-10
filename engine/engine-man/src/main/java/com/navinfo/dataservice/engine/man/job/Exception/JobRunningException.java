package com.navinfo.dataservice.engine.man.job.Exception;

/**
 * Created by wangshishuai3966 on 2017/7/8.
 */
public class JobRunningException extends Exception {
    public JobRunningException(){
        super("执行中的任务不允许重复执行！");
    }
}
