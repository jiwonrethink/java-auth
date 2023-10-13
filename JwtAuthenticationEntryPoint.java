package jwt;

import constant.ResponseStatus;
import error.ErrorResponseDto;
import error.exception.JwtException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {
    static final int ERROR_STATUS_CODE = 401;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            setErrorResponse(response, e);
        }
    }
    private void setErrorResponse(HttpServletResponse response, JwtException error) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(ERROR_STATUS_CODE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(ERROR_STATUS_CODE, new ErrorResponseDto(error.getErrorCode()));

        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Data
    public static class ErrorResponse {
        private final String status = ResponseStatus.ERROR.name();
        private final int code;
        private final ErrorResponseDto data;
    }
}
