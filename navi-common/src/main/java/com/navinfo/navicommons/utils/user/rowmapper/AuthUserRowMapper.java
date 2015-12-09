package com.navinfo.navicommons.utils.user.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.navinfo.navicommons.utils.user.model.AuthUser;

/**
 * 包装器
 */
public class AuthUserRowMapper implements RowMapper<AuthUser> {

	private static AuthUserRowMapper instance = new AuthUserRowMapper();

	public static AuthUserRowMapper getInsatance() {
		return instance;
	}

	public AuthUser mapRow(ResultSet rs, int index) throws SQLException {
		AuthUser obj = new AuthUser();

		obj.setUserId(rs.getInt("user_id"));

		obj.setUserName(rs.getString("user_name"));

		obj.setPassword(rs.getString("password"));

		obj.setDecs(rs.getString("decs"));

		obj.setDepartment(rs.getString("department"));

		obj.setTelephone(rs.getString("telephone"));

		obj.setEMail(rs.getString("e_mail"));

		obj.setUserNameCn(rs.getString("user_name_cn"));

		obj.setPlainpassword(rs.getString("plainpassword"));

		return obj;
	}

}
