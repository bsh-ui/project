<h1 align="center" style="font-size: 3em;">🎵 ListenIt MAGAZINE</h1>
<h3 align="center"><em>Vol.1 - The Sound of Tomorrow: 당신의 음악, 당신의 방식으로.</em></h3>

<p align="center">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/ListenIt%20Cover.png" alt="ListenIt Cover" width="300px" height="450px" >
</p>

<p align="center">
<strong>💡 개인화된 음악 경험과 견고한 보안을 제공하는 React + Spring Boot 기반 풀스택 음악 플랫폼</strong><br>
<strong>개발 기간:</strong> 2025.06.02 ~ 2025.06.10 &nbsp;|&nbsp;
<strong>Repository:</strong> <a href="https://github.com/bsh-ui/project">GitHub 링크</a>
<!-- 🔗 배포 링크: <a href="[배포된_서비스_URL_입력]">Live Demo 바로가기</a> (서비스 배포 시 추가) -->
</p>

🌟 Quick Summary for Reviewers (📌 핵심 요약)
구분

내용

🎯 목표

개인화된 음악 스트리밍 + 검색 플랫폼 구현

🛠 기술 스택

Java 17, Spring Boot, React 18, MySQL, Apache Solr, JWT, OAuth2

🔐 인증

폼 로그인 + 소셜 로그인(Google, Naver, Kakao), JWT, 계정 잠금

🎵 기능

음악 업로드/재생, 플레이리스트 관리, Solr 검색

💬 커뮤니티

게시판/댓글/좋아요/평점

📩 보안기능

이메일 기반 비밀번호 재설정 기능

🧪 개발 편의

테스트 계정, 활동 기록 저장 기능

💡 프로젝트 개요: 음악 서비스에 대한 풀스택 구현과정에서 얻은 학습 경험
<p align="justify">
ListenIt 프로젝트는 6개월간의 개발 교육 과정 중, 음악 스트리밍 서비스에 대한 흥미와 '직접 구현해 볼 수 있을까?' 하는 궁금증에서 시작되었습니다. 평소 즐겨 사용하던 '지니 뮤직'과 같은 플랫폼을 접하며, 서비스를 직접 설계하고 구현해보고자 하는 목표를 세웠습니다.<br><br>

단순히 기능을 나열하기보다, 학습했던 다양한 기술 스택을 실제 프로젝트에 적용해보는 도전을 하고 싶었습니다. 초기에는 백엔드 중심으로 JPA, OAuth2, JWT 인증 시스템 등을 구현하며 기능을 확장해나갔습니다. 프로젝트 중반, React를 접하면서 사용자 경험 측면에서 더 나은 선택이라고 판단했고, 프론트엔드를 React, 백엔드를 Spring Boot(STS)로 분리하여 연동하는 아키텍처를 시도했습니다. 이에 따라 메인 화면과 로그인 기능을 포함한 프론트엔드 부분을 React로 다시 구현하며, 백엔드와 RESTful API로 유기적으로 연동하는 과정을 통해 분산 시스템 설계의 실제적인 경험을 얻을 수 있었습니다.<br><br>

Apache Solr의 도입은 서비스의 핵심 기능을 고도화하기 위한 선택이었습니다. 마침 교육 과정에서 Solr를 이용한 색인 및 검색 구현을 다루었을 때, 이를 개인 프로젝트에 적용해 보면 좋겠다고 생각했습니다. Solr가 다양한 파일 타입의 메타데이터를 효율적으로 색인하고 검색할 수 있다는 점에 착안하여, 방대한 음악 데이터(가사, 아티스트, 앨범, 노래)를 빠르고 정확하게 검색하는 기능을 구현했습니다. 나아가 Solr의 확장성을 고려하여, 사용자나 아티스트 검색 시 관련 앨범, 노래, 플레이리스트까지 불러올 수 있는 가능성 또한 구상해보았습니다.<br><br>

ListenIt은 비교적 짧은 기간 안에 개인이 주도적으로 기획하고 구현한 풀스택 프로젝트입니다. 이 과정을 통해 저는 새로운 기술을 학습하고 실제 프로젝트에 적용하는 주도성, 그리고 문제 해결을 위해 끊임없이 고민하고 시도하는 개발자로서의 태도를 다질 수 있었습니다.

</p>

🛠 기술 스택
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

