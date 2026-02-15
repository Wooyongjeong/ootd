# 날씨 별 OOTD 공유 서비스

날씨 기반 OOTD(Outfit Of The Day) 게시물을 공유하고, 좋아요/댓글/조회 이벤트를 반영해 실시간 인기 게시물을 제공하는 SNS 백엔드입니다.

## 1. 프로젝트 개요

- 목적: 날씨/지역 조건으로 피드를 빠르게 조회하고, 참여 지표(좋아요/댓글/조회수) 기반 인기 게시물을 실시간 제공
- 역할: DB 설계, API 서버 구현, Redis 기반 성능 최적화
- 기술 스택: `Java 21`, `Spring Boot`, `Spring Data JPA`, `QueryDSL`, `MySQL`, `Redis`

## 2. 핵심 성능 설계

### 2.1 실시간 인기 게시물 최적화

- 문제: 좋아요/조회수 증가 이벤트를 매번 DB write로 처리하면 동시성 충돌과 성능 저하가 발생
- 적용:
- Redis `Sorted Set`에 실시간 점수 집계
- Redis `Hash`에 증분 카운터 누적
- 5분 주기 `Write-Back` 스케줄러로 DB 배치 반영
- JPA `@Version` + 트랜잭션 격리(`READ_COMMITTED`)로 정합성 보호
- 기대 효과: 고빈도 write 트래픽을 인메모리에서 흡수하고 DB 부하 분산

### 2.2 날씨별 피드 조회 최적화

- 문제: 날씨/지역 필터 + 페이징 조회 시 Full Scan 병목 발생 가능
- 적용:
- QueryDSL 기반 동적 쿼리
- `WHERE + ORDER BY`를 고려한 복합 인덱스
- 콘텐츠 조회 쿼리와 `COUNT` 쿼리 분리
- 기대 효과: 피드 조회 지연 감소 및 처리량 향상

## 3. 아키텍처

- `PostRepositoryImpl`: QueryDSL 기반 피드 쿼리
- `PopularRankingService`: Redis 점수/증분 카운터 처리
- `EngagementWriteBackScheduler`: 5분 주기 DB 반영
- `EngagementService`: 좋아요/조회/댓글 도메인 처리
- `PostService`: 게시물 생성/피드/인기글 조회

## 4. 실행 방법

### 4.1 인프라 실행

```bash
docker compose up -d
```

### 4.2 애플리케이션 실행

```bash
./gradlew bootRun
```

PowerShell:

```powershell
.\gradlew.bat bootRun
```

## 5. 주요 환경변수

- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `3306`)
- `DB_NAME` (default: `ootd`)
- `DB_USERNAME` (default: `ootd`)
- `DB_PASSWORD` (default: `ootd`)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `WRITE_BACK_INTERVAL_MS` (default: `300000`)
- `JWT_SECRET` (JWT HMAC secret, prod 필수)
- `JWT_ACCESS_TOKEN_EXP` (default: `1800`)
- `JWT_REFRESH_TOKEN_EXP` (default: `1209600`)
- `KAKAO_CLIENT_ID` (카카오 REST API 키)
- `KAKAO_CLIENT_SECRET` (선택)

## 6. API 요약

- `POST /api/auth/kakao/login` 카카오 OAuth2 로그인/회원가입(JWT 발급)
- `POST /api/auth/refresh` 액세스 토큰 재발급(리프레시 토큰 회전)
- `POST /api/auth/logout` 로그아웃(리프레시 토큰 폐기, `Authorization: Bearer ...`)
- `POST /api/posts` 게시글 생성 (`Authorization: Bearer ...`)
- `GET /api/posts/feed?weatherType=SUNNY&region=Seoul&page=0&size=20` 피드 조회
- `GET /api/posts/popular?limit=10` 인기 게시물 조회
- `POST /api/posts/{postId}/likes` 좋아요 (`Authorization: Bearer ...`)
- `DELETE /api/posts/{postId}/likes` 좋아요 취소 (`Authorization: Bearer ...`)
- `POST /api/posts/{postId}/views` 조회 이벤트 기록
- `POST /api/posts/{postId}/comments` 댓글 작성 (`Authorization: Bearer ...`)
- `GET /api/posts/{postId}/comments` 최근 댓글 조회(최대 50건)

## 7. 테스트

```bash
./gradlew test
```

PowerShell:

```powershell
.\gradlew.bat test
```

## 8. API 스펙 문서

- OpenAPI 3.1 명세: `docs/openapi.yaml`

## 9. Common Response Format

Every API returns this envelope:

```json
{
  "success": true,
  "code": "COMMON-200",
  "message": "Success",
  "data": {}
}
```

Error example:

```json
{
  "success": false,
  "code": "COMMON-409",
  "message": "Already liked",
  "data": null
}
```
