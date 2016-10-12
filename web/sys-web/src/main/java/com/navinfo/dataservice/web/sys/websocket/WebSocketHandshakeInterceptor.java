package com.navinfo.dataservice.web.sys.websocket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.navinfo.dataservice.commons.token.AccessToken;




public class WebSocketHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
			HttpSession session = serverRequest.getServletRequest().getSession(false);
			if (session != null) {
				HttpServletRequest httpServletRequest = serverRequest.getServletRequest();
				AccessToken tokenObj = (AccessToken) httpServletRequest.getAttribute("token");
				String userId = Long.toString(tokenObj.getUserId());
				// 使用userId区分WebSocketHandler，以便定向发送消息
				attributes.put("userId", userId);
			}
		}
		return super.beforeHandshake(request, response, wsHandler, attributes);
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		super.afterHandshake(request, response, wsHandler, exception);
	}
}

