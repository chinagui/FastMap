package com.navinfo.dataservice.engine.sys.msg.handle;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.sys.msg.websocket.MsgManWebSocketHandler;

import net.sf.json.JSONObject;

/**
 * 申请消息
 * @ClassName ApplyMsgHandler
 * @author Han Shaoming
 * @date 2016年11月14日 下午2:27:33
 * @Description TODO
 */
public class ApplyMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void handle(String message) {
		// TODO Auto-generated method stub
		//日志
		log.info("订阅的申请消息数据:"+message);
		//解析message
		JSONObject jo = JSONObject.fromObject(message);
		long auditor = (Integer) jo.get("auditor");
		String applyMessage = (String) jo.get("applyMessage");
		//websocket实时发送消息
		MsgManWebSocketHandler.getInstance().sendMessageToUser(Long.toString(auditor), new TextMessage(applyMessage));
	}

}
