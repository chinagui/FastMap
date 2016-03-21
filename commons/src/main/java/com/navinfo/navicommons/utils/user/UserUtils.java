package com.navinfo.navicommons.utils.user;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.navinfo.navicommons.database.DBConnectionFactory;
import com.navinfo.navicommons.database.DataSourceType;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.utils.user.model.AuthUser;
import com.navinfo.navicommons.utils.user.rowmapper.AuthUserRowMapper;


public class UserUtils {
	
	private static final transient Logger logger = Logger.getLogger(UserUtils.class);
//	protected static MavenConfigMap config = new MavenConfigMap(SystemConfig.getSystemConfig());
	/**查询用户
	 * @param userName
	 * @return
	 * @throws ServiceException
	 */
	public static  AuthUser getUser(String userName) {
		AuthUser user=null;
		JdbcTemplate jdbcTemplate=null;
		try {
			String sql="select * from auth_user t where t.user_name=?";
//			DataSource vmDataSource = DBConnectionFactory.getInstance().setupDataSource(DataSourceType.VM,config);
//			VersionManager vmVersion=new VersionManager(vmDataSource);
//			Version dmsVersion= vmVersion.ge();
			
			DataSource dataSourceDMS = DBConnectionFactory.getInstance().getDataSource(DataSourceType.DMS);
			jdbcTemplate=new JdbcTemplate(dataSourceDMS);
			List<AuthUser> list=jdbcTemplate.query(sql, new Object[]{userName}, new int[]{Types.VARCHAR},new AuthUserRowMapper());
			if(list.size()==1){
				user=list.get(0);
			}
		}catch(Exception e){
			logger.error("查询用户出错", e);
		}
		return user;
	}
}
