# 🎵 ListenIt MAGAZINE

<h3 align="center"><em>Vol.1 - The Sound of Tomorrow: 당신의 음악, 당신의 방식으로.</em></h3>

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/ListenIt%20Cover.png" alt="ListenIt Cover" width="280" height="420" />
</p>

<p align="center">
  <strong>💡 개인화된 음악 경험과 견고한 보안을 제공하는 React + Spring Boot 기반 풀스택 음악 플랫폼</strong><br/>
  <strong>개발 기간:</strong> 2025.06.02 ~ 2025.06.10 &nbsp;&nbsp;|&nbsp;&nbsp;  
  <strong>Repository:</strong> <a href="https://github.com/bsh-ui/project" target="_blank">GitHub 링크</a>
</p>

---

## 🌟 Quick Summary

| 구분       | 내용                                                  |
|------------|-------------------------------------------------------|
| 🎯 목표    | 개인화된 음악 스트리밍 + 검색 플랫폼 구현             |
| 🛠 기술 스택 | Java 17, Spring Boot, React 18, MySQL, Apache Solr, JWT, OAuth2 |
| 🔐 인증    | 폼 로그인 + 소셜 로그인(Google, Naver, Kakao), JWT, 계정 잠금 |
| 🎵 기능    | 음악 업로드/재생, 플레이리스트 관리, Solr 검색         |
| 💬 커뮤니티 | 게시판/댓글/좋아요/평점                               |
| 📩 보안기능 | 이메일 기반 비밀번호 재설정                            |
| 🧪 개발 편의 | 테스트 계정, 활동 기록 저장                            |

---

## 💡 프로젝트 개요

ListenIt은 6개월간의 개발 교육 과정 중 직접 설계 및 구현에 도전한 풀스택 음악 스트리밍 서비스입니다.  
React 프론트엔드와 Spring Boot 백엔드를 RESTful API로 유기적으로 연동하고, Apache Solr를 도입해 고성능 음악 검색을 구현했습니다.  

다양한 인증 방식 통합과 견고한 보안 설계, 음악 업로드 및 스트리밍, 플레이리스트, 커뮤니티 기능까지 폭넓게 다루며 실무 경험을 쌓았습니다.

---

## 🛠 기술 스택

| 구분        | 주요 기술                         |
|-------------|---------------------------------|
| Backend     | Java 17, Spring Boot 2.7, JPA, MyBatis, JWT, Lombok, Tomcat 9 |
| Frontend    | React 18, JavaScript (ES6), jQuery, CSS3, HTML5              |
| 인증 & API  | OAuth2 (Google, Naver, Kakao), RESTful API, Ajax              |
| DB & 도구  | MySQL 8, Apache Solr 9.8, Eclipse(STS), VS Code, GitHub        |

---

## 📌 주요 기능

### 1. 사용자 인증 및 계정 관리

- **폼 로그인 + 소셜 로그인 (Google, Naver, Kakao)**
- 비밀번호 5회 실패 시 자동 계정 잠금
- JWT 기반 토큰 인증 및 HttpOnly 쿠키에 Refresh Token 저장

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="450" alt="로그인 화면" />
</p>

