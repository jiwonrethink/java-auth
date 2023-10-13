package error;

import com.companywe.xergy.csms.api.constant.LangCd;
import domain.ErrorMessage;
import repository.ErrorMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND_USER,
    NOT_FOUND_PERMISSION,

    INVALID_JWT,
  
    FAILURE_JWT_VERIFIED,
  
    JWT_EXPIRED,
    NOT_SUPPORT_JWT,
    PERMISSION_DENIED,

    SERVER_ERROR;

    private String code;
    private String ko;
    private String en;

    @Slf4j
    @Component
    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    public static class ErrorMessageInjector {
        private final ErrorMessageRepository errorMessageRepository;

        @PostConstruct
        public void setErrorMessage() {
            try {
                for (ErrorCode errorCode : ErrorCode.values()) {
                    List<ErrorMessage> errorList = errorMessageRepository.findAllByName(errorCode.name());
                    ErrorMessage koErrInfo = errorList.stream()
                            .filter(error -> error.getLang().equals(LangCd.KO.getValue())).findAny().orElse(null);
                    ErrorMessage enErrInfo = errorList.stream()
                            .filter(error -> error.getLang().equals(LangCd.EN.getValue())).findAny().orElse(null);

                    errorCode.code = koErrInfo.getCode();
                    errorCode.ko = koErrInfo.getMessage();
                    errorCode.en = enErrInfo.getMessage();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
