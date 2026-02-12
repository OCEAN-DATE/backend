# OceanDate Backend

소개팅 매칭 데이팅 플랫폼 **OceanDate**의 백엔드 서버입니다.
1:1 소개팅, 로테이션(단체) 소개팅, 여행 소개팅 등 다양한 매칭 유형을 지원하며,
결제, 쿠폰, 리뷰, 관리자 기능을 포함한 종합 플랫폼입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Security** | Spring Security, OAuth2 (Kakao), JWT (JJWT 0.11.5) |
| **Database** | MySQL, Spring Data JPA |
| **Cache** | Redis (Reactive) |
| **Payment** | Toss Payments API |
| **SMS** | Naver Cloud Platform SENS |
| **Storage** | AWS S3 |
| **Docs** | Swagger / OpenAPI (springdoc 2.7.0) |
| **Monitoring** | Spring Actuator, Micrometer Prometheus |
| **CI/CD** | GitHub Actions → AWS EC2 |
| **Build** | Gradle 9.2.1 |

---

## 주요 기능

### 1. 인증 & 회원 관리
- **Kakao OAuth2** 소셜 로그인
- **JWT 토큰** 기반 인증 (Access Token: 3시간, Refresh Token: 7일)
- 토큰 자동 갱신 (Refresh Token Rotation)
- HttpOnly/Secure 쿠키로 토큰 관리
- 이메일 기반 관리자 역할 자동 부여
- 회원 탈퇴 (Soft Delete)

### 2. 매칭 시스템

#### 1:1 소개팅 (OneToOne)
- 이벤트별 신청 → 관리자 승인 → 결제 → 매칭 → 완료
- 선호 날짜 기반 일정 매칭
- 남녀 쌍 매칭 (OneToOneMatching)
- 상대방 취소 시 자동 전액 환불

#### 로테이션 소개팅 (Rotation)
- 단체 소개팅 이벤트 (남녀 정원 관리)
- 성별별 정원 자동 관리 (OPEN ↔ CLOSED)
- 비관적 락(Pessimistic Lock)으로 동시 신청 처리

#### 여행 소개팅 (Travel)
- 다일 여행 이벤트 (일정 + 숙소 관리)
- 일별 스케줄(TravelSchedule) 및 숙소(Accommodation) CRUD
- 24시간 이내 재신청 지원

### 3. 결제 시스템
- **Toss Payments** API 연동 (결제 확인, 조회, 취소)
- 결제 실패 시 자동 롤백 (Toss 결제 취소)
- 비관적 락으로 중복 결제 방지
- 멱등성 지원 (Idempotency-Key)

#### 환불 정책
| 취소 시점 | 환불율 |
|-----------|--------|
| 결제 당일 & 이벤트 5일 이상 전 | 100% |
| 이벤트 3일 이상 전 | 80% |
| 이벤트 2~3일 전 | 50% |
| 이벤트 2일 이내 | 0% |
| 상대방 취소로 인한 자동 취소 | 100% |

### 4. 쿠폰 시스템
- **웰컴 쿠폰**: 회원가입 시 자동 발급
- **리뷰 쿠폰**: 리뷰 작성 시 발급
- **관리자 발급**: 개별/대량 발급 (리뷰 작성자, 특정 회원, 전체 회원)
- 정률(%) / 정액(원) 할인 지원
- 최소 주문 금액, 최대 할인 금액 설정
- 자동 만료 처리 (스케줄러)

### 5. 리뷰 시스템
- 매칭 완료 후 30일 이내 작성 가능
- 별점 (1~5) + 텍스트 리뷰
- 이벤트 상세 페이지에 리뷰 표시
- 수정/삭제 가능 (본인만, 30일 이내)

### 6. 관리자 기능
- 전체 회원 조회 (블랙리스트 포함/제외)
- 이벤트 CRUD (이미지 S3 업로드)
- 신청 상태 관리 (승인 시 SMS 결제 링크 자동 발송)
- 쿠폰 생성/관리/발급
- 여행 일정 및 숙소 관리

### 7. 마이페이지
- 전체 매칭 이력 통합 조회 (1:1, 로테이션, 여행)
- 리뷰 작성 가능 여부 표시
- 작성한 리뷰 포함

---

## 환경 설정

### 필수 환경변수

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/oceandate
spring.datasource.username={DB_USER}
spring.datasource.password={DB_PASSWORD}

# JWT
jwt.secret={BASE64_ENCODED_256BIT_KEY}

# OAuth2 (Kakao)
spring.security.oauth2.client.registration.kakao.client-id={KAKAO_CLIENT_ID}
spring.security.oauth2.client.registration.kakao.client-secret={KAKAO_CLIENT_SECRET}
spring.security.oauth2.client.registration.kakao.redirect-uri={KAKAO_REDIRECT_URI}

# Toss Payments
toss.payments.secret-key={TOSS_SECRET_KEY}

# Naver Cloud SMS (SENS)
toss.sms.service-id={NCP_SERVICE_ID}
toss.sms.access-key={NCP_ACCESS_KEY}
toss.sms.secret-key={NCP_SECRET_KEY}
toss.sms.from-phone={SENDER_PHONE}

# AWS S3
cloud.aws.s3.bucket.name={S3_BUCKET_NAME}
cloud.aws.region.static=ap-northeast-2
cloud.aws.credentials.access-key={AWS_ACCESS_KEY}
cloud.aws.credentials.secret-key={AWS_SECRET_KEY}

# Frontend URL
app.frontend-url={FRONTEND_URL}

# Admin
admin.emails={ADMIN_EMAIL_1},{ADMIN_EMAIL_2}
```

### 프로필
- `prod` - 운영 환경
- `test` - 테스트 환경

---

## 실행 방법

### 사전 요구사항
- Java 21+
- MySQL 8.x
- Redis

### 로컬 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

### API 문서 확인
서버 실행 후 Swagger UI 접속:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 배포

GitHub Actions를 통한 자동 배포 파이프라인:

```
main 브랜치 push
    → GitHub Actions 트리거
    → JDK 21 설정 & Gradle 빌드 (테스트 제외)
    → 빌드된 JAR를 EC2로 SCP 전송
    → EC2에서 기존 프로세스 종료 후 새 JAR 실행
```

**배포 경로**: `/home/ubuntu/meetup/build/libs/backend-0.0.1-SNAPSHOT.jar`

**실행 프로필**: `--spring.profiles.active=prod,test`