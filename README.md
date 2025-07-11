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

### 1. 사용자 인증 및 계정 관리: 견고한 보안과 편리한 접근의 통합

<p align="center">
  <img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="500">
  <br>
  <em>(이미지 설명: 폼 로그인, 소셜 로그인 버튼 및 계정 잠금 경고 화면)</em>
</p>

**기능 설명:**

ListenIt은 사용자에게 익숙한 **아이디/비밀번호 기반의 폼 로그인**과 함께 **Google, Kakao, Naver 소셜 로그인**을 모두 통합하여 편리한 인증 경험을 제공합니다. 무단 접근 시도를 방지하기 위해 **비밀번호 5회 실패 시 자동으로 계정이 잠기는 보안 기능**을 구현했으며, 로그인 성공 시에는 **JWT 토큰 기반 인증**을 통해 서버 부담을 줄이고 서비스의 확장성을 확보합니다. 또한, 비밀번호를 잊어버린 사용자를 위해 **이메일 인증 기반의 재설정 기능**을 구현하여 안전하고 사용자 친화적인 계정 복구 경로를 제공합니다.

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
[핵심 코드: 이메일 인증 코드 생성 및 발송]
backend/src/main/java/com/boot/service/AuthService.java (가정)

Java

// 사용자 이메일로 비밀번호 재설정 인증 코드를 발송하는 메서드
@Transactional
public void sendPasswordResetEmail(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    // 🌟 1. 무작위 인증 코드 생성 (보안 강화)
    String verificationCode = generateRandomCode(); // 예: 6자리 숫자/문자 조합 함수 호출
    // 🌟 2. 생성된 코드를 DB에 저장하며 유효 시간 설정 (예: 10분)
    userService.saveVerificationCode(user.getId(), verificationCode, LocalDateTime.now().plusMinutes(10));

    // 🌟 3. 이메일 내용 구성 및 Spring Mail 연동을 통한 이메일 발송
    String emailContent = String.format("안녕하세요, ListenIt 입니다.\n\n" +
            "비밀번호 재설정을 위한 인증 코드입니다: <strong>%s</strong>\n\n" +
            "이 코드는 10분간 유효합니다. 절대 타인에게 공유하지 마세요.", verificationCode);
    emailService.sendEmail(email, "ListenIt 비밀번호 재설정 인증 코드", emailContent);
    log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
}
기술적 도전 및 해결:

다중 인증 방식 통합: 폼 로그인, 소셜 로그인, JWT 기반 인증을 하나의 Spring Security 설정에 유기적으로 통합하는 것이 가장 큰 난관이었습니다. CustomAuthenticationSuccessHandler 및 JwtAuthenticationFilter의 정교한 설정을 통해 이 문제를 해결하고 확장성 높은 인증 아키텍처를 구축했습니다.

JWT 토큰 관리 보안: 클라이언트와 백엔드 간 JWT 토큰을 안전하게 주고받기 위해, HttpOnly 쿠키에 Refresh Token을 저장하고 Access Token은 JSON 응답으로 전달하는 방식을 채택했습니다. 이를 통해 XSS 공격으로부터 Refresh Token을 보호하고, 사용자 경험을 해치지 않으면서도 보안성을 강화했습니다.

계정 잠금 및 해제 로직 구현: 로그인 실패 횟수를 DB에 기록하고 특정 횟수(5회) 초과 시 계정을 잠그는 로직을 구현했으며, 로그인 성공 시 자동으로 잠금 해제 및 실패 횟수를 초기화하여 보안과 사용자 편의성을 동시에 잡았습니다.

보안에 민감한 비밀번호 재설정 처리: 비밀번호 재설정 과정은 무단 계정 탈취의 위험이 있어 보안에 매우 민감합니다. 사용자 이메일로 발송되는 인증 코드의 '일회성', '유효 시간 제한', 그리고 '무작위성'을 철저히 보장하는 로직을 구현하여 무단 접근 시도를 원천적으로 차단했습니다.

2. 음악 콘텐츠 관리 및 재생: 대용량 미디어의 효율적 스트리밍과 개인화된 경험




(이미지 설명: 음악 상세 페이지 및 플레이리스트 추가 기능)

기능 설명:

ListenIt의 핵심은 음악 감상 경험입니다. 관리자가 업로드한 MP3 파일은 웹 환경에서 스트리밍 방식으로 끊김 없이 재생됩니다. 각 음악의 상세 정보를 제공하며, 사용자는 이 곡을 기존 플레이리스트에 손쉽게 추가하거나 새로운 플레이리스트를 즉시 생성할 수 있습니다. 생성된 플레이리스트는 음악 추가/삭제, 순서 변경, 제목 변경 등 세부적인 관리가 가능합니다.

