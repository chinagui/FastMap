package com.navinfo.dataservice.engine.sys.msg.websocket;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class ManStaticWebSocketHandler extends TextWebSocketHandler {
	protected static Logger log;
	private static final ArrayList<WebSocketSession> users;
	static {
		users = new ArrayList<WebSocketSession>();
		log = LoggerRepos.getLogger(ManStaticWebSocketHandler.class);
	}
	private volatile static ManStaticWebSocketHandler instance;
	public static ManStaticWebSocketHandler getInstance(){
		if(instance==null){
			synchronized(ManStaticWebSocketHandler.class){
				if(instance==null){
					instance=new ManStaticWebSocketHandler();
				}
			}
		}
		return instance;
	}
	private ManStaticWebSocketHandler(){}

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
	 * 给所有在线用户发送消息
	 *
	 * @param message
	 */
	public void sendMessageToUsers(TextMessage message) {
		for (WebSocketSession user : users) {
			try {
				if (user.isOpen()) {
					user.sendMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error("发送失败,原因:"+e.getMessage(), e);
			}
		}
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
