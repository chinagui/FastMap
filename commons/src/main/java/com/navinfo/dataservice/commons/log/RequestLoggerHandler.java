package com.navinfo.dataservice.commons.log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** 
* @ClassName: RequestLoggerHandler 
* @author Xiao Xiaowen 
* @date 2017年6月8日 下午9:10:01 
* @Description: TODO
*/
public class RequestLoggerHandler extends HandlerInterceptorAdapter {
    private static Logger log = Logger.getLogger(RequestLoggerHandler.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<Long>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTime.set(System.currentTimeMillis());
        log.info("[http]preHandle url:{"+request.getRequestURL()+"}, params: {"+request.getQueryString()+"}" );
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
    	
        Long causeTime = System.currentTimeMillis() - startTime.get();
        log.info("[http]postHandle url: {"+request.getRequestURL()+"}, used:"+causeTime+"ms");
        startTime.remove();
        modelAndView.addObject("time_consuming(ms)", causeTime);
        if(modelAndView !=null&&modelAndView.getModel()!=null){
            String errmsg = String.valueOf(modelAndView.getModel().get("errmsg"));
            modelAndView.getModel().put("errmsg", "[请求编号："+ThreadLogToken.getInstance().get()+"]"+errmsg);
        }
        ThreadLogToken.getInstance().remove();
    }
}