```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    String accessToken = jwtTokenProvider.createAccessToken(authentication.getName(), authentication.getAuthorities());
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());

    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
    response.addCookie(cookie);

    userService.handleSuccessfulLogin(authentication.getName());

    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(String.format("{\"message\":\"로그인 성공\", \"accessToken\":\"%s\"}", accessToken));
}
2. 비밀번호 재설정 이메일 인증
이메일로 인증 코드 발송 후 비밀번호 재설정 지원

Spring Mail 활용, 인증 코드의 무작위성과 일회성 보장

<p align="center"> <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%B4%EB%A9%94%EC%9D%BC.png" width="400" alt="비밀번호 재설정 이메일" /> </p>
java
 
 
public void sendPasswordResetEmail(String email) {
    String code = generateRandomCode();
    emailService.send(email, "비밀번호 재설정 코드", "코드: " + code);
    authCodeRepository.save(email, code, expirationTime);
}
3. 음악 업로드 및 재생
MP3 파일 업로드 및 메타데이터(제목, 아티스트, 앨범 등) 저장

웹 스트리밍 방식으로 끊김 없이 재생 가능

음악 상세 페이지에서 플레이리스트에 추가

<p align="center"> <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" width="450" alt="음악 상세 및 재생" /> </p>
java
 
 
@Transactional
public MusicDTO uploadMusic(MultipartFile file, String title, String artist, Long uploaderId) {
    String filePath = fileStorageService.store(file);
    Music music = new Music(title, artist, filePath, uploaderId);
    musicRepository.save(music);
    return MusicDTO.fromEntity(music);
}
4. 플레이리스트 기능
플레이리스트 생성, 음악 추가/삭제, 이름 변경 지원

사용자의 맞춤 재생목록 관리

<p align="center"> <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" width="450" alt="플레이리스트 관리" /> </p>
java
 
 
public Playlist createPlaylist(Long userId, String name) {
    Playlist playlist = new Playlist(userId, name);
    playlistRepository.save(playlist);
    return playlist;
}
5. Apache Solr 기반 통합 검색
음악 제목, 아티스트, 가사, 앨범 이미지 등 고속 색인 및 검색

색인 자동화 및 Full-text 검색 최적화

<p align="center"> <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%80%EC%83%89.png" width="450" alt="검색 화면" /> </p>
java
 
 
public List<Music> searchMusic(String query) {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery(query);
    QueryResponse response = solrClient.query(solrQuery);
    return response.getBeans(Music.class);
}
6. 커뮤니티 게시판
게시글 CRUD, 댓글 기능

좋아요/싫어요, 별점 평가 기능

<p align="center"> <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%8C%EC%8B%9C%ED%8C%90.png" width="450" alt="커뮤니티 화면" /> </p>
java
 
 
public void addComment(Long postId, Comment comment) {
    comment.setPostId(postId);
    commentRepository.save(comment);
}
7. 공지사항 관리 (관리자 전용)
관리자 권한으로 공지사항 등록, 수정, 삭제 가능

java
 
 
@PreAuthorize("hasRole('ADMIN')")
public void createNotice(Notice notice) {
    noticeRepository.save(notice);
}
8. 개발 편의 기능
테스트용 자동 로그인 계정 제공

로그인 및 음악 재생 활동 로그 저장 및 조회

java
 
 
public void saveActivityLog(Long userId, String action) {
    ActivityLog log = new ActivityLog(userId, action, LocalDateTime.now());
    activityLogRepository.save(log);
}
📢 프로젝트 회고
ListenIt 프로젝트를 통해 React와 Spring Boot의 완전한 통신 구조 설계, 보안 강화, 검색 엔진 연동 등 실무 중심 기술을 직접 구현하며<br>
서비스 기획력과 시스템 설계 능력을 크게 향상시킬 수 있었습니다.

📸 주요 화면 미리보기
로그인 및 계정 잠금	음악 상세 및 플레이리스트	Apache Solr 검색
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="300" />	<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" width="300" />	<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%80%EC%83%89.png" width="300" />

메인 페이지	커뮤니티 게시판	비밀번호 재설정 이메일
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A6%AC%EC%95%A1%ED%8A%B8%EC%97%B0%EB%8F%99%EB%90%9C%ED%99%94%EB%A9%B4.png" width="300" />	<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%8C%EC%8B%9C%ED%8C%90.png" width="300" />	<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%B4%EB%A9%94%EC%9D%BC.png" width="300" />

<p align="center">   ⓒ 2025 ListenIt Project by <strong>박 성 훈</strong> &nbsp;|&nbsp; Powered by Java ☕ + Spring 🌿 + React ⚛️<br/>   <em>“당신의 음악, 당신의 방식으로.”</em> </p
