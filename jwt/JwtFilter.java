package jwt;

import error.ErrorCode;
import error.exception.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException, JwtException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
        Boolean isRefrshToken = false;

        if (!requestURI.equals("/")
                && !requestURI.equals("/signup")
                && !requestURI.equals("/login")) { // token 없이 요청 제외
            // access token 재발행인 경우 확인
            if (requestURI.equals("/reissue")) {
                isRefrshToken = true;
            }

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt, isRefrshToken)) {
                Authentication authentication = jwtUtil.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                throw new JwtException(ErrorCode.FAILURE_JWT_VERIFIED);
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String jwt = null;
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            jwt = bearerToken.substring(7);
        }

        return jwt;
    }
}
