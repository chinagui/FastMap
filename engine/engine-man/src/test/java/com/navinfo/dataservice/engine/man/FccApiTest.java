package com.navinfo.dataservice.engine.man;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.mqmsg.InfoChangeMsgHandler;
import com.navinfo.dataservice.engine.man.mqmsg.SendEmailMsgHandler;

public class FccApiTest extends InitApplication {
	

	@Override
	@Before
	public void init() {
		// TODO Auto-generated method stub
		initContext();
	}
	
	
	
	@Test
	public void fccApiTest() throws Exception{
		
		String parameter="{\"gdbid\":12,\"au_db_ip\":\"192.168.3.227\",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":\"orcl\",\"au_db_port\":1521,\"types\":\"\",\"phaseId\":106,\"collectTaskIds\":\"\",\"task_type\":1,\"taskInfo\":{\"manager_id\":2208,\"imp_task_name\":\"鍖椾含甯傚寳浜競鍩庡尯甯歌惀2_20170526\",\"province\":\"北京市\",\"city\":\"北京市\",\"district\":\"北京市北京市城区常营2\",\"job_nature\":\"更新\",\"job_type\":\"中线一体化作业\"}}";
		
		//parameter="{\"gdbid\":12,\"au_db_ip\":\"192.168.3.227 \",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":\"orcl\",\"au_db_port\":1521,\"types\":\"\",\"phaseId\":0,\"collectTaskIds\":[],\"task_type\":1,\"taskInfo\":{\"manager_id\":888,\"imp_task_name\":\"北京市北京市城区香山_20170526\",\"province\":\"北京市\",\"city\":\"北京市\",\"district\":\"北京市北京市城区香山\",\"job_nature\":\"更新\",\"job_type\":\"中线一体化作业\"}}";
		//中线任务号 155
		parameter="{\"gdbid\":12,\"au_db_ip\":\"192.168.3.227 \",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":\"orcl\",\"au_db_port\":1521,\"types\":\"\",\"phaseId\":0,\"collectTaskIds\":[],\"task_type\":1,\"taskInfo\":{\"manager_id\":155,\"imp_task_name\":\"北京市北京市城区香山_20170526\",\"province\":\"北京市\",\"city\":\"北京市\",\"district\":\"北京市北京市城区香山\",\"job_nature\":\"更新\",\"job_type\":\"中线一体化作业\"}}";
		
		//中线子任务号 198
		parameter="{\"gdbid\":12,\"au_db_ip\":\"192.168.3.227 \",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":\"orcl\",\"au_db_port\":1521,\"types\":\"\",\"phaseId\":0,\"collectTaskIds\":[],\"task_type\":2,\"taskInfo\":{\"manager_id\":198,\"imp_task_name\":\"北京市北京市城区香山_20170526\",\"province\":\"北京市\",\"city\":\"北京市\",\"district\":\"北京市北京市城区香山\",\"job_nature\":\"更新\",\"job_type\":\"中线一体化作业\"}}";
		
		JSONObject  json=JSONObject.fromObject(parameter);
		
		FccApi fccApi = (FccApi) ApplicationContextUtil.getBean("fccApi");
		try{
			fccApi.tips2Aumark(json);
		}catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
