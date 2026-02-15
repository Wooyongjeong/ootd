# OOTD API Server

날씨 기반 OOTD(Outfit Of The Day) 공유 서비스의 백엔드 API 서버입니다.  
모바일 앱 클라이언트를 대상으로 하며, 카카오 OAuth2 로그인 + JWT 인증, Redis 기반 실시간 인기 집계, QueryDSL 기반 피드 조회 최적화를 포함합니다.

## 1. 프로젝트 개요

### 1.1 목표
- 날씨/지역 조건 기반으로 OOTD 피드를 빠르게 조회
- 좋아요/댓글/조회 이벤트를 실시간 집계해 인기 게시물 제공
- 모바일 앱에 적합한 무상태 인증(JWT) 구조 제공

### 1.2 핵심 기능
- 카카오 OAuth2 로그인/회원가입
- JWT Access/Refresh 토큰 발급 및 재발급(Refresh Token Rotation)
- 게시글 CRUD 일부(생성/피드/인기 조회)
- 좋아요, 댓글, 조회 이벤트 처리
- 공통 API 응답 포맷 + 전역 예외 처리

### 1.3 기술 스택
- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA
- QueryDSL
- MySQL
- Redis
- JUnit 5 / Mockito / MockMvc

---

## 2. 시스템 아키텍처

### 2.1 인증/인가
- 카카오 OAuth2 인가코드로 로그인
- 서버가 카카오 API를 호출해 사용자 식별
- 내부 User 엔티티 생성/갱신 후 JWT 발급
- 인증이 필요한 API는 `Authorization: Bearer <accessToken>` 사용

### 2.2 데이터 처리
- 게시글/좋아요/댓글: MySQL 저장
- 실시간 인기 점수 및 참여 증분: Redis 저장

### 2.3 성능 최적화 설계
- 실시간 이벤트(좋아요/조회/댓글)는 Redis에 먼저 누적
- 스케줄러가 주기적으로 DB에 Write-Back
- 피드 조회는 QueryDSL + 조건부 where + count 분리

---

## 3. 프로젝트 구조

```text
src/main/java/com/github/wooyong/ootd
├─ auth        # JWT 필터, 토큰 provider, @LoginUser 리졸버
├─ common      # ApiResponse, ResponseCode, GlobalExceptionHandler
├─ config      # Security, QueryDSL, WebMvc 설정
├─ controller  # AuthController, PostController
├─ domain      # User, Post, PostLike, Comment, WeatherType
├─ dto         # 요청/응답 DTO
├─ repository  # JPA/QueryDSL 저장소
├─ scheduler   # Redis -> DB Write-Back 스케줄러
└─ service     # 비즈니스 로직
```

---

## 4. 실행 방법

## 4.1 요구사항
- JDK 21
- Docker / Docker Compose

## 4.2 인프라 실행

```bash
docker compose up -d
```

- MySQL: `localhost:3306`
- Redis: `localhost:6379`

## 4.3 애플리케이션 실행

```bash
./gradlew bootRun
```

PowerShell:

```powershell
.\gradlew.bat bootRun
```

프로파일:
- 기본: `local`
- 운영: `prod`

