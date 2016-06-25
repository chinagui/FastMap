package com.navinfo.dataservice.commons.token;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.navinfo.dataservice.commons.util.ResponseUtils;

/** 
* @ClassName: TokenValidateFilter 
* @author Xiao Xiaowen 
* @date 2016年5月23日 下午18:21:51 
* @Description: TODO
*/
public class TokenValidateFilter implements Filter {
	private String excludeUrlPattern;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//获取本filter不过滤的url
		this.excludeUrlPattern = filterConfig.getInitParameter("exclude-url-pattern"); 
		

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try{
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			String requestUri = httpRequest.getRequestURI();
			Pattern p = Pattern.compile(this.excludeUrlPattern);
	        Matcher m = p.matcher(requestUri);
	        if (!m.find()){//只有请求的uri需要进行token验证的，才进行token验证
	        	String tokenString = request.getParameter("access_token");
				AccessToken token = AccessTokenFactory.validate(tokenString);
				request.setAttribute("token", token);
	        }			
			chain.doFilter(request, response);
		}catch(TokenExpiredException te){
			response.getWriter().println(
					ResponseUtils.assembleFailResult("登陆已过期，请重新登陆",-100));
		}catch(Exception e){
			response.getWriter().println(
					ResponseUtils.assembleFailResult("验证Token时发生未知错误",-100));
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
