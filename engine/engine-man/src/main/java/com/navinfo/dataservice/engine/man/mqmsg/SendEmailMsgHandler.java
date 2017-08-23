package com.navinfo.dataservice.engine.man.mqmsg;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.man.message.SendEmail;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SendEmailMsgHandler
 * @author Han Shaoming
 * @date 2016年11月3日 下午5:02:34
 * @Description TODO
 */
public class SendEmailMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void handle(String message) {
		// TODO Auto-generated method stub
		try {
			//解析message
			JSONObject jo = JSONObject.fromObject(message);
			String toMail = jo.getString("toMail");
			String mailTitle = jo.getString("mailTitle");
			String mailContent = jo.getString("mailContent");
			//发送邮件
			if(toMail != null){
				log.info("start push mail:"+toMail+","+mailTitle+","+mailContent);
				//SendEmail.sendEmail(toMail, mailTitle, mailContent);
				String SEND_USER=SystemConfigFactory.getSystemConfig().getValue(PropConstant.sendEmail);
				String SEND_PWD=SystemConfigFactory.getSystemConfig().getValue(PropConstant.sendPwd);
				sendMailBySmap(SEND_USER,SEND_PWD,toMail, mailTitle, mailContent);
				log.info("end push mail:"+toMail+","+mailTitle+","+mailContent);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
	}
	
	private void  sendMailBySmap(String mailUser,String mailPwd,String userMail,String title,String content) throws Exception{
		String smapMailUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.smapMailUrl);		
		
		Map<String,String> parMap = new HashMap<String,String>();
		parMap.put("mailUser", mailUser);
		parMap.put("mailPwd", mailPwd);
		parMap.put("mailList", userMail);
		parMap.put("title", title);
		parMap.put("content", content);
		
		log.info(parMap);
		log.info(smapMailUrl);
		String result = ServiceInvokeUtil.invokeByGet(smapMailUrl,parMap,2000);
		log.info("发送邮件，调用smap请求返回值："+result);
	}

}
