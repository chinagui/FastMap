package ${package}.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.ServiceException;
import ${package}.model.${modelClassName};
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;
import com.navinfo.dataservice.commons.util.StringUtils;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ${modelClassName}Service 
* @author code generator
* @date ${date} 
* @Description: TODO
*/
@Service
public class ${modelClassName}Service {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(json, ${modelClassName}.class);	
			
			String createSql = "insert into ${tableName} (<#list columns as column><#if column?is_first>${column.name}<#else>, ${column.name}</#if></#list>) values(<#list columns as column><#if column?is_first>?<#else>,?</#if></#list>)";			
			run.update(conn, 
					   createSql, 
					   <#list columns as column><#if column?is_first>bean.get${column.camelName?cap_first}() <#else>, bean.get${column.camelName?cap_first}()</#if></#list>
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(obj, ${modelClassName}.class);	
			
			String updateSql = "update ${tableName} set <#list columns as column><#if column?is_first>${column.name}=?<#else>, ${column.name}=?</#if></#list> where 1=1 <#list columns as column><#if column?is_first>${column.name}=?<#else> and ${column.name}=?</#if></#list>";
			List<Object> values=new ArrayList();
			<#list columns as column>
			if (bean!=null&&bean.get${column.camelName?cap_first}()!=null && StringUtils.isNotEmpty(bean.get${column.camelName?cap_first}().toString())){
				updateSql+=" and ${column.name}=? ";
				values.add(bean.get${column.camelName?cap_first}());
			};
			</#list>				
			run.update(conn, 
					   updateSql, 
					   <#list columns as column><#if column?is_first>bean.get${column.camelName?cap_first}() <#else>,bean.get${column.camelName?cap_first}()</#if></#list>,
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void delete(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(obj, ${modelClassName}.class);	
			
			String deleteSql = "delete from  ${tableName} where 1=1 ";
			List<Object> values=new ArrayList();
			<#list columns as column>
			if (bean!=null&&bean.get${column.camelName?cap_first}()!=null && StringUtils.isNotEmpty(bean.get${column.camelName?cap_first}().toString())){
				deleteSql+=" and ${column.name}=? ";
				values.add(bean.get${column.camelName?cap_first}());
			};
			</#list>		
			if (values.size()==0){
	    		run.update(conn, deleteSql);
	    	}else{
	    		run.update(conn, deleteSql,values.toArray());
	    	}
	    	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public Page list(JSONObject json ,final int currentPageNum)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(obj, ${modelClassName}.class);
			
			String selectSql = "select * from ${tableName} where 1=1 ";
			List<Object> values=new ArrayList();
			<#list columns as column>
			if (bean!=null&&bean.get${column.camelName?cap_first}()!=null && StringUtils.isNotEmpty(bean.get${column.camelName?cap_first}().toString())){
				selectSql+=" and ${column.name}=? ";
				values.add(bean.get${column.camelName?cap_first}());
			};
			</#list>
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>(){
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
		            Page page = new Page(currentPageNum);
					while(rs.next()){
						HashMap map = new HashMap();
						page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
						<#list columns as column>
						map.put("${column.camelName}", rs.get${column.sqlType?cap_first}("${column.name}"));
						</#list>
						list.add(map);
					}
					page.setResult(list);
					return page;
				}
	    		
	    	}	;
			if (values.size()==0){
	    		return run.query(currentPageNum, 20, conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(currentPageNum, 20, conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public List<HashMap> list(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
					
			JSONObject obj = JSONObject.fromObject(json);	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(obj, ${modelClassName}.class);	
			String selectSql = "select * from ${tableName} where 1=1 ";
			List<Object> values=new ArrayList();
			<#list columns as column>
			if (bean!=null&&bean.get${column.camelName?cap_first}()!=null && StringUtils.isNotEmpty(bean.get${column.camelName?cap_first}().toString())){
				selectSql+=" and ${column.name}=? ";
				values.add(bean.get${column.camelName?cap_first}());
			};
			</#list>
			ResultSetHandler<List<HashMap>> rsHandler = new ResultSetHandler<List<HashMap>>(){
				public List<HashMap> handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					while(rs.next()){
						HashMap<String,?> map = new HashMap<String,?>();
						<#list columns as column>
						map.put("${column.camelName}", rs.get${column.sqlType?cap_first}("${column.name}"));
						</#list>
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (values.size()==0){
	    		return run.query(conn, selectSql, rsHandler
						);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray()
					);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public HashMap query(JSONObject json)throws ServiceException{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			${modelClassName}  bean = (${modelClassName})JSONObject.toBean(obj, ${modelClassName}.class);	
			
			String selectSql = "select * from ${tableName} where <#list columns as column><#if column?is_first>${column.name}=?<#else> and ${column.name}=?</#if></#list>";
			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>(){
				public HashMap handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						HashMap map = new HashMap();
						<#list columns as column>
						map.put("${column.camelName}", rs.get${column.sqlType?cap_first}("${column.name}"));
						</#list>
						return map;
					}
					return null;
				}
	    		
	    	}		;				
			return run.query(conn, 
					   selectSql,
					   rsHandler, 
					   <#list modelAttrs as modelAttr>bean.get${modelAttr}()<#sep>, </#list>);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
