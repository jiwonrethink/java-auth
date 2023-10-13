package jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPrincipal {

    private String agentId;
    private String userUuid;
}
