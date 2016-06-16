package com.navinfo.dataservice.engine.edit.datalock;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.datalock.MeshLockManager.FmMeshResultSetHandler4QueryLock;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: GridLockManager 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:32:03 
 * @Description: 
 * 检查、批处理、借入和归还操作之前，申请锁；操作完成后解锁
 * 项目号取值要小于Integer.MAX_VALUE:2147483647
 */
public class GridLockManager{
	protected Logger log = Logger.getLogger(this.getClass());
	
	private volatile static GridLockManager instance;
	
	public static GridLockManager getInstance(){
		if(instance==null){
			synchronized(GridLockManager.class){
				if(instance==null){
					instance = new GridLockManager();
				}
			}
		}
		return instance;
	}
	private GridLockManager(){}
	
	/**
	 * 返回状态为锁定或者已借出的图幅Set，否则返回null
	 * @param regionId
	 * @param grids
	 * @return
	 * @throws LockException
	 */
    public Set<Integer> query(int regionId,Set<Integer> grids,int lockObject,String dbType)throws LockException{
    	if(grids==null){
    		throw new LockException("查询锁失败：传入图幅为空，请检查。");
    	}
    	int size = grids.size();
    	if(size==0){
    		throw new LockException("查询锁失败：传入图幅为空，请检查。");
    	}
    	Connection conn = null;
    	try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//当size超过1000时，才转clob，提高效率
			String gridInClause = null;
			Clob clobGrids=null;
			if(size>1000){
				clobGrids=conn.createClob();
				clobGrids.setString(1, StringUtils.join(grids, ","));
				gridInClause = " GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				gridInClause = " GRID_ID IN ("+StringUtils.join(grids, ",")+")";
			}
			StringBuffer sqlBuf = new StringBuffer();
			sqlBuf.append("SELECT "+getGridLockDbName(dbType)+" FROM GRID_LOCK WHERE");
			sqlBuf.append(gridInClause);
			sqlBuf.append(" AND (HANDLE_REGION_ID <> ? OR LOCK_STATUS=1)");
			sqlBuf.append(getLockObjectClause(lockObject));
			Set<Integer> result = null;
			if(size>1000){
				result = run.query(conn, sqlBuf.toString(), new GridLockResultSetHandler4QueryLock(),clobGrids, regionId);
			}else{
				result = run.query(conn, sqlBuf.toString(), new GridLockResultSetHandler4QueryLock(), regionId);
			}
			return result;
    	}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			if(e instanceof LockException){
				throw (LockException)e;
			}else{
				throw new LockException("申请图幅锁发生SQL错误，"+e.getMessage(),e);
			}
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
    }

    /**
     * 所有grid均符合锁定条件，则申请锁成功，否则抛出异常
     * @param regionId
     * @param grids
     * @param lockType 
     * @return 本次申请锁的批次号
     * @throws LockException
     */
    public int lock(int regionId, int lockObject, Collection<Integer> grids,int lockType,int jobId,String dbType)throws LockException{
    	if(grids==null){
    		throw new LockException("申请锁失败：传入grids为空，请检查。");
    	}
    	int size = grids.size();
    	if(size==0){
    		throw new LockException("申请锁失败：传入grids为空，请检查。");
    	}
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			int lockSeq = run.queryForInt(conn, "SELECT GRID_LOCK_SEQ.NEXTVAL FROM DUAL");
			//当size超过1000时，才转clob，提高效率
			String gridInClause = null;
			Clob clobGrids=null;
			if(size>1000){
				clobGrids=conn.createClob();
				clobGrids.setString(1, StringUtils.join(grids, ","));
				gridInClause = " GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				gridInClause = " GRID_ID IN ("+StringUtils.join(grids, ",")+")";
			}
			int gridModSize=getGridModSize(size,lockObject);//由于每个grid存在lockobject为poi和road两条数据，所以对于
			String lockObjClause = getLockObjectClause(lockObject);
			StringBuffer sqlBuf = new StringBuffer();
			int updateCount=0;
			//申请锁时，借出例外
			if(lockType==FmEditLock.TYPE_BORROW){
				sqlBuf = new StringBuffer();
				
				sqlBuf.append("UPDATE "+getGridLockDbName(dbType)+" SET HANDLE_REGION_ID=?,LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,LOCK_TIME=SYSDATE WHERE");
				sqlBuf.append(gridInClause);
				sqlBuf.append(" AND HANDLE_REGION_ID <> ? AND LOCK_STATUS=0");
				sqlBuf.append(lockObjClause);
				log.debug(sqlBuf);
				if(size>1000){
					updateCount=run.update(conn, sqlBuf.toString(), regionId,lockType,lockSeq,clobGrids,regionId);
				}else{
					updateCount=run.update(conn, sqlBuf.toString(), regionId,lockType,lockSeq,regionId);
				}
				
			}else if(lockType==FmEditLock.TYPE_GIVE_BACK){
				sqlBuf=new StringBuffer();
				sqlBuf.append("UPDATE "+getGridLockDbName(dbType)+" SET LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,LOCK_TIME=SYSDATE WHERE");
				sqlBuf.append(gridInClause);
				sqlBuf.append(" AND REGION_ID <> ? AND HANDLE_REGION_ID = ? AND LOCK_STATUS=0");
				sqlBuf.append(lockObjClause);
				log.debug(sqlBuf);
				if(size>1000){
					updateCount=run.update(conn, sqlBuf.toString(),lockType,lockSeq,clobGrids,regionId,regionId);
				}else{
					updateCount=run.update(conn, sqlBuf.toString(),lockType,lockSeq,regionId,regionId);
				}
			}else{
				sqlBuf=new StringBuffer();
				sqlBuf.append("UPDATE "+getGridLockDbName(dbType)+" SET LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,LOCK_TIME=SYSDATE WHERE");
				sqlBuf.append(gridInClause);
				sqlBuf.append(" AND HANDLE_REGION_ID = ? AND LOCK_STATUS=0");
				sqlBuf.append(lockObjClause);
				log.debug(sqlBuf);
				if(size>1000){
					updateCount=run.update(conn, sqlBuf.toString(),lockType,lockSeq,clobGrids,regionId);
				}else{
					updateCount=run.update(conn, sqlBuf.toString(),lockType,lockSeq,regionId);
				}
			}
			if(updateCount!=gridModSize){
				throw new LockException("锁定图幅失败，能够被锁定的图幅数和传入图幅数不相等。");
			}
			return lockSeq;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			if(e instanceof LockException){
				throw (LockException)e;
			}else{
				throw new LockException("申请图幅锁发生SQL错误，"+e.getMessage(),e);
			}
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
    }
	
    /**
     * 当所有grid符合解锁条件，则解锁成功，否则抛出异常
     * @param regionId
     * @param lockSeq
     * @return 解锁的图幅个数
     * @throws LockException
     */
	public int unlock(int lockSeq,String dbType)throws LockException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//解锁时，归还例外
			String sql = "UPDATE "+getGridLockDbName(dbType)+" "
					+ " SET HANDLE_REGION_ID = (case  when LOCK_TYPE=5 then REGION_ID else handle_region_id end),"
					+ " LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,LOCK_TIME=SYSDATE" 
					+ " WHERE LOCK_SEQ=?";
			this.log.debug(sql);
			int updateCount = run.update(conn,sql ,lockSeq);
			return updateCount;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new LockException("释放图幅锁发生SQL错误，"+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 解锁成功无异常，解锁失败抛异常。
	 * @param regionId
	 * @param grids
	 * @param lockType
	 * @throws LockException
	 */
	public void unlock(int regionId, Set<Integer> grids,int lockType,int lockObject,String dbType)throws LockException{
    	if(grids==null){
    		throw new LockException("释放锁失败：传入图幅为空，请检查。");
    	}
    	int size = grids.size();
    	if(size==0){
    		throw new LockException("释放锁失败：传入图幅为空，请检查。");
    	}
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//当size超过1000时，才转clob，提高效率
			String gridInClause = null;
			Clob clobGrids=null;
			if(size>1000){
				clobGrids=conn.createClob();
				clobGrids.setString(1, StringUtils.join(grids, ","));
				gridInClause = " GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				gridInClause = " GRID_ID IN ("+StringUtils.join(grids, ",")+")";
			}
			//解锁时，归还例外
			StringBuffer sqlBuf = new StringBuffer();
			int updateCount=0;
			int gridModSize=getGridModSize(size,lockObject);//由于每个grid存在lockobject为poi和road两条数据，所以对于
			String lockObjClause = getLockObjectClause(lockObject);
			if(lockType==FmEditLock.TYPE_GIVE_BACK){
				sqlBuf.append("UPDATE "+getGridLockDbName(dbType)+" SET HANDLE_REGION_ID=REGION_ID,LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,LOCK_TIME=SYSDATE WHERE");
			}else{
				sqlBuf.append("UPDATE "+getGridLockDbName(dbType)+" SET LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,LOCK_TIME=SYSDATE WHERE");
			}
			sqlBuf.append(gridInClause);
			sqlBuf.append(" AND LOCK_STATUS=1 AND LOCK_TYPE=? AND HANDLE_REGION_ID=?");
			sqlBuf.append(lockObjClause);
			
			if(size>1000){
				updateCount = run.update(conn, sqlBuf.toString(),clobGrids,lockType,regionId);
			}else{
				updateCount = run.update(conn, sqlBuf.toString(),lockType,regionId);
			}
			
			
			if(updateCount!=gridModSize){
				throw new LockException("释放图幅锁失败，符合释放条件的图幅数和传入图幅数不相等。");
			}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			if(e instanceof LockException){
				throw (LockException)e;
			}else{
				throw new LockException("释放图幅锁锁发生SQL错误，"+e.getMessage(),e);
			}
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	

	public static void main(String[] args){
		try{
//		long t1 = System.currentTimeMillis();
//		System.out.println(UUID.randomUUID().toString());
//		System.out.println(System.currentTimeMillis()-t1);
//		System.out.println(Integer.MAX_VALUE);
//		System.out.println(Long.MAX_VALUE);
//			for(int i=0;i<1001;i++){
//				meshes.add(100000+i);
//			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	private class GridLockResultSetHandler4QueryLock implements ResultSetHandler<Set<Integer>>{
		@Override
		public Set<Integer> handle(ResultSet rs) throws SQLException {
			Set<Integer> meshSet = null;
			if(rs.next()){
				meshSet = new HashSet<Integer>();
			}else{
				return null;
			}
			do{
				meshSet.add(rs.getInt(1));
			} while(rs.next());
			return meshSet;
		}
		
	}
	private String getLockObjectClause(int lockObject) {
		String lockObjClause="";
		if (FmEditLock.LOCK_OBJ_POI==lockObject){
			lockObjClause=" AND LOCK_OBJECT="+FmEditLock.LOCK_OBJ_POI;
		}else if(FmEditLock.LOCK_OBJ_ROAD==lockObject){
			lockObjClause=" AND LOCK_OBJECT="+FmEditLock.LOCK_OBJ_ROAD;
		}
		return lockObjClause;
	}
	private int getGridModSize(int size,int lockObject) {
		if (FmEditLock.LOCK_OBJ_POI==lockObject ||FmEditLock.LOCK_OBJ_ROAD==lockObject){
			return size;
		}
		return size*2;
	}
	private static String getGridLockDbName(final String dbType){
		if (FmEditLock.DB_TYPE_DAY.equals(dbType)) 
			return "GRID_LOCK_DAY";
		return "GRID_LOCK_MONTH";
	}
	
}
