package com.navinfo.dataservice.engine.man.websocket;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.navinfo.dataservice.commons.log.LoggerRepos;

public class TaskOther2MediumWebSocketHandler extends TextWebSocketHandler {
	protected static Logger log;
	private static final ArrayList<WebSocketSession> users;
	static {
		users = new ArrayList<WebSocketSession>();
		log = LoggerRepos.getLogger(TaskOther2MediumWebSocketHandler.class);
	}
	private volatile static TaskOther2MediumWebSocketHandler instance;
	public static TaskOther2MediumWebSocketHandler getInstance(){
		if(instance==null){
			synchronized(TaskOther2MediumWebSocketHandler.class){
				if(instance==null){
					instance=new TaskOther2MediumWebSocketHandler();
				}
			}
		}
		return instance;
	}
	private TaskOther2MediumWebSocketHandler(){}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("connect to the websocket success......");
		users.add(session);
		//日志
		log.info("管理消息集合中WebSocketSession的保存个数:"+users.size());
	}

	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		super.handleTextMessage(session, message);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		log.info("websocket connection closed......");
		users.remove(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		log.info("websocket connection closed......");
		users.remove(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 给某个用户发送消息
	 *
	 * @param userId
	 * @param message
	 */
	public void sendMessageToUser(String userId, TextMessage message) {
		for (WebSocketSession user : users){
			if (user.getAttributes().get("userId").equals(userId)) {
				try {
					if (user.isOpen()){
						user.sendMessage(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
					log.error("发送失败,原因:"+e.getMessage(), e);
				}
			}
		}
	}
}
