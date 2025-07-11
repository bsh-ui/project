<h1 align="center" style="font-size: 3em;">🎵 ListenIt MAGAZINE</h1>
<h3 align="center"><em>Vol.1 - The Sound of Tomorrow: 당신의 음악, 당신의 방식으로.</em></h3>

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/ListenIt%20Cover.png" alt="ListenIt Cover" width="300px" height="450px" >
</p>

<p align="center">
  <strong>💡 개인화된 음악 경험과 견고한 보안을 제공하는 React + Spring Boot 기반 풀스택 음악 플랫폼</strong><br>
  <strong>개발 기간:</strong> 2025.06.02 ~ 2025.06.10 &nbsp;|&nbsp;
  <strong>Repository:</strong> <a href="https://github.com/bsh-ui/project">GitHub 링크</a>
  </p>

---

## 🌟 Quick Summary for Reviewers (📌 핵심 요약)

| 구분 | 내용 |
|------|------|
| 🎯 **목표** | 개인화된 음악 스트리밍 + 검색 플랫폼 구현 |
| 🛠 **기술 스택** | Java 17, Spring Boot, React 18, MySQL, Apache Solr, JWT, OAuth2 |
| 🔐 **인증** | 폼 로그인 + 소셜 로그인(Google, Naver, Kakao), JWT, 계정 잠금 |
| 🎵 **기능** | 음악 업로드/재생, 플레이리스트 관리, Solr 검색 |
| 💬 **커뮤니티** | 게시판/댓글/좋아요/평점 |
| 📩 **보안기능** | 이메일 기반 비밀번호 재설정 기능 |
| 🧪 **개발 편의** | 테스트 계정, 활동 기록 저장 기능 |

---

## 💡 프로젝트 개요: 음악 서비스에 대한 풀스택 구현과정에서 얻은 학습 경험

<p align="justify">
ListenIt 프로젝트는 6개월간의 개발 교육 과정 중, **음악 스트리밍 서비스에 대한 흥미와 '직접 구현해 볼 수 있을까?' 하는 궁금증**에서 시작되었습니다. 평소 즐겨 사용하던 '지니 뮤직'과 같은 플랫폼을 접하며, **서비스를 직접 설계하고 구현해보고자 하는 목표**를 세웠습니다.<br><br>

단순히 기능을 나열하기보다, **학습했던 다양한 기술 스택을 실제 프로젝트에 적용해보는 도전**을 하고 싶었습니다. 초기에는 백엔드 중심으로 **JPA, OAuth2, JWT 인증 시스템** 등을 구현하며 기능을 확장해나갔습니다. 프로젝트 중반, React를 접하면서 사용자 경험 측면에서 더 나은 선택이라고 판단했고, **프론트엔드를 React, 백엔드를 Spring Boot(STS)로 분리하여 연동하는 아키텍처**를 시도했습니다. 이에 따라 메인 화면과 로그인 기능을 포함한 프론트엔드 부분을 React로 다시 구현하며, 백엔드와 RESTful API로 유기적으로 연동하는 과정을 통해 **분산 시스템 설계의 실제적인 경험**을 얻을 수 있었습니다.<br><br>

**Apache Solr**의 도입은 서비스의 핵심 기능을 고도화하기 위한 선택이었습니다. 마침 교육 과정에서 Solr를 이용한 색인 및 검색 구현을 다루었을 때, 이를 개인 프로젝트에 적용해 보면 좋겠다고 생각했습니다. Solr가 다양한 파일 타입의 메타데이터를 효율적으로 색인하고 검색할 수 있다는 점에 착안하여, **방대한 음악 데이터(가사, 아티스트, 앨범, 노래)를 빠르고 정확하게 검색**하는 기능을 구현했습니다. 나아가 Solr의 확장성을 고려하여, 사용자나 아티스트 검색 시 관련 앨범, 노래, 플레이리스트까지 불러올 수 있는 가능성 또한 구상해보았습니다.<br><br>

ListenIt은 **비교적 짧은 기간 안에 개인이 주도적으로 기획하고 구현한 풀스택 프로젝트**입니다. 이 과정을 통해 저는 **새로운 기술을 학습하고 실제 프로젝트에 적용하는 주도성, 그리고 문제 해결을 위해 끊임없이 고민하고 시도하는 개발자로서의 태도**를 다질 수 있었습니다.
</p>

---

## 🛠 기술 스택