[핵심 코드: 효율적인 음악 파일 저장 전략 및 스트리밍 처리]
backend/src/main/java/com/boot/service/MusicService.java (가정, 업로드/재생 관련)

Java

// 관리자용 MP3 파일 업로드 및 메타데이터 저장 로직 (관리자 전용 기능)
@Transactional
public Music uploadMusic(MultipartFile mp3File, MusicMetadataDTO metadata) throws IOException {
    // 🌟 1. 대용량 파일을 DB에 직접 저장하는 대신 서버 물리 경로에 저장
    String uploadDir = "/uploads/music/"; // 실제 서버 경로
    String uniqueFileName = UUID.randomUUID().toString() + "-" + mp3File.getOriginalFilename();
    Path filePath = Paths.get(uploadDir, uniqueFileName);
    Files.copy(mp3File.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // 🌟 2. 물리적 파일의 URL/경로 정보만 DB에 저장하여 DB 부하 경감
    Music music = Music.builder()
            .title(metadata.getTitle())
            .artist(metadata.getArtist())
            .album(metadata.getAlbum())
            .filePath(filePath.toString()) // 파일 시스템 경로 저장
            .fileUrl("/api/music/stream/" + music.getId()) // 스트리밍 URL 생성 (가정)
            .build();
    return musicRepository.save(music);
}

// 음악 스트리밍 응답 (HttpHeaders.CONTENT_RANGE, Resource 등 활용)
public ResponseEntity<Resource> streamMusic(Long musicId) {
    Music music = musicRepository.findById(musicId).orElseThrow(() -> new MusicNotFoundException("음악을 찾을 수 없습니다."));
    Resource resource = new FileSystemResource(music.getFilePath()); // 물리적 파일 리소스 로드

    // 🌟 3. Content-Type 및 Content-Length 설정으로 안정적인 스트리밍 제공
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg"); // MP3 MIME 타입
    headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
    return ResponseEntity.ok().headers(headers).body(resource);
}
기술적 도전 및 해결:

대용량 파일의 효율적 관리: MP3와 같은 대용량 미디어 파일을 데이터베이스에 직접 저장할 경우 발생할 수 있는 DB 성능 저하, 백업/복구의 어려움, 비용 문제를 해결하기 위해, 파일을 서버의 특정 물리적 경로에 저장하고 해당 URL/경로만을 DB에 관리하는 전략을 채택했습니다. 이는 시스템의 확장성과 유지보수성을 크게 향상시켰습니다.

안정적인 웹 스트리밍 구현: 웹 환경에서 음악을 끊김 없이 스트리밍하기 위해 파일 경로 문제, Content Security Policy (CSP) 위반, JavaScript 오류 등 다양한 기술적 문제를 해결했습니다. 특히, 서버에 저장된 물리적 파일 경로를 안전하게 웹에서 접근 가능하도록 설정하고, HTTP Content-Type 및 Content-Length 헤더를 정확히 설정하여 안정적인 재생 환경을 구축했습니다.

직관적인 플레이리스트 UX: 사용자가 복잡한 과정 없이 자신만의 음악 컬렉션을 만들고 관리할 수 있도록, 백엔드 데이터 모델링부터 프론트엔드 인터랙션까지 사용자 경험(UX)을 최우선으로 고려하여 직관적인 플레이리스트 기능을 설계했습니다.

3. 고성능 검색 (Apache Solr): 방대한 데이터 속에서 찰나의 발견




(이미지 설명: Solr 기반 음악 검색 결과 화면)

기능 설명:

수많은 음악 콘텐츠 속에서 사용자가 원하는 곡을 찰나의 순간에 찾아내기 위해 Apache Solr (버전 9.8.1)를 직접 설치하고 구성했습니다. 데이터베이스의 모든 음악 정보(제목, 아티스트, 가사 등)를 Solr의 인덱스에 **실시간으로 색인(indexing)**하여 DB 탐색의 비효율을 제거하고 초고속 검색을 가능하게 했습니다. 검색 결과에는 음악의 가사, 앨범 이미지 등 관련 정보를 함께 표시하여 풍부한 사용자 경험을 제공합니다.

[핵심 코드: Solr 문서 인덱싱 처리]
backend/src/main/java/com/boot/service/MusicSolrService.java (가정)

Java

// 음악 엔티티를 Solr 문서로 변환하고 Solr에 인덱싱하는 서비스
@Service
@RequiredArgsConstructor
public class MusicSolrService {
    private final SolrClient solrClient; // Apache Solr 클라이언트

    // 🌟 DB에 음악 저장/수정 시 Solr에 해당 음악 문서 인덱싱
    public void indexMusicDocument(Music music) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", music.getId().toString()); // 고유 ID
        doc.addField("title", music.getTitle());
        doc.addField("artist", music.getArtist());
        doc.addField("album", music.getAlbum());
        doc.addField("lyrics", music.getLyrics()); // 가사 필드 (전문 검색 대상)
        // ... 필요한 필드 추가 (예: album_image_url)

        solrClient.add("music_core", doc); // 'music_core'는 Solr에 생성된 코어 이름
        solrClient.commit("music_core"); // 변경사항 커밋
        log.info("Solr에 음악 문서 인덱싱 완료: {}", music.getTitle());
    }

    // 🌟 Solr를 이용한 음악 검색 쿼리 예시
    public QueryResponse searchMusic(String query, int start, int rows) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query); // 검색어 설정 (Solr는 여러 필드에서 자동으로 검색)
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.set("defType", "edismax"); // 확장된 dismax 파서 사용 (다중 필드 검색에 유리)
        solrQuery.set("qf", "title^2 artist^1.5 lyrics album"); // 검색 필드 및 가중치 설정

        return solrClient.query("music_core", solrQuery);
    }
}
기술적 도전 및 해결:

