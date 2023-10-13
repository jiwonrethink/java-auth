package error;

import lombok.Getter;

@Getter
public class ErrorResponseDto {

    private String code;
    private String ko;
    private String en;

    public ErrorResponseDto(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.ko = errorCode.getKo();
        this.en = errorCode.getEn();
    }
}
