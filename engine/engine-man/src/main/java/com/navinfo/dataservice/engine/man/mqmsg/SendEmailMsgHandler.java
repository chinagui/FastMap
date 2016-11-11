package com.navinfo.dataservice.engine.man.mqmsg;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
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
				SendEmail.sendEmail(toMail, mailTitle, mailContent);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
	}

}