대용량 데이터의 고성능 검색 구현: 관계형 데이터베이스만으로는 방대한 음악 데이터에 대한 전문 검색(Full-Text Search) 및 실시간 검색 성능에 한계가 있었습니다. 이 문제를 해결하기 위해 Apache Solr를 직접 서버에 설치하고, 데이터베이스의 음악 정보를 Solr에 실시간으로 색인(indexing)하는 시스템을 구축했습니다.

다양한 조건의 검색 최적화: 음악 제목, 아티스트, 가사, 앨범 등 여러 필드에서 동시에 검색이 가능하도록 Solr의 edismax 파서와 필드 가중치(qf)를 설정하여 사용자에게 가장 관련성 높은 검색 결과를 제공하도록 최적화했습니다. 이는 검색 정확도와 사용자 경험을 크게 향상시켰습니다.

확장성 고려: Solr의 유연한 스키마와 코어 관리를 통해, 향후 사용자 플레이리스트 검색, 아티스트 정보 검색 등으로 기능을 확장할 수 있는 기반을 마련했습니다.

4. 커뮤니티 기능: 사용자 간 소통과 공감의 장




(이미지 설명: 커뮤니티 게시판 목록 및 상세 페이지)

기능 설명:

음악 감상 외에도, 사용자들이 자유롭게 의견을 공유하고 상호작용할 수 있는 커뮤니티 게시판 기능을 제공합니다. 게시글의 생성(Create), 조회(Read), 수정(Update), 삭제(Delete)의 완전한 CRUD 기능을 구현했으며, 각 게시글에 대한 댓글 기능과 좋아요/싫어요, 별점 평가를 통해 사용자 간의 심층적인 소통과 활발한 상호작용을 촉진합니다. 이 모든 커뮤니티 화면과 인터랙션은 React를 기반으로 구축되었습니다.

[핵심 코드: 게시글 상호작용 (좋아요/싫어요) 처리]
backend/src/main/java/com/boot/controller/PostController.java (가정)

Java

// 게시글에 대한 좋아요/싫어요 처리 API
@PostMapping("/api/posts/{postId}/likes")
public ResponseEntity<?> toggleLike(@PathVariable Long postId, @AuthenticationPrincipal UserDetails userDetails) {
    try {
        // 🌟 1. 사용자 ID와 게시글 ID를 기반으로 좋아요 상태 토글
        String username = userDetails.getUsername();
        boolean liked = postService.toggleLike(postId, username); // 좋아요/싫어요 로직은 서비스 계층에서 처리
        return ResponseEntity.ok(new ApiResponse(liked ? "좋아요 성공" : "좋아요 취소", true));
    } catch (ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("좋아요 처리 중 오류 발생", false));
    }
}