🚀 핵심 구현 기능: 프로젝트를 통해 만난 기술적 도전과 해결의 여정
<p align="justify">
ListenIt 프로젝트는 사용자에게 몰입감 있는 음악 경험을 제공하기 위해 다양한 기술적 도전을 거쳤습니다. 먼저, 견고한 사용자 인증 및 계정 관리 시스템을 구축하는 것에서 시작했습니다. 사용자 편의를 위해 아이디/비밀번호 기반의 폼 로그인과 Google, Kakao, Naver 소셜 로그인을 모두 통합하는 한편, 무단 접근 방지를 위해 비밀번호 5회 실패 시 자동으로 계정이 잠기는 보안 기능을 강화했습니다. 모든 인증 과정은 서버 부담을 줄이고 확장성을 확보하는 JWT 토큰 기반 인증으로 구현되었으며, 비밀번호를 잊은 사용자를 위한 이메일 인증 기반의 재설정 기능 또한 안전하게 구현했습니다. 이 과정에서 폼, 소셜 로그인, JWT를 하나의 Spring Security 설정에 통합하는 복잡한 필터 체인 구성, HttpOnly 쿠키를 이용한 토큰 관리 보안 강화, 그리고 이메일 인증 코드의 '일회성'과 '무작위성' 보장 등 세밀한 기술적 고민과 해결이 뒤따랐습니다.
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EB%A1%9C%EA%B7%B8%EC%9D%B8.png" width="450">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%B4%EB%A9%94%EC%9D%BC.png" width="450">
<br>
<em>(이미지 설명: 통합 로그인 화면 및 비밀번호 재설정 이메일 인증 화면)</em>
</p>

[핵심 코드: JWT 토큰 발행 및 계정 보안 처리]
backend/src/main/java/com/boot/oauth2/CustomFormSuccessHandler.java

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
    userService.handleSuccessfulLogin(authentication.getName()); 
    // 🌟 4. Access Token 및 성공 메시지를 JSON 형태로 클라이언트에 전송 (React에서 처리)
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    PrintWriter writer = response.getWriter();
    writer.write(String.format("{\"message\": \"로그인 성공\", \"accessToken\": \"%s\"}", accessToken));
    writer.flush();
}

기술적 도전 및 해결:

다중 인증 방식 통합: 폼 로그인, 소셜 로그인, JWT 기반 인증을 하나의 Spring Security 설정에 유기적으로 통합하는 것이 가장 큰 난관이었습니다. CustomAuthenticationSuccessHandler 및 JwtAuthenticationFilter의 정교한 설정을 통해 이 문제를 해결하고 확장성 높은 인증 아키텍처를 구축했습니다.

JWT 토큰 관리 보안: 클라이언트와 백엔드 간 JWT 토큰을 안전하게 주고받기 위해, HttpOnly 쿠키에 Refresh Token을 저장하고 Access Token은 JSON 응답으로 전달하는 방식을 채택했습니다. 이를 통해 XSS 공격으로부터 Refresh Token을 보호하고, 사용자 경험을 해치지 않으면서도 보안성을 강화했습니다.

계정 잠금 및 해제 로직 구현: 로그인 실패 횟수를 DB에 기록하고 특정 횟수(5회) 초과 시 계정을 잠그는 로직을 구현했으며, 로그인 성공 시 자동으로 잠금 해제 및 실패 횟수를 초기화하여 보안과 사용자 편의성을 동시에 잡았습니다.

보안에 민감한 비밀번호 재설정 처리: 비밀번호 재설정 과정은 무단 계정 탈취의 위험이 있어 보안에 매우 민감합니다. 사용자 이메일로 발송되는 인증 코드의 '일회성', '유효 시간 제한', 그리고 '무작위성'을 철저히 보장하는 로직을 구현하여 무단 접근 시도를 원천적으로 차단했습니다.

<p align="justify">
다음으로, 사용자가 음악을 자유롭게 즐길 수 있도록 대용량 음악 콘텐츠의 효율적인 관리 및 안정적인 스트리밍 재생 시스템을 구축했습니다. 관리자가 MP3 파일을 업로드할 때, DB 부하를 줄이기 위해 파일을 서버의 물리 경로에 저장하고 URL만 DB에서 관리하는 전략을 채택했습니다. 웹 환경에서 끊김 없는 스트리밍을 제공하기 위해 파일 경로 문제, CSP 위반, JavaScript 오류 등을 해결하며 안정적인 재생 환경을 구축했으며, 사용자들은 각 음악 상세 페이지에서 자신만의 플레이리스트를 생성하고 관리할 수 있습니다.
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EC%9D%8C%EC%95%85%EC%83%81%EC%84%B8.jpg" width="500">
<br>
<em>(이미지 설명: 음악 상세 페이지 및 플레이리스트 추가 기능)</em>
</p>

