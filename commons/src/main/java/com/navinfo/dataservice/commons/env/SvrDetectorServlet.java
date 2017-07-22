package com.navinfo.dataservice.commons.env;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mayunfei
 * 用于探测服务环境是否正常启动的servlet
 */
public class SvrDetectorServlet extends HttpServlet{
	private static final String SVR_WORK_MSG = "svr works!";
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.getWriter().write(SVR_WORK_MSG);
    }
}