// 🌟 React 컴포넌트 예시 (좋아요 버튼 클릭 시 API 호출)
// frontend/src/components/community/PostDetail.js (가정)
/*
import React, { useState, useEffect } => {
    const [post, setPost] = useState(null);
    const [isLiked, setIsLiked] = useState(false); // 사용자 좋아요 상태
    const [likeCount, setLikeCount] = useState(0); // 좋아요 수

    useEffect(() => {
        const fetchPost = async () => {
            try {
                const response = await apiClient.get(`/api/posts/${postId}`);
                setPost(response.data.post);
                setIsLiked(response.data.userLiked); // 백엔드에서 사용자 좋아요 여부도 전달 (가정)
                setLikeCount(response.data.likeCount);
            } catch (error) {
                console.error("게시글 로드 실패:", error);
            }
        };
        fetchPost();
    }, [postId]);

    const handleLike = async () => {
        try {
            const response = await apiClient.post(`/api/posts/${postId}/likes`);
            // 🌟 긍정적 UI 업데이트 (Optimistic UI Update) - 빠르게 사용자에게 반응
            setIsLiked(!isLiked);
            setLikeCount(prevCount => isLiked ? prevCount - 1 : prevCount + 1);
            console.log(response.data.message);
        } catch (error) {
            console.error("좋아요 처리 실패:", error);
            // 오류 발생 시 UI 상태 롤백 (필요 시)
        }
    };

    if (!post) return <div>로딩 중...</div>;

    return (
        <div>
            <h2>{post.title}</h2>
            <p>{post.content}</p>
            <button onClick={handleLike}>
                {isLiked ? '❤️ 좋아요 취소' : '🤍 좋아요'} ({likeCount})
            </button>
            // ... 댓글, 별점 기능 등
        </div>
    );
};
export default PostDetail;
*/
기술적 도전 및 해결:

동적인 데이터 변화 및 상호작용 처리: 게시글의 좋아요 수, 댓글 목록 등이 실시간으로 업데이트되어야 하는 동적인 환경에서, React의 효율적인 상태 관리와 컴포넌트 라이프사이클을 활용하여 데이터 변경에 따른 UI 업데이트를 최적화했습니다. 특히, 'Optimistic UI Update' 패턴을 일부 적용하여 사용자 클릭 시 UI가 즉각적으로 반응하도록 설계함으로써, 백엔드 응답을 기다리지 않고도 빠른 사용자 경험을 제공하고자 노력했습니다.

데이터 무결성 보장: 여러 사용자가 동시에 좋아요/싫어요를 누르거나 댓글을 달았을 때, 백엔드에서 동시성 제어(Concurrency Control) 및 트랜잭션 관리를 통해 데이터의 정확성과 무결성을 보장했습니다. 이를 통해 잘못된 카운트나 누락 없이 모든 상호작용이 정확하게 반영되도록 했습니다.

📚 부가 기능 및 시스템 관리: 서비스 운영의 효율성과 확장성을 위한 고려
공지사항 기능 (관리자 전용): 서비스 운영의 투명성과 효율성을 위해 **관리자 권한을 가진 사용자만이 공지사항을 생성(Create), 조회(Read), 수정(Update), 삭제(Delete)**할 수 있는 완전한 CRUD 기능을 구현했습니다. Spring Security의 URL 기반 인가를 통해 관리자 접근을 철저히 제한했습니다.

개발 편의 기능: 개발 및 시연 효율성을 높이는 자동 로그인 테스트 계정을 제공하여 반복적인 로그인 작업을 줄였습니다. 또한, 사용자의 로그인 기록, 음악 청취 기록 등 주요 활동 내역을 저장하여 서비스 모니터링 및 사용자 행동 분석을 위한 데이터 기반을 마련했습니다.

👨‍💻 나의 역할
전체 프로젝트 기획 및 백엔드 설계

사용자 인증 시스템 (JWT, OAuth2) 구축

비밀번호 재설정 이메일 처리 로직 개발

음악 업로드/재생 및 스트리밍 시스템 구현

Apache Solr 연동 및 검색 API 구축

플레이리스트 및 커뮤니티 기능 개발

GitHub 버전 관리 및 README 문서 작성

💭 프로젝트 회고

ListenIt 프로젝트를 통해 풀스택 개발의 흐름을 직접 주도하며

React 프론트엔드와 Spring Boot 백엔드 간의 완전한 통신 구조를 설계하고 보안/검색/계정관리까지 전반적인 기능을 안정적으로 구현했습니다.


특히 OAuth2 로그인, JWT 인증, Solr 검색 엔진 연동, 이메일 기반 비밀번호 찾기 등의 실무 중심 기술을 직접 다뤄보며

서비스 기획력과 시스템 설계 능력을 동시에 성장시킬 수 있었습니다.


  ⓒ 2025 ListenIt Project by 박 성 훈  |  Powered by Java ☕ + Spring 🌿 + React ⚛️

  “당신의 음악, 당신의 방식으로.”            