[핵심 코드: 효율적인 음악 파일 저장 전략 및 스트리밍 처리]
backend/src/main/java/com/boot/service/MusicService.java (가정, 업로드/재생 관련)

// 관리자용 MP3 파일 업로드 및 메타데이터 저장 로직 (관리자 전용 기능)
@Transactional
public Music uploadMusic(MultipartFile mp3File, MusicMetadataDTO metadata) throws IOException {
    // 🌟 1. 대용량 파일을 DB에 직접 저장하는 대신 서버 물리 경로에 저장
    String uploadDir = "/uploads/music/"; 
    String uniqueFileName = UUID.randomUUID().toString() + "-" + mp3File.getOriginalFilename();
    Path filePath = Paths.get(uploadDir, uniqueFileName);
    Files.copy(mp3File.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // 🌟 2. 물리적 파일의 URL/경로 정보만 DB에 저장하여 DB 부하 경감
    Music music = Music.builder().title(metadata.getTitle()).artist(metadata.getArtist()).album(metadata.getAlbum()).filePath(filePath.toString()).fileUrl("/api/music/stream/" + music.getId()).build();
    return musicRepository.save(music);
}

// 음악 스트리밍 응답 (HttpHeaders.CONTENT_RANGE, Resource 등 활용)
public ResponseEntity<Resource> streamMusic(Long musicId) {
    Music music = musicRepository.findById(musicId).orElseThrow(() -> new MusicNotFoundException("음악을 찾을 수 없습니다."));
    Resource resource = new FileSystemResource(music.getFilePath()); 
    // 🌟 3. Content-Type 및 Content-Length 설정으로 안정적인 스트리밍 제공
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg");
    headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
    return ResponseEntity.ok().headers(headers).body(resource);
}

기술적 도전 및 해결:

대용량 파일의 효율적 관리: MP3와 같은 대용량 미디어 파일을 데이터베이스에 직접 저장할 경우 발생할 수 있는 DB 성능 저하, 백업/복구의 어려움, 비용 문제를 해결하기 위해, 파일을 서버의 특정 물리적 경로에 저장하고 해당 URL/경로만을 DB에 관리하는 전략을 채택했습니다. 이는 시스템의 확장성과 유지보수성을 크게 향상시켰습니다.

안정적인 웹 스트리밍 구현: 웹 환경에서 음악을 끊김 없이 스트리밍하기 위해 파일 경로 문제, Content Security Policy (CSP) 위반, JavaScript 오류 등 다양한 기술적 문제를 해결했습니다. 특히, 서버에 저장된 물리적 파일 경로를 안전하게 웹에서 접근 가능하도록 설정하고, HTTP Content-Type 및 Content-Length 헤더를 정확히 설정하여 안정적인 재생 환경을 구축했습니다.

직관적인 플레이리스트 UX: 사용자가 복잡한 과정 없이 자신만의 음악 컬렉션을 만들고 관리할 수 있도록, 백엔드 데이터 모델링부터 프론트엔드 인터랙션까지 사용자 경험(UX)을 최우선으로 고려하여 직관적인 플레이리스트 기능을 설계했습니다.

<p align="justify">
이와 함께, 방대한 음악 콘텐츠 속에서 사용자가 원하는 곡을 찰나에 찾아낼 수 있도록 Apache Solr 기반의 고성능 검색 기능을 구현했습니다. DB의 음악 정보를 Solr에 실시간으로 색인하여 비효율적인 DB 탐색 없이 초고속 전문 검색을 가능하게 했습니다. 음악 제목, 아티스트, 가사 등 다양한 필드에서 동시에 검색이 가능하도록 최적화했으며, 검색 결과에 관련 이미지와 정보들을 함께 표시하여 사용자의 검색 경험을 풍부하게 만들었습니다.
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%80%EC%83%89.png" width="500">
<br>
<em>(이미지 설명: Solr 기반 음악 검색 결과 화면)</em>
</p>

[핵심 코드: Solr 문서 인덱싱 처리]
backend/src/main/java/com/boot/service/MusicSolrService.java (가정)

// 음악 엔티티를 Solr 문서로 변환하고 Solr에 인덱싱하는 서비스
@Service
@RequiredArgsConstructor
public class MusicSolrService {
    private final SolrClient solrClient;

    // 🌟 DB에 음악 저장/수정 시 Solr에 해당 음악 문서 인덱싱
    public void indexMusicDocument(Music music) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", music.getId().toString());
        doc.addField("title", music.getTitle());
        // ... (아티스트, 앨범, 가사 필드 추가) ...
        solrClient.add("music_core", doc);
        solrClient.commit("music_core");
    }

    // 🌟 Solr를 이용한 음악 검색 쿼리 예시
    public QueryResponse searchMusic(String query, int start, int rows) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.set("defType", "edismax"); // 확장된 dismax 파서 사용
        solrQuery.set("qf", "title^2 artist^1.5 lyrics album"); // 검색 필드 및 가중치 설정
        return solrClient.query("music_core", solrQuery);
    }
}

