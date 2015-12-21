package com.navinfo.dataservice.versionman.lock;

import java.sql.Clob;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.versionman.exception.LockException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: MeshLockManager 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:32:03 
 * @Description: 
 * 检查、批处理、借入和归还操作之前，申请锁；操作完成后解锁
 * 项目号取值要小于Integer.MAX_VALUE:2147483647
 */
public class MeshLockManager {
	protected Logger log = Logger.getLogger(this.getClass());
	private DataSource manDataSource;
	
	public MeshLockManager(DataSource manDataSource){
		this.manDataSource=manDataSource;
	}

	/**
	 * 返回被锁定的图幅Set，如果null则没有锁
	 * @param prjId
	 * @param meshes
	 * @return
	 * @throws LockException
	 */
    public Set<String> query(int prjId,Set<String> meshes)throws LockException{
    	
    	return null;
    }

    /**
     * 所有图幅均符合锁定条件，则申请锁成功，否则抛出异常
     * @param prjId
     * @param meshes
     * @param lockType
     * @return 本次申请锁的批次号
     * @throws LockException
     */
    public int lock(int prjId,Set<Integer> meshes,int lockType)throws LockException{
    	if(meshes==null){
    		throw new LockException("申请锁失败：传入图幅为空，请检查。");
    	}
    	int size = meshes.size();
    	if(size==0){
    		throw new LockException("申请锁失败：传入图幅为空，请检查。");
    	}
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = manDataSource.getConnection();
			int lockSeq = run.queryForInt(conn, "SELECT MESHES_LOCK_SEQ.NEXTVAL FROM DUAL");
			//当size超过1000时，才转clob，提高效率
			String meshInClause = null;
			Clob clobMeshes=null;
			if(size>1000){
				clobMeshes=conn.createClob();
				clobMeshes.setString(1, StringUtils.collection2String(meshes, ","));
				meshInClause = " MESH_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				meshInClause = " MESH_ID IN ("+StringUtils.collection2String(meshes, ",")+")";
			}
			StringBuffer sqlBuf = new StringBuffer();
			int updateCount=0;
			//申请锁时，借出例外
			if(lockType==FmMesh4Lock.TYPE_BORROW){
				sqlBuf.append("UPDATE FM_MESH SET HANDLE_PROJECT_ID=?,LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,UPDATE_TIME=SYSDATE WHERE");
				sqlBuf.append(meshInClause);
				sqlBuf.append(" AND HANDLE_PROJECT_ID <> ? AND LOCK_STATUS=0");
				if(size>1000){
					updateCount = run.update(conn, sqlBuf.toString(),prjId,lockType,lockSeq,clobMeshes,prjId);
				}else{
					updateCount = run.update(conn, sqlBuf.toString(),prjId,lockType,lockSeq,prjId);
				}
			}else{
				sqlBuf.append("UPDATE FM_MESH SET LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,UPDATE_TIME=SYSDATE WHERE");
				sqlBuf.append(meshInClause);
				sqlBuf.append(" AND HANDLE_PROJECT_ID = ? AND LOCK_STATUS=0");
				if(size>1000){
					updateCount = run.update(conn, sqlBuf.toString(),lockType,lockSeq,clobMeshes,prjId);
				}else{
					updateCount = run.update(conn, sqlBuf.toString(),lockType,lockSeq,prjId);
				}
			}
			if(updateCount!=size){
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
     * 当所有图幅符合解锁条件，则解锁成功，否则抛出异常
     * @param prjId
     * @param lockSeq
     * @return 解锁的图幅个数
     * @throws LockException
     */
	public int unlock(int prjId,int lockSeq,int lockType)throws LockException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = manDataSource.getConnection();
			//解锁时，归还例外
			String sql = null;
			if(lockType==FmMesh4Lock.TYPE_GIVE_BACK){
				sql = "UPDATE FM_MESH SET HANDLE_PROJECT_ID=PROJECT_ID,LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,UPDATE_TIME=SYSDATE" +
						" WHERE HANDLE_PROJECT_ID=? AND LOCK_TYPE=? AND LOCK_STATUS=1 AND LOCK_SEQ=?";
			}else{
				sql = "UPDATE FM_MESH SET LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,UPDATE_TIME=SYSDATE" +
						" WHERE HANDLE_PROJECT_ID=? AND LOCK_TYPE=? AND LOCK_STATUS=1 AND LOCK_SEQ=?";
			}
			int updateCount = run.update(conn, sql,prjId,lockType,lockSeq);
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
	 * @param prjId
	 * @param meshes
	 * @param lockType
	 * @throws LockException
	 */
	public void unlock(int prjId,Set<Integer> meshes,int lockType)throws LockException{
    	if(meshes==null){
    		throw new LockException("释放锁失败：传入图幅为空，请检查。");
    	}
    	int size = meshes.size();
    	if(size==0){
    		throw new LockException("释放锁失败：传入图幅为空，请检查。");
    	}
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = manDataSource.getConnection();
			//当size超过1000时，才转clob，提高效率
			String meshInClause = null;
			Clob clobMeshes=null;
			if(size>1000){
				clobMeshes=conn.createClob();
				clobMeshes.setString(1, StringUtils.collection2String(meshes, ","));
				meshInClause = " MESH_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				meshInClause = " MESH_ID IN ("+StringUtils.collection2String(meshes, ",")+")";
			}
			//解锁时，归还例外
			StringBuffer sqlBuf = new StringBuffer();
			int updateCount=0;
			if(lockType==FmMesh4Lock.TYPE_GIVE_BACK){
				sqlBuf.append("UPDATE FM_MESH SET HANDLE_PROJECT_ID=PROJECT_ID,LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,UPDATE_TIME=SYSDATE WHERE");
			}else{
				sqlBuf.append("UPDATE FM_MESH SET LOCK_STATUS=0,LOCK_TYPE=NULL,LOCK_SEQ=NULL,UPDATE_TIME=SYSDATE WHERE");
			}
			sqlBuf.append(meshInClause);
			sqlBuf.append(" AND LOCK_STATUS=1 AND LOCK_TYPE=? AND HANDLE_PROJECT_ID=?");
			
			if(size>1000){
				updateCount = run.update(conn, sqlBuf.toString(),clobMeshes,lockType,prjId);
			}else{
				updateCount = run.update(conn, sqlBuf.toString(),lockType,prjId);
			}
			
			if(updateCount!=size){
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
	
	@Deprecated
	/**
	 * 全部图幅都能够被借出才成功，否则抛异常
	 * @param prjId
	 * @param meshes:key-借出的项目号，value为从该项目借入的图幅set
	 * @return 本次借出锁申请的批次号
	 * @throws LockException
	 */
	public int borrow(int prjId,Map<Integer,Set<String>> meshes)throws LockException{
		if(meshes==null||meshes.size()==0){
			throw new LockException("借图幅失败：传入图幅参数为空，请检查。");
		}
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = manDataSource.getConnection();
			int lockSeq = run.queryForInt(conn, "SELECT MESHES_LOCK_SEQ.NEXTVAL FROM DUAL");
			String sql = "UPDATE FM_MESH SET HANDLE_PROJECT_ID=?,LOCK_STATUS=1,LOCK_TYPE=?,LOCK_SEQ=?,UPDATE_TIME=SYSDATE" +
					" WHERE MESH_ID IN (select to_number(column_value) from table(clob_to_table(?))) AND LOCK_STATUS=0 AND HANDLE_PROJECT_ID = ?";
			for(Integer rentPrjId : meshes.keySet()){
				Set<String> rentMeshes = meshes.get(rentPrjId);
				if(rentMeshes==null){
					throw new LockException("借图幅失败：项目"+rentPrjId+"传入图幅参数为空，请检查。");
				}
				int rentSize = rentMeshes.size();

				int updateCount = run.update(conn, sql,prjId,FmMesh4Lock.TYPE_BORROW,lockSeq,StringUtils.collection2String(rentMeshes, "','"),rentPrjId);
				if(updateCount!=rentSize){
					throw new LockException("借图幅失败：项目"+rentPrjId+"能够借出的图幅数和传入图幅数不相等。");
				}
			}
			return lockSeq;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			if(e instanceof LockException){
				throw (LockException)e;
			}else{
				throw new LockException("借图幅发生SQL错误："+e.getMessage(),e);
			}
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	@Deprecated
	public int giveBack(String prjId,Map<Integer,Set<String>> meshes)throws LockException{
		if(meshes==null||meshes.size()==0){
			throw new LockException("归还图幅失败：传入图幅参数为空，请检查。");
		}
		//...
		
		return 0;
	}
	
	@Deprecated
	public void unlockBorrowOrGiveback(String prjId,Map<String,Set<String>> meshes)throws LockException{
		
	}

	public static void main(String[] args){
		try{
//		long t1 = System.currentTimeMillis();
//		System.out.println(UUID.randomUUID().toString());
//		System.out.println(System.currentTimeMillis()-t1);
//		System.out.println(Integer.MAX_VALUE);
//		System.out.println(Long.MAX_VALUE);
			MeshLockManager man = new MeshLockManager(MultiDataSourceFactory.getInstance().getManDataSource());
			Set<Integer> meshes = new HashSet<Integer>();
			for(int i=0;i<1001;i++){
				meshes.add(100000+i);
			}
//			meshes.add(595672);
//			meshes.add(595671);
			man.lock(1, meshes, FmMesh4Lock.TYPE_GIVE_BACK);
//			System.out.println(result);
			System.out.println("over.");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
