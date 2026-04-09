<div align="center">
  <h1>🧠 AI Intelligence CRM — API Server</h1>
  <p><strong>AI 기반 상담 기록 관리 시스템의 RESTful API 게이트웨이</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Boot%203.5-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
    <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white"/>
    <img src="https://img.shields.io/badge/Google_OAuth2-4285F4?style=flat-square&logo=google&logoColor=white"/>
    <img src="https://img.shields.io/badge/Flyway-CC0200?style=flat-square&logo=flyway&logoColor=white"/>
    <img src="https://img.shields.io/badge/Elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white"/>
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white"/>
    <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
    <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white"/>
  </p>
  <p>
    <img src="https://img.shields.io/badge/포트-8080-blue?style=flat-square"/>
    <img src="https://img.shields.io/badge/인프라_포함-docker/_폴더-informational?style=flat-square"/>
  </p>
</div>

---

## 📌 목차

1. [시스템 개요](#-시스템-개요)
2. [이 레포의 역할](#-이-레포의-역할--api-server)
3. [기술 스택](#-기술-스택)
4. [인프라 구성](#-인프라-구성)
5. [환경 변수](#-환경-변수)
6. [시작하기](#-시작하기)
7. [API 문서](#-api-문서)
8. [관련 레포지토리](#-관련-레포지토리)
9. [Git 컨벤션](#-git-컨벤션)

---

## 🌐 시스템 개요

**AI Intelligence CRM** 은 대규모 상담 데이터를 AI로 분석·인덱싱하고, 이를 빠르게 검색·조회할 수 있는 **AI 기반 상담 기록 관리 시스템**입니다. 시스템은 역할에 따라 두 개의 백엔드 서버로 분리되어 운영됩니다.

| 서버 | 역할 | 포트 |
| :--- | :--- | :---: |
| **API Server** (이 레포) | 실시간 사용자 요청 처리, 상담 조회·검색·인증 API | `8080` |
| **Batch Server** ([AI-Intelligence-CRM-BE-Batch](https://github.com/CoderGogh/AI-Intelligence-CRM-BE-Batch)) | 대용량 상담 데이터 배치 처리, Gemini AI 분석, 검색 인덱싱 | `8081` |

---

## ⚙️ 이 레포의 역할 — API Server

이 서버는 프론트엔드와 데이터 저장소 사이의 **RESTful API 게이트웨이** 역할을 합니다.

Spring Security + JWT 기반 인증·인가, Google OAuth2 소셜 로그인, Flyway 기반 DB 마이그레이션 관리가 핵심이며, Batch 서버가 인덱싱한 Elasticsearch 데이터를 실시간으로 검색해 저지연 응답을 제공합니다.

```
프론트엔드 클라이언트
      │
      ▼ REST API (Spring Security + JWT)
  API Server :8080
      │
      ├──▶ MySQL          (상담·유저 원천 데이터 Read/Write)
      ├──▶ Elasticsearch  (AI 분석 결과 전문 검색 조회)
      ├──▶ MongoDB        (AI 분석 문서 조회)
      └──▶ Redis          (토큰 캐싱, 세션 관리)
```

---

## 🛠 기술 스택

### Core

| 기술 | 버전 | 용도 |
| :--- | :---: | :--- |
| Java | 17 | 메인 언어 |
| Spring Boot | 3.5.10 | 애플리케이션 프레임워크 |
| Spring Security | - | 인증·인가 처리, 요청 필터 체인 |
| JWT (`jjwt`) | 0.12.5 | Access / Refresh 토큰 발급 및 검증 |
| Google OAuth2 | 2.2.0 | Google 소셜 로그인 |
| Flyway | 11.7.2 | DB 스키마 버전 관리 및 자동 마이그레이션 |
| Spring Validation | - | 요청 입력값 유효성 검사 |
| Swagger (springdoc) | 2.8.8 | REST API 문서 자동화 |
| Dotenv (`spring-dotenv`) | 4.0.0 | `.env` 파일 자동 로드 |
| Lombok | - | 보일러플레이트 코드 제거 |

### 데이터 저장소

| 기술 | 용도 |
| :--- | :--- |
| MySQL | 상담·유저 원천 데이터 (Flyway로 스키마 관리) |
| Elasticsearch | Batch 서버가 인덱싱한 상담 데이터 전문 검색 |
| MongoDB | Batch 서버가 저장한 AI 분석 결과 문서 조회 |
| Redis | JWT Refresh Token 캐싱, 세션/상태 관리 |

### 인프라

| 기술 | 용도 |
| :--- | :--- |
| Docker / Docker Compose | 전체 인프라 컨테이너 관리 (`docker/` 폴더) |
| eclipse-temurin:17-jdk | API 서버 컨테이너 베이스 이미지 |
| G1GC (`-XX:+UseG1GC`) | GC 최적화 (힙 128MB ~ 512MB) |

---

## 🏗 인프라 구성

이 레포의 `docker/` 폴더에 **전체 시스템 인프라**가 정의되어 있습니다. Batch 서버 실행에도 이 docker-compose를 먼저 띄워야 합니다.

```bash
cd docker
docker compose up -d
```

### 컨테이너 목록 및 접속 정보

| 컨테이너 | 용도 | 로컬 포트 | 접속 확인 |
| :--- | :--- | :---: | :--- |
| `crm-mysql` | 원천 데이터 DB | `13306` | `mysql -h 127.0.0.1 -P 13306 -u crm -p` |
| `crm-mongo` | AI 분석 결과 저장 | `27018` | `mongosh mongodb://localhost:27018` |
| `crm-mongo-ui` | MongoDB 관리 UI | `18081` | http://localhost:18081 |
| `crm-redis` | 캐시 / 토큰 저장 | `6380` | `redis-cli -p 6380 ping` → `PONG` |
| `crm-es` | 전문 검색 인덱스 | `9201` | http://localhost:9201 |
| `crm-kibana` | ES 모니터링 UI | `15601` | http://localhost:15601 |

> 로컬 포트가 기본값(3306, 27017, 6379, 9200)과 다르므로 연결 설정 시 주의하세요.

---

## 🔐 환경 변수

`.env.example`을 복사해 `.env` 파일을 생성하고 아래 항목을 채워넣으세요.

```bash
cp .env.example .env
```

> `spring-dotenv` 라이브러리가 적용되어 있어 `.env` 파일만 만들면 IDE 환경변수 별도 설정 없이 자동 로드됩니다.

### 로컬 개발 환경 (`application-local.yml`)

일반 로그인 테스트만 할 경우 아래 3개는 **설정하지 않아도** 서버가 실행됩니다.

| 변수명 | 설명 |
| :--- | :--- |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID — 소셜 로그인 테스트 시 필요 |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 — 절대 외부 노출 금지 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) — 미설정 시 기본값으로 동작 |

### 프로덕션 환경 (`application-prod.yml`)

`SPRING_PROFILES_ACTIVE=prod` 설정 시 아래 항목이 **모두 필수**입니다.

| 변수명 | 설명 |
| :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` 로 설정 |
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 접속 계정 |
| `MONGODB_URI` | MongoDB 연결 URI |
| `REDIS_HOST` / `REDIS_PORT` | Redis 접속 정보 |
| `ES_URIS` | Elasticsearch 엔드포인트 |
| `KAFKA_SERVERS` | Kafka 브로커 주소 |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth (프로덕션용) |
| `JWT_SECRET` | JWT 시크릿 (`openssl rand -base64 32` 권장) |

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Gradle
- Docker Desktop (Mac/Windows) 또는 Linux + Docker Engine + docker compose

### 1단계 — 인프라 컨테이너 실행

```bash
# 이 레포 루트 기준
cd docker
docker compose up -d

# 실행 확인
docker ps
```

`crm-mysql`, `crm-mongo`, `crm-redis`, `crm-es`, `crm-kibana`, `crm-mongo-ui` 6개가 모두 기동되어야 합니다.

### 2단계 — 레포지토리 클론 및 환경 변수 설정

```bash
git clone https://github.com/CoderGogh/AI-Intelligence-CRM-BE-Api.git
cd AI-Intelligence-CRM-BE-Api

cp .env.example .env
# .env 파일에서 필요한 값 입력
```

### 3단계 — 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun
```

> Flyway가 자동으로 실행되어 DB 스키마 마이그레이션을 처리합니다.

### 4단계 — Docker 이미지 빌드 및 실행 (선택)

```bash
./gradlew build
docker build -t crm-api .
docker run -p 8080:8080 --env-file .env crm-api
```

> Dockerfile은 `eclipse-temurin:17-jdk` 기반이며, JVM 옵션 `-Xms128m -Xmx512m -XX:+UseG1GC`가 적용됩니다.

---

## 📖 API 문서

서버 실행 후 Swagger UI에서 전체 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/api/swagger-ui/index.html
```

> Spring Security가 적용되어 있으므로, 로그인 후 발급된 JWT를 Swagger UI의 **Authorize** 버튼에 입력해야 인증이 필요한 API를 테스트할 수 있습니다.

---

## 🔗 관련 레포지토리

| 레포 | 역할 |
| :--- | :--- |
| [CoderGogh/AI-Intelligence-CRM-BE-Api](https://github.com/CoderGogh/AI-Intelligence-CRM-BE-Api) | 이 레포 — API 서버, 인증, 실시간 조회·검색, 인프라 Docker 구성 |
| [CoderGogh/AI-Intelligence-CRM-BE-Batch](https://github.com/CoderGogh/AI-Intelligence-CRM-BE-Batch) | Batch 서버 — Gemini AI 분석, 대용량 배치 처리, 검색 인덱싱 |

---

## 📐 Git 컨벤션

### 브랜치 전략

| 브랜치 | 설명 |
| :--- | :--- |
| `main` | 배포 대상 브랜치 |
| `develop` | 개발 통합 브랜치 |
| `feat/{feature-name}` | 기능 개발 브랜치 — develop으로 병합 |
| `hotfix/` | 버그 수정 브랜치 |
| `docs/` | 문서 작업 브랜치 |

**브랜치 예시**

```
feat/users
feat/consult
feat/jira에_등록된_기능_이름
```

### 커밋 타입

| 타입 | 설명 |
| :--- | :--- |
| `feat` | 새로운 기능 추가 또는 요구사항 반영 수정 |
| `fix` | 버그 수정 |
| `build` | 빌드 설정, 모듈 설치·삭제 |
| `chore` | 패키지 매니저, `.gitignore` 등 기타 수정 |
| `ci` | CI 설정 수정 |
| `docs` | 문서·주석 수정 |
| `style` | 코드 스타일·포맷팅 수정 |
| `refactor` | 기능 변화 없는 코드 리팩터링 |
| `test` | 테스트 코드 추가·수정 |
| `release` | 버전 릴리즈 |

**커밋 메시지 예시**

```
feat/홍길동/auth/google_oauth2_소셜로그인_구현
fix/홍길동/consult/elasticsearch_검색_쿼리_오류_수정
build/홍길동/flyway_마이그레이션_스크립트_추가
```
