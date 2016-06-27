package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

/** 
* @ClassName: BorrowDataJob 
* @author Xiao Xiaowen 
* @date 2016年6月22日 下午3:54:48 
* @Description: TODO
*  
*/
public class BorrowDataJob extends AbstractJob {

	protected FmEditLock editLock=null;
	protected List<Integer> grids = null;
	
	public BorrowDataJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		BorrowDataJobRequest req = (BorrowDataJobRequest)request;
		try{
			//计算借出lendOutDbId
			ManApi man = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionsWithGrid = man.queryRegionWithGrids(grids);
			if(regionsWithGrid==null||regionsWithGrid.size()!=1)throw new JobException("计算借出区域id时发生错误");
			Region r = regionsWithGrid.get(0);
			int lentOutDbId = 0;
			if(editLock!=null){
				if(FmEditLock.DB_TYPE_DAY.equals(editLock.getDbType())){
					lentOutDbId = r.getDailyDbId();
				}else if(FmEditLock.DB_TYPE_MONTH.equals(editLock.getDbType())){
					lentOutDbId = r.getMonthlyDbId();
				}
			}else{
				Region mine = man.queryRegionByDbId(req.getBorrowInDbId());
				if (mine.getDailyDbId() == req.getBorrowInDbId()) {
					lentOutDbId = r.getDailyDbId();
				} else if (mine.getMonthlyDbId() == req.getBorrowInDbId()) {
					lentOutDbId = r.getMonthlyDbId();
				}
			}
			//1. 以delete_copy模式进行数据导出
			req.getSubJobRequest("borrow").setAttrValue("sourceDbId", lentOutDbId);
			JobInfo borrowJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			AbstractJob borrowJob = JobCreateStrategy.createAsSubJob(borrowJobInfo, req.getSubJobRequest("borrow"),
					this);
			borrowJob.run();
			if (borrowJob.getJobInfo().getResponse().getInt("exeStatus") != 3) {
				throw new Exception("创建备份子版本库时job执行失败。");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
		
	}


	@Override
	public void lockResources() throws LockException {
		BorrowDataJobRequest req = (BorrowDataJobRequest) request;
		// 根据批处理的目标库找到对应的大区
		try {
			grids = new ArrayList<Integer>();
			for(String mesh:req.getMeshes()){
				Set<String> gs = CompGridUtil.mesh2Grid(mesh);
				for(String g:gs){
					grids.add(Integer.valueOf(g));
				}
			}
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			editLock = datalock.lockGrid(req.getBorrowInDbId(), FmEditLock.LOCK_OBJ_ALL, grids, FmEditLock.TYPE_BORROW, jobInfo.getId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("加锁发生错误," + e.getMessage(), e);
		}
	}

	@Override
	public void unlockResources() throws LockException {
		if (editLock==null)
			return;
		try {
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalock.unlockGrid(editLock.getLockSeq(), editLock.getDbType());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("解锁时发生错误," + e.getMessage(), e);
		}
	}
	
	public static void main(String[] args){
		List<String> meshes = new ArrayList<String>();
		meshes.add("595670");
		meshes.add("505671");
		Set<Integer> grids = new HashSet<Integer>();
		for(String mesh:meshes){
			Set<String> gs = CompGridUtil.mesh2Grid(mesh);
			for(String g:gs){
				grids.add(Integer.valueOf(g));
			}
		}
		System.out.println(StringUtils.join(grids,","));
		MultiValueMap map = new MultiValueMap();
	}
}
