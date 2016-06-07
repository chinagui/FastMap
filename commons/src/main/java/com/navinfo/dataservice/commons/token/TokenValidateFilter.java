package com.navinfo.dataservice.commons.token;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
			String servletPath = httpRequest.getServletPath();
			System.out.print(servletPath );
			System.out.print(excludeUrlPattern );
			String tokenString = request.getParameter("token");
			AccessToken token = AccessTokenFactory.validate(tokenString);
			request.setAttribute("token", token);
			chain.doFilter(request, response);
		}catch(TokenExpiredException te){
			throw new ServletException("Token已过期");
		}catch(Exception e){
			throw new ServletException("验证Token时发生错误");
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
