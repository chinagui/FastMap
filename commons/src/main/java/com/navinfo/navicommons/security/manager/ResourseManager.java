package com.navinfo.navicommons.security.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.navinfo.navicommons.database.SqlNamedQuery;

/**
 * 权限方面的一些对库的操作
 * 
 * @author 杨小军
 * 
 */
@Service
@Transactional(readOnly = true)
public class ResourseManager {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 根据用户名及请求的URL，返回此用户 及 其所以角色 是否有当前URL 的权限
	 * 
	 * 
	 * @param userName
	 * @param url
	 * @param type，0,应用级别，如某些应用的欢迎页面<即不走SERVLET的分发>，
	 * 				1,资源级别，即走SERVLET的分发的	
	 * @return
	 * @throws Exception
	 */
	public boolean findResourseForUserName(String userName, String url,String type,String app)
			throws Exception {
		String condition = "";
		
		if("0".equals(type))
		{
			url = url.replace("/", "");
			condition = "and r.app_domain_name = ?";
		}else
		{
			condition = "and r.url = ?";
			app = app.replace("/", "");
			condition += " and r.app_domain_name = '"+app+"'";
		}
		
		String userAppcountSql = SqlNamedQuery.getInstance().getSql(
				"authresoursemanager.findauthresourseforusername.count");

		userAppcountSql += condition;
		int userAppCount = jdbcTemplate.queryForObject(
				userAppcountSql, new Object[] {userName,url},Integer.class);

		
		String roleAppcountSql = SqlNamedQuery.getInstance().getSql(
				"AuthResourseManager.findAuthResourseForUserRole.count");
		roleAppcountSql += condition;
		int	roleAppCount = jdbcTemplate.queryForObject(
				roleAppcountSql, new Object[] {userName,url},Integer.class);
		
		if((userAppCount + roleAppCount)>0)
			return true;
		else
			return false;
	}
}
