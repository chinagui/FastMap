package com.navinfo.navicommons.chain;

import java.util.List;

import com.navinfo.navicommons.exception.ThreadExecuteException;

/**
 * 多线程按步骤执行sql
 *
 * @author liuqing
 */
public class ThreadExecuteExceptionCheckFilter extends AbstractFilter {
    

    @SuppressWarnings("unchecked")
    public boolean doExecute() throws Exception {
        if (!chainContext.isAppRunning()) {
            //由于子线程异常终止，所以本次导出失败，抛出异常
            List<Exception> exceptions = chainContext.getThreadExceptionList();
            
            for (Exception e : exceptions) {
                log.error("错误信息" + e.getMessage(), e);
            }
            throw new ThreadExecuteException("子线程执行失败，请检查日志");
        }
        return false;
    }


}
