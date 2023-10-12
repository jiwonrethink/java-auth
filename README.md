# java-auth
Spring Security + JWT 이용 인증

### Refresh Token과 Session Sliding 방식 사용
<img src="https://velog.velcdn.com/images/hyehyeonmoon/post/e9a58e7c-5e5f-426a-9466-997168a18aea/image.png" width="700" height="400"><br>
#### 1/2/3/4) 토큰 생성 case: AccessToken/RefreshToken 생성하여 Redis에 저장 후 token 전달
#### 5/6/7) 토큰 만료 case: API 요청시 AccessToken 만료된 경우: 특정 에러 응답
#### 8/9/10) 토큰 재발행 case: RefreshToken 유효한지 확인 후 유효한 경우 AccessToken 전달, 유효하지 않은 경우 특정 에러 전달
