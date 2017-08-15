package com.navinfo.navicommons.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.navinfo.dataservice.commons.springmvc.WebApplicationContextHolder;
import com.navinfo.navicommons.security.manager.ResourseManager;

/***
 * 
 * @author 杨小军
 * 
 * @version 1.0
 * 
 *          过滤器,取到当前用户在当前子系统中有哪些功能权限
 * 
 */
public class ResourseFilter implements Filter {
	private static ResourseManager resourseManager = null;
	private ServletContext servletContext;

	public void destroy() {

	}

	/**
	 * 权限控制过滤器,需要判断此路径是否需要对权限进行控制:如果路径包含/SPRINGMVC/UNSECURITY/则这种资源不受保护
	 * 
	 * 
	 * 
	 * 没有权限的话,进行重定向,这里又个特殊情况,就是根目录的时候,访问index页面
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		// 获取网站根目录
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String path = httpRequest.getContextPath()+"/";
		// 获取CAS中的用户名
		HttpSession session = httpRequest.getSession();
		String userName = session.getAttribute(
				"edu.yale.its.tp.cas.client.filter.user").toString();
		
		try {
			String requestUri = (httpRequest).getRequestURI();

			// 如果访问得路径包括/SPRINGMVC/UNSECURITY/则代表这个页面不需要过滤,直接跳过()
			if (!(requestUri.indexOf("/springmvc/unsecurity/") > 0)&&!"admin".equals(userName)) {
				
				String url;
				String type;//0代表是应用级别的，1为资源级别的
				String app = "";//如果是资源级别的，需要把应用的名称传过去，以便判断不同应该，同样的MAPPING 的情况
				
				//判断路径是否需要经过SERVLTE的分发，如果不经过，则只要验证当前用户或所在角色是否有当前应用的权限即可
				if(requestUri.indexOf("/springmvc/")<0)
				{
					url = path;
					type = "0";
				}else{
					if(requestUri.indexOf("?")>0)
						requestUri = requestUri.split("\\?")[0];
					url = requestUri.replace(path+"springmvc", "");
					type = "1";
					app = path;
				}
				//资源级别的才去判断资源权限，如果是应用的，则只要用户是存在的，则都可以进入应用的欢迎页面
				if("1".equals(type))
					if (!haveResourse(url,userName,type,app)) {
						httpRequest.getRequestDispatcher("/noPower.html").forward(httpRequest, httpResponse);
						return ;
					}
			}
			
			filterChain.doFilter(request, response);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void init(FilterConfig config) throws ServletException {
		this.servletContext = config.getServletContext();
	}

	private ResourseManager getResourseManager() {
		if (resourseManager == null) {
			WebApplicationContext wac = WebApplicationContextHolder
					.getWebApplicationContext(servletContext);
			resourseManager = (ResourseManager) wac.getBean("resourseManager");
			Assert.notNull(resourseManager);
		}
		return resourseManager;

	}

	/**
	 * 根据当前资源的路径，及用户名，去数据库里面查询是否有这个资源
	 * 
	 * @param arg0
	 *            用来取当前请求的url
	 * @return list<String resourse>
	 */
	public boolean haveResourse(String url,String userName,String type,String app) throws Exception {
		boolean hasResourse = getResourseManager().findResourseForUserName(userName,
				url,type,app);
		return hasResourse;
	}


}