기술적 도전 및 해결:

대용량 데이터의 고성능 검색 구현: 관계형 데이터베이스만으로는 방대한 음악 데이터에 대한 전문 검색(Full-Text Search) 및 실시간 검색 성능에 한계가 있었습니다. 이 문제를 해결하기 위해 Apache Solr를 직접 서버에 설치하고, 데이터베이스의 음악 정보를 Solr에 실시간으로 색인(indexing)하는 시스템을 구축했습니다.

다양한 조건의 검색 최적화: 음악 제목, 아티스트, 가사, 앨범 등 여러 필드에서 동시에 검색이 가능하도록 Solr의 edismax 파서와 필드 가중치(qf)를 설정하여 사용자에게 가장 관련성 높은 검색 결과를 제공하도록 최적화했습니다. 이는 검색 정확도와 사용자 경험을 크게 향상시켰습니다.

확장성 고려: Solr의 유연한 스키마와 코어 관리를 통해, 향후 사용자 플레이리스트 검색, 아티스트 정보 검색 등으로 기능을 확장할 수 있는 기반을 마련했습니다.

<p align="justify">
마지막으로, 음악 감상 외에 사용자 간 소통을 위해 커뮤니티 기능을 제공합니다. 게시글의 생성, 조회, 수정, 삭제의 완전한 CRUD 기능은 물론, 댓글, 좋아요/싫어요, 별점 평가 등 다양한 상호작용을 지원합니다. 이 모든 커뮤니티 화면과 동적인 인터랙션은 React를 기반으로 구축되어, 게시글 목록과 댓글 등이 실시간으로 업데이트되고 효율적으로 렌더링되도록 최적화했습니다. 특히 'Optimistic UI Update' 패턴을 일부 적용하여 사용자 클릭 시 UI가 즉각적으로 반응하도록 설계하여 끊김 없는 사용자 경험을 제공하고자 노력했습니다.
</p>

<p align="center">
<img src="https://raw.githubusercontent.com/bsh-ui/project/Listenlt/images/%EA%B2%8C%EC%8B%9C%ED%8C%90.png" width="500">
<br>
<em>(이미지 설명: 커뮤니티 게시판 목록 및 상세 페이지)</em>
</p>

[핵심 코드: 게시글 상호작용 (좋아요/싫어요) 처리]
backend/src/main/java/com/boot/controller/PostController.java (가정)

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
// ... (React 컴포넌트 예시는 주석 처리됨, 실제 구현 시 필요) ...


👨‍💻 나의 역할
전체 프로젝트 기획 및 백엔드 설계

사용자 인증 시스템 (JWT, OAuth2) 구축

비밀번호 재설정 이메일 처리 로직 개발

음악 업로드/재생 및 스트리밍 시스템 구현

Apache Solr 연동 및 검색 API 구축

플레이리스트 및 커뮤니티 기능 개발

GitHub 버전 관리 및 README 문서 작성

💭 프로젝트 회고
<p align="justify">
ListenIt 프로젝트를 통해 풀스택 개발의 흐름을 직접 주도하며<br>
React 프론트엔드와 Spring Boot 백엔드 간의 완전한 통신 구조를 설계하고 보안/검색/계정관리까지 전반적인 기능을 안정적으로 구현했습니다.<br><br>
특히 OAuth2 로그인, JWT 인증, Solr 검색 엔진 연동, 이메일 기반 비밀번호 찾기 등의 실무 중심 기술을 직접 다뤄보며<br>
서비스 기획력과 시스템 설계 능력을 동시에 성장시킬 수 있었습니다.
</p>

<p align="center">
  ⓒ 2025 ListenIt Project by <strong>박 성 훈</strong> &nbsp;|&nbsp; Powered by Java ☕ + Spring 🌿 + React ⚛️<br>
  <em>“당신의 음악, 당신의 방식으로.”</em>
</p>
