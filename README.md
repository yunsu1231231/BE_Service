# NewsTickr (BE)
AI 기반 증권 요약 및 평가 서비스


## 🖥️ 프로젝트 소개 및 배경
뉴스티커는? 증권 거래소에서 시세가 시시각각으로 변동하는 것처럼, 뉴스도 신속하고 정확하게 전달되어 실시간으로 흐른다는 의미를 갖고 있다.

1. 투자자들은 다양한 뉴스 속에서 신뢰할 수 있는 핵심 정보를 빠르게 파악하기 어려움
2. Open API를 활용해 증권 종목 뉴스를 요약·평가하는 서비스를 기획
3. 댓글 기능과 관리자 시스템을 통해 신뢰할 수 있는 금융 정보 커뮤니티를 구축

<br>

## 팀원 구성

<div align="center">

| **곽동헌** | **김우영** | **김지현** | **이다빈** | **조윤수** |
| :------: | :------: | :------: | :------: | :------: |
| ![곽동헌](https://raw.githubusercontent.com/NewsTickr/FE/main/asset/a.png) | ![김우영](https://raw.githubusercontent.com/NewsTickr/FE/main/asset/b.png) | ![김지현](https://raw.githubusercontent.com/NewsTickr/FE/main/asset/c.png) | ![이다빈](https://raw.githubusercontent.com/NewsTickr/FE/main/asset/d.png) | ![조윤수](https://raw.githubusercontent.com/NewsTickr/FE/main/asset/e.png) |

</div>

</div>

<br>

## 🕰️ 개발 기간
* 25.02.24 ~ 25.02.27


<br>

## 🗂️ 파일구조

```
BE_Service/
├── README.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── src/
    └── main/
        ├── java/
        │   └── com/newstickr/newstickr/
        │       ├── NewsTickrApplication.java
        │       ├── admin/
        │       │   ├── controller/
        │       │   │   ├── AdminUserController.java
        │       │   │   └── AuthController.java
        │       │   ├── dto/
        │       │   ├── repository/
        │       │   └── service/
        │       ├── comment/
        │       │   ├── controller/CommentController.java
        │       │   ├── dto/
        │       │   ├── entity/Comment.java
        │       │   ├── entity/CommentLike.java
        │       │   ├── repository/
        │       │   └── service/CommentService.java
        │       ├── common/
        │       │   ├── aop/
        │       │   └── config/OpenAPIConfig.java
        │       ├── news/
        │       │   ├── controller/NewsController.java
        │       │   ├── dto/
        │       │   ├── entity/News.java
        │       │   ├── repository/NewsRepository.java
        │       │   └── service/
        │       │       ├── NewsService.java
        │       │       └── NewsServiceImpl.java
        │       ├── security/
        │       │   ├── config/
        │       │   │   ├── CorsMvcConfig.java
        │       │   │   └── SecurityConfig.java
        │       │   ├── jwt/
        │       │   │   ├── JWTFilter.java
        │       │   │   └── JWTUtil.java
        │       │   ├── oauth2/CustomSuccessHandler.java
        │       │   └── service/CustomUserDetails.java
        │       └── user/
        │           ├── controller/UserController.java
        │           ├── dto/
        │           ├── entity/User.java
        │           ├── enums/Role.java
        │           ├── repository/UserRepository.java
        │           └── service/
        │               ├── CustomOAuth2UserService.java
        │               ├── FileService.java
        │               └── UserService.java
        └── resources/
            └── static/
                ├── analysis-improvement.html

```

### ERD
<img width="1074" height="555" alt="image" src="https://github.com/user-attachments/assets/0b126e12-f65e-4575-8aff-256f42f79b31" />

<br />

### 🧑‍🤝‍🧑 맴버구성
 - 백엔드  : 곽동헌,김우영,조윤수
 - 프론트엔드 : 김지현,이다빈

<br>

### ⚙️ 기술 스택

프론트엔드: React (뉴스 목록)
백엔드: Spring Boot (뉴스 API, 댓글 관리, Open API 연동)
데이터베이스: MySQL
배포: Docker, Docker Hub, docker - compose
AI: Groq API (기사 요약 및 평가 )

<br>


## 📌 주요 기능

#### 뉴스(News)
- 뉴스 검색: 네이버 뉴스 Open API 연동으로 키워드 기반 기사 검색 (`/news/news?query=...`)
- 게시글 기능: 뉴스 선택 후 게시글 작성/조회/검색/삭제 (JPA 기반 저장)
- AI 감정 분석: Groq API(LLM)로 뉴스 요약문 감정 분석 (`/news/analysis`)

#### 댓글(Comment)
- 댓글 CRUD: 뉴스별 댓글 조회/추가/수정/삭제
- 좋아요: 댓글 좋아요 토글 + 좋아요 여부 확인 + 사용자가 좋아요한 댓글 목록 조회

#### 사용자(User)
- 프로필 조회/수정: 이름/이메일 수정, 타인 프로필 조회
- 프로필 이미지: 업로드/조회/다운로드 (정적 리소스 및 파일 제공)

#### 인증/보안(Security)
- 네이버 OAuth2 로그인: OAuth2 로그인 성공 시 JWT 발급
- JWT 인증: `Authorization` 쿠키 기반으로 JWT를 읽어 인증 처리(JWT Filter)
- 권한 관리: 관리자 API는 `ROLE_ADMIN` 권한 필요

#### 관리자(Admin)
- 회원 관리: 전체 회원 조회(페이지네이션), 회원 Role 변경
- 콘텐츠 관리: 사용자 댓글 삭제, 뉴스 게시글 삭제

#### API 문서(OpenAPI/Swagger)
- `springdoc-openapi` 기반으로 Swagger UI 사용 가능 (프로젝트 설정에 따라 경로 상이)
<br>






