package error.exception;

import error.ErrorCode;
import lombok.Getter;

@Getter
public class JwtException extends RuntimeException {

    private final ErrorCode errorCode;

    public JwtException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }
}
