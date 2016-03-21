package com.navinfo.navicommons.chain;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import org.apache.log4j.Logger;


/**
 * 抽象类，实现线程和主线程的控制
 *
 * @author liuqing
 */
public abstract class AbstractFilter implements Filter {




    protected Logger log = Logger.getLogger(getClass());

    protected static String SHARED_OBJECT_LOCK = "SHARED_OBJECT_LOCK";
    protected ChainContext chainContext;


    public boolean execute(Context context) throws Exception {
        this.chainContext = (ChainContext) context;

        return doExecute();
    }


    /**
     * 确保此处一定会执行
     *
     * @param context
     * @param exception
     * @return
     */
    public boolean postprocess(Context context, Exception exception) {
        return false;
    }

    public abstract boolean doExecute() throws Exception;


}
