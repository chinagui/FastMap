package com.navinfo.dataservice.engine.fcc.service;


import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
/*import com.navinfo.nirobot.business.Tips2AuMarkMRTest;*/

@Service("fccApi")
public class FccApiImpl implements FccApi{

	@Override
	public JSONArray searchDataBySpatial(String wkt, int type, JSONArray stages) throws Exception {
		try {
			TipsSelector selector = new TipsSelector();
			JSONArray array = selector.searchDataBySpatial(wkt,type,stages);
			return array;
		} catch (Exception e) {
			throw e;
		}
		
	}

	@Override
	public JSONObject getSubTaskStats(JSONArray grids) throws Exception {
		JSONObject result=new JSONObject();
		
		if (grids==null||grids.isEmpty()) {
			
            throw new IllegalArgumentException("参数错误:grids不能为空。");
        }

		TipsSelector selector = new TipsSelector();
		
		//统计日编总量 stage=1
		int total=selector.getTipsCountByStage(grids, 1);
		
		//统计日编已完成量stage=2 and t_dStatus=1
		int finished=selector.getTipsCountByStageAndTdStatus(grids,2,1);
		
		result.put("total", total);
		
		result.put("finished", finished);
		
		return result;
	}
	
	@Override
	public JSONObject getSubTaskStatsByWkt(String wkt) throws Exception {
		JSONObject result=new JSONObject();
		
		if (wkt==null||wkt.isEmpty()) {
			
            throw new IllegalArgumentException("参数错误:wkt不能为空。");
        }

		TipsSelector selector = new TipsSelector();
		
		//统计日编总量 stage=1
		int total=selector.getTipsCountByStageAndWkt(wkt, 1);
		
		//统计日编已完成量stage=2 and t_dStatus=1
		int finished=selector.getTipsCountByStageAndTdStatusAndWkt(wkt,2,1);
		
		result.put("total", total);
		
		result.put("finished", finished);
		
		return result;
	}
	
	@Override
    public void tips2Aumark(JSONObject parameter) throws Exception {
    	
    }

   /* @Override
    public void tips2Aumark(JSONObject parameter) throws Exception {

        if (parameter==null||parameter.isEmpty()) {

            throw new IllegalArgumentException("参数错误:数据parameter不能为空。");
        }

        new tips2Aumark(parameter).run();

    }

    class tips2Aumark implements Runnable{
        JSONObject parameter = null;
        tips2Aumark(JSONObject parameter){
            this.parameter=parameter;
        }
        @Override
        public void run() {

                //外业库信息
                String auip = parameter.getString("au_db_ip");

                if (auip==null||auip.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:au_db_ip不能为空");
                }

                String auuser = parameter.getString("au_db_username");

                if (auuser==null||auuser.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:au_db_username不能为空");
                }

                String aupw = parameter.getString("au_db_password");

                if (aupw==null||aupw.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:au_db_password不能为空");
                }

                String ausid = parameter.getString("au_db_sid");

                if (ausid==null||ausid.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:au_db_sid不能为空");
                }

                String auport = parameter.getString("au_db_port");

                if (auport==null||auport.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:au_db_port不能为空");
                }

                //gdb参考库
                String gdbId = parameter.getString("gdbid");

                if (gdbId==null||gdbId.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:参考库gdbId不能为空");
                }

                //grid，types
                String grids = parameter.getString("grids");

                if (grids==null||grids.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:grids不能为空");
                }

                String types = parameter.getString("types");

                JSONObject taskInfo = parameter.getJSONObject("taskid");

                if (taskInfo==null||taskInfo.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:任务信息参数不能为空");
                }else{
                    String managerId = taskInfo.getString("manager_id");
                    if (managerId==null||managerId.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:中线采集任务ID不能为空");
                    }
                    String taskName = taskInfo.getString("imp_task_name");
                    if (taskName==null||taskName.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:中线采集任务名称不能为空");
                    }
                    String province = taskInfo.getString("province");
                    if (province==null||province.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:省份不能为空");
                    }
                    String city = taskInfo.getString("city");
                    if (city==null||city.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:市不能为空");
                    }
                    String district = taskInfo.getString("district");
                    if (district==null||district.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:区不能为空");
                    }
                    String jobNature= taskInfo.getString("job_nature");
                    if (jobNature==null||jobNature.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:作业性质不能为空");
                    }
                    String jobType = taskInfo.getString("job_type");
                    if (jobType==null||jobType.isEmpty()) {
                        throw new IllegalArgumentException("参数错误:作业类型不能为空");
                    }
                }

                //phaseId
                int phaseId = parameter.getInt("phaseId");

            try{
               // Tips2AuMarkMRTest tip2mark = new Tips2AuMarkMRTest();
                
                //tip2mark.tips2Aumark(auip,ausid,auport,auuser,aupw,gdbId,grids,types,taskInfo);

                Tips2AuMarkMRTest.tips2Aumark(auip,ausid,auport,auuser,aupw,gdbId,grids,types,taskInfo);
                
                ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
                apiService.taskUpdateCmsProgress(phaseId,2);

            }catch(Exception e){
                ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
                try {
                    apiService.taskUpdateCmsProgress(phaseId,3);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }*/

}
