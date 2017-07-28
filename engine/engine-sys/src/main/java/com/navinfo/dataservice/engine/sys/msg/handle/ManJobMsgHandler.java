package com.navinfo.dataservice.engine.sys.msg.handle;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.engine.sys.msg.websocket.MsgManJobWebSocketHandler;
import com.navinfo.dataservice.engine.sys.msg.websocket.MsgManWebSocketHandler;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;

/**
 * job执行结果消息
 * @ClassName ApplyMsgHandler
 * @author wangshishuai
 * @date 2016年11月14日 下午2:27:33
 */
public class ManJobMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	@Override
	public void handle(String message) {
		try{
		//日志
			log.info("订阅的MANJOB消息数据:"+message);
			//解析message
			JSONObject jo = JSONObject.fromObject(message);
			long auditor = (Integer) jo.get("auditor");
			String jobMessage =  jo.getJSONObject("jobMessage").toString();
			//websocket实时发送消息
			MsgManJobWebSocketHandler.getInstance().sendMessageToUser(Long.toString(auditor), new TextMessage(jobMessage));
		}catch (Exception e) {
			log.error("", e);
		}
	}

}
