package com.navinfo.navicommons.database.sql;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

/** 
 * 暂时不可用
* @ClassName: ListResultSetHandler 
* @author Xiao Xiaowen 
* @date 2017年8月31日 下午7:46:36 
* @Description: TODO
*/
public class ListResultSetHandler<T> implements ResultSetHandler<List<T>>{

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		List<T> list = new ArrayList<T>();
		Class<T>  tClass  =  (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		while(rs.next()){
			T t = rs.getObject(1, tClass);
			list.add(t);
		}
		return list;
	}

}
