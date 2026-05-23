package trainfocus.backend.auth.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.common.ui.ErrorResponse;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    public static final String FIREBASE_USER_ATTRIBUTE = "firebaseUserInfo";
    public static final String LOGIN_USER_ATTRIBUTE = "loginUser";
    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseAuthClient firebaseAuthClient;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path.equals("/api/auth/login")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/prometheus");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeError(response, request, ErrorCode.AUTH_TOKEN_MISSING);
            return;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            FirebaseUserInfo userInfo = firebaseAuthClient.verifyToken(token);
            User loginUser = userService.findOrCreateUser(userInfo);
            request.setAttribute(FIREBASE_USER_ATTRIBUTE, userInfo);
            request.setAttribute(LOGIN_USER_ATTRIBUTE, loginUser);
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            log.warn("인증 실패: {}", e.getMessage());
            writeError(response, request, e.getErrorCode());
        }
    }

    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            ErrorCode errorCode) throws IOException {
        ErrorResponse body = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI()
        );
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
