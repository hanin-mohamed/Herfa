package com.ProjectGraduation.aucation.config;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JWTService jwtService;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getHeader("Authorization");

            if (token == null) {
                token = servletRequest.getServletRequest().getParameter("token");
            }

            System.out.println("💡 WebSocket TOKEN RECEIVED: " + token);

            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.replace("Bearer ", "");
                }

                try {
                    String username = jwtService.getUsername(token);
                    User user = userService.getUserByUsername(username);

                    attributes.put("username", username);
                    attributes.put("user", user);

                    System.out.println("✅ WebSocket Authenticated as: " + username);
                    return true;
                } catch (Exception e) {
                    System.out.println("❌ WebSocket token invalid: " + e.getMessage());
                }
            }
        }

        System.out.println("❌ WebSocket connection rejected (missing or invalid token)");
        response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