운영 프로파일 실행:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=prod"
```

---

## 5. 환경 변수

## 5.1 공통
- `WRITE_BACK_INTERVAL_MS` (default: `300000`)
- `JWT_ACCESS_TOKEN_EXP` (default: `1800`)
- `JWT_REFRESH_TOKEN_EXP` (default: `1209600`)

## 5.2 DB
- `DB_HOST` (local default: `localhost`)
- `DB_PORT` (local default: `3306`)
- `DB_NAME` (local default: `ootd`)
- `DB_USERNAME` (local default: `ootd`)
- `DB_PASSWORD` (local default: `ootd`)

## 5.3 Redis
- `REDIS_HOST` (local default: `localhost`)
- `REDIS_PORT` (local default: `6379`)

## 5.4 인증
- `JWT_SECRET` (prod 필수, 충분히 긴 랜덤 문자열)
- `KAKAO_CLIENT_ID` (카카오 REST API 키)
- `KAKAO_CLIENT_SECRET` (선택)

---

## 6. 인증 플로우 (모바일)

1. 모바일 앱이 카카오 로그인 진행 후 `authorizationCode` 획득  
2. 앱이 `POST /api/auth/kakao/login` 호출  
3. 서버가 카카오 토큰/프로필 조회 후 내부 사용자 식별  
4. 서버가 Access/Refresh 토큰 발급 후 반환  
5. 앱은 보호 API 호출 시 Access 토큰을 Bearer로 전송  
6. Access 만료 시 `POST /api/auth/refresh`로 토큰 재발급  
7. 로그아웃 시 `POST /api/auth/logout`

---

## 7. 공통 응답 포맷

모든 API는 아래 래퍼를 사용합니다.

```json
{
  "success": true,
  "code": "COMMON-200",
  "message": "Success",
  "data": {}
}
```

오류 예시:

```json
{
  "success": false,
  "code": "COMMON-409",
  "message": "Already liked",
  "data": null
}
```

---

## 8. 공통 응답 코드

- `COMMON-200` Success
- `COMMON-201` Created
- `COMMON-400` Bad request
- `COMMON-401` Unauthorized
- `COMMON-403` Forbidden
- `COMMON-404` Not found
- `COMMON-409` Conflict
- `COMMON-422` Validation failed
- `COMMON-500` Internal server error

---

## 9. API 요약

## 9.1 Auth
- `POST /api/auth/kakao/login` 로그인/회원가입 + 토큰 발급
- `POST /api/auth/refresh` 토큰 재발급
- `POST /api/auth/logout` 로그아웃 (인증 필요)

## 9.2 Posts
- `POST /api/posts` 게시글 생성 (인증 필요)
- `GET /api/posts/feed` 피드 조회
- `GET /api/posts/popular` 인기 게시글 조회

## 9.3 Engagement
- `POST /api/posts/{postId}/likes` 좋아요 (인증 필요)
- `DELETE /api/posts/{postId}/likes` 좋아요 취소 (인증 필요)
- `POST /api/posts/{postId}/views` 조회 이벤트
- `POST /api/posts/{postId}/comments` 댓글 작성 (인증 필요)
- `GET /api/posts/{postId}/comments` 댓글 조회

---

## 10. API 상세 예시

## 10.1 카카오 로그인

Request:

```http
POST /api/auth/kakao/login
Content-Type: application/json

{
  "authorizationCode": "kakao-auth-code",
  "redirectUri": "myapp://oauth"
}
```

Response (201):

```json
{
  "success": true,
  "code": "COMMON-201",
  "message": "Created",
  "data": {
    "accessToken": "....",
    "refreshToken": "....",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "userId": 12345,
    "nickname": "tester"
  }
}
```

## 10.2 게시글 생성

Request:

```http
POST /api/posts
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "weatherType": "SUNNY",
  "region": "Seoul",
  "content": "오늘 코디",
  "imageUrl": "https://example.com/image.jpg"
}
```

## 10.3 피드 조회

```http
GET /api/posts/feed?weatherType=SUNNY&region=Seoul&page=0&size=20
```

## 10.4 좋아요

```http
POST /api/posts/{postId}/likes
Authorization: Bearer {accessToken}
```

---

## 11. 테스트

전체 테스트:

```bash
./gradlew test
```

PowerShell:

```powershell
.\gradlew.bat test
```

참고:
- 로컬 환경에서 JDK 21이 없으면 Gradle toolchain 오류가 발생할 수 있습니다.

---

## 12. OpenAPI 문서

- OpenAPI 3.1 명세: `docs/openapi.yaml`

---

## 13. 운영 시 권장 사항

- `prod`에서 `JWT_SECRET`은 길고 예측 불가능한 값 사용
- Refresh Token 저장소(Redis) 장애 대응 모니터링 설정
- MySQL 인덱스/쿼리 플랜 정기 점검
- 카카오 API 에러율/지연 모니터링
- 로그에 토큰/민감정보 기록 금지

---

## 14. 트러블슈팅

## 14.1 `Cannot find a Java installation matching languageVersion=21`
- 원인: JDK 21 미설치
- 조치: JDK 21 설치 후 `JAVA_HOME` 설정

## 14.2 `401 Unauthorized`
- Access 토큰 누락/만료/형식 오류 가능
- `Authorization: Bearer <token>` 형식 확인
- 만료 시 `/api/auth/refresh` 사용

## 14.3 `COMMON-409` 좋아요 충돌
- 이미 좋아요된 상태에서 중복 좋아요 요청
- 클라이언트에서 버튼 디바운스/상태 동기화 처리 권장