<table>
<tr>
<td><strong>📦 Backend</strong></td>
<td>
<img src="https://img.shields.io/badge/Java-17-blue" />
<img src="https://img.shields.io/badge/SpringBoot-2.7.13-green" />
<img src="https://img.shields.io/badge/MyBatis-2.3.1-orange" />
<img src="https://img.shields.io/badge/JPA-Hibernate-blue" />
<img src="https://img.shields.io/badge/SpringDataJPA-2.7.x-important" />
<img src="https://img.shields.io/badge/JWT-0.11.5-yellow" />
<img src="https://img.shields.io/badge/Lombok-%23FFA500.svg" />
<img src="https://img.shields.io/badge/Tomcat-9.0-blue" />
</td>
</tr>
<tr>
<td><strong>🎨 Frontend</strong></td>
<td>
<img src="https://img.shields.io/badge/React-18-blue" />
<img src="https://img.shields.io/badge/JavaScript-ES6-yellow" />
<img src="https://img.shields.io/badge/jQuery-3.6.0-blue" />
<img src="https://img.shields.io/badge/CSS3-%231572B6.svg" />
<img src="https://img.shields.io/badge/HTML5-%23E34F26.svg" />
</td>
</tr>
<tr>
<td><strong>🔐 Auth & API</strong></td>
<td>
<img src="https://img.shields.io/badge/OAuth2-Naver-green" />
<img src="https://img.shields.io/badge/OAuth2-Kakao-yellow" />
<img src="https://img.shields.io/badge/OAuth2-Google-blue" />
<img src="https://img.shields.io/badge/RESTful-API-red" />
<img src="https://img.shields.io/badge/Ajax-%230078D4.svg" />
</td>
</tr>
</tr>
<tr>
<td><strong>🗃 DB & Tools</strong></td>
<td>
<img src="https://img.shields.io/badge/MySQL-8.0-blue" />
<img src="https://img.shields.io/badge/ApacheSolr-9.8.1-orange" />
<img src="https://img.shields.io/badge/STS-Eclipse-green" />
<img src="https://img.shields.io/badge/VisualStudioCode-blue" />
<img src="https://img.shields.io/badge/GitHub-VersionControl-black" />
</td>
</tr>
</table>

---

## 🚀 핵심 구현 기능: 기술적 도전과 해결

---

### 1. 사용자 인증 및 계정 관리: 안전하고 편리한 음악 여정의 시작

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="500">
  <br>
  <em>(이미지 설명: 폼 로그인, 소셜 로그인 버튼 및 계정 잠금 경고 화면)</em>
</p>

**기능 설명:**

ListenIt은 사용자에게 익숙한 **아이디/비밀번호 기반의 폼 로그인**과 함께 **Google, Kakao, Naver 소셜 로그인**을 모두 통합하여 편리한 인증 경험을 제공합니다. 특히, 무단 접근 시도를 방지하기 위해 **비밀번호 5회 실패 시 자동으로 계정이 잠기는 보안 기능**을 구현하여 사용자 계정의 안전성을 최우선으로 고려했습니다. 로그인 성공 시에는 **JWT 토큰 기반 인증**을 통해 서버 부담을 줄이고 서비스의 확장성을 확보합니다.

#### **[핵심 코드: JWT 토큰 발행 및 계정 보안 처리]**
`backend/src/main/java/com/boot/oauth2/CustomFormSuccessHandler.java`

```java
// Spring Security의 AuthenticationSuccessHandler를 구현하여 로그인 성공 후 처리
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    // 🌟 1. 인증된 사용자 정보 기반 JWT Access Token 및 Refresh Token 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication.getName(), authentication.getAuthorities());
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());

    // 🌟 2. Refresh Token을 HttpOnly 쿠키에 담아 XSS 공격으로부터 보호
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
    refreshTokenCookie.setHttpOnly(true); // JavaScript 접근 방지
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
    response.addCookie(refreshTokenCookie);

    // 🌟 3. 로그인 성공 시 계정 잠금 해제 및 실패 횟수 초기화 (보안 강화 로직)
    userService.handleSuccessfulLogin(authentication.getName()); // 사용자 계정 상태 업데이트

    // 🌟 4. Access Token 및 성공 메시지를 JSON 형태로 클라이언트에 전송 (React에서 처리)
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    PrintWriter writer = response.getWriter();
    writer.write(String.format("{\"message\": \"로그인 성공\", \"accessToken\": \"%s\"}", accessToken));
    writer.flush();
}
