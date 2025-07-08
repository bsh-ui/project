package com.boot.service;

import com.boot.domain.Music;
import com.boot.domain.MusicDocument;
import com.boot.domain.User;
import com.boot.dto.MusicDTO;
import com.boot.exception.MyFileNotFoundException;
import com.boot.repository.MusicRepository;
import com.boot.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
<<<<<<< HEAD
import java.nio.file.StandardCopyOption; // 이 부분은 FileStorageService에서 처리하는 것이 일반적
=======
import java.nio.file.StandardCopyOption;
>>>>>>> main
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicService {

    private final MusicRepository musicRepository;
    private final FileStorageService fileStorageService; // FileStorageService를 통해 파일 저장/읽기/삭제
    private final UserRepository userRepository;
    private final MusicSolrService musicSolrService;

    @Transactional
    public MusicDTO uploadMusic(String title, String artist, String album, String lyricsContent, // lyrics는 이제 실제 가사 내용
                                String genre, Integer releaseYear, String category, String tags,
                                MultipartFile musicFile, Long uploaderId, MultipartFile coverImageFile) { // coverImageFile 추가
        log.info("MusicService - uploadMusic 호출: 제목={}, 아티스트={}, 앨범={}, UploaderId={}", title, artist, album, uploaderId);

        String musicFilePath = null; // DB에 저장될 음악 파일의 경로
        String coverImagePath = null; // DB에 저장될 커버 이미지의 경로
        String lyricsDbPath = null; // DB에 저장될 가사 파일의 경로
        Long duration = 0L;
        String uploaderNickname = null;

        try {
            // 1. 음악 파일 저장
            if (musicFile != null && !musicFile.isEmpty()) {
                musicFilePath = fileStorageService.storeFile(musicFile, "music");
                log.info("음악 파일 저장 완료. filePath (DB 저장될 경로): {}", musicFilePath);
            }

            // 2. 커버 이미지 파일 저장
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                coverImagePath = fileStorageService.storeFile(coverImageFile, "cover-image");
                log.info("커버 이미지 파일 저장 완료. coverImagePath (DB 저장될 경로): {}", coverImagePath);
            }

            // ⭐ 3. 가사 텍스트를 파일로 저장하고, 그 파일 경로를 DB에 저장 ⭐
            if (lyricsContent != null && !lyricsContent.trim().isEmpty()) {
                lyricsDbPath = fileStorageService.storeLyricsFile(lyricsContent); // 가사 내용을 파일로 저장하고 경로(URL) 반환
                log.info("가사 파일 저장 완료. lyricsDbPath (DB 저장될 경로): {}", lyricsDbPath);
            }

            // 4. uploaderId를 사용하여 닉네임 조회
            if (uploaderId != null) {
                Optional<User> userOptional = userRepository.findById(uploaderId);
                if (userOptional.isPresent()) {
                    uploaderNickname = userOptional.get().getNickname();
                } else {
                    log.warn("Uploader with ID {} not found. Uploader nickname will be null.", uploaderId);
                }
            }

            // 5. 음악 길이 추출
            if (musicFile != null && !musicFile.isEmpty()) {
                Path tempFile = null;
                try {
                    // MultipartFile을 임시 파일로 변환하여 jaudiotagger에 전달
                    tempFile = Files.createTempFile("temp_music_", ".mp3");
                    Files.copy(musicFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

                    AudioFile audioFile = AudioFileIO.read(tempFile.toFile());
                    duration = (long) audioFile.getAudioHeader().getTrackLength();
                    log.info("음악 길이 추출 성공: {}초", duration);
                } catch (Exception e) {
                    log.warn("음악 길이 추출 실패 (파일: {}): {}", musicFile.getOriginalFilename(), e.getMessage());
                } finally {
                    if (tempFile != null) {
                        try {
                            Files.deleteIfExists(tempFile);
                            log.debug("임시 파일 삭제 완료: {}", tempFile.getFileName());
                        } catch (IOException e) {
                            log.error("임시 파일 삭제 실패: {}", e.getMessage());
                        }
                    }
                }
            }

            // Music 엔티티 생성 및 DB 저장
            Music music = Music.builder()
                    .title(title)
                    .artist(artist)
                    .album(album)
                    .lyrics(lyricsDbPath) // ⭐ DB에는 가사 파일 경로(URL) 저장 ⭐
                    .genre(genre)
                    .releaseYear(releaseYear)
                    .category(category)
                    .tags(tags)
                    .filePath(musicFilePath)
                    .fileName(musicFile.getOriginalFilename()) // 원본 파일명 저장
                    .duration(duration)
                    .uploaderId(uploaderId)
                    .uploaderNickname(uploaderNickname)
                    .playCount(0L)
                    .uploadDate(LocalDateTime.now())
                    .coverImagePath(coverImagePath)
                    .build();

            Music savedMusic = musicRepository.save(music);
            log.info("Music 엔티티 DB 저장 완료: ID={}, 제목={}", savedMusic.getId(), savedMusic.getTitle());

            // ⭐ Solr에 색인할 MusicDocument 생성 및 색인 (실제 가사 내용 전달) ⭐
            MusicDocument musicDocument = createMusicDocumentForSolrIndexing(savedMusic, lyricsContent);
            boolean solrIndexed = musicSolrService.indexMusicDocument(musicDocument);
            if (!solrIndexed) {
                log.error("Failed to index music document in Solr for music ID: {}", savedMusic.getId());
            }

            // DTO 반환 (실제 가사 내용을 포함하여 반환)
            return convertToDtoWithLyricsContent(savedMusic, lyricsContent);

        } catch (Exception e) {
            log.error("음악 업로드 및 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("음악 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public MusicDTO getMusicById(Long id) {
        log.info("MusicService - getMusicById 호출: ID={}", id);
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new MyFileNotFoundException("Music not found with id " + id));

        // ⭐ 가사 파일 경로에서 실제 가사 내용을 읽어와 DTO에 설정 ⭐
        String lyricsContent = null;
        if (music.getLyrics() != null && !music.getLyrics().trim().isEmpty()) {
<<<<<<< HEAD
            try {
                lyricsContent = fileStorageService.readLyricsFile(music.getLyrics());
                log.info("가사 파일 읽기 성공: {}", music.getLyrics());
            } catch (IOException e) {
                log.error("가사 파일 읽기 실패: {} - {}", music.getLyrics(), e.getMessage());
                lyricsContent = null; // 파일 읽기 실패 시 null 또는 빈 문자열로 처리
=======
            lyricsContent = fileStorageService.readLyricsFile(music.getLyrics());
            // readLyricsFile이 IOException을 던지지 않고 null을 반환하므로 try-catch 제거
            if (lyricsContent == null) {
                log.warn("getMusicById: 가사 파일 읽기 실패 또는 파일 없음: {}", music.getLyrics());
                lyricsContent = "[가사를 불러올 수 없습니다]"; // 또는 다른 기본값 설정
>>>>>>> main
            }
        }
        List<String> artistList = Optional.ofNullable(music.getArtist())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>()); // 빈 리스트 반환

        List<String> genreList = Optional.ofNullable(music.getGenre())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());

        List<String> categoryList = Optional.ofNullable(music.getCategory())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());

        List<String> tagsList = Optional.ofNullable(music.getTags())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.asList(s.split(",\\s*")))
                .orElse(new ArrayList<>());



        return MusicDTO.builder()
                .id(music.getId())
                .title(music.getTitle())
                .artist(artistList)
                .album(music.getAlbum())
                .genre(genreList)
                .musicUrl("/api/music/stream/" + music.getId())
                .coverImagePath(music.getCoverImagePath())
                .fileName(music.getFileName())
                .duration(music.getDuration())
                .uploadDate(music.getUploadDate())
                .uploaderId(music.getUploaderId())
                .uploaderNickname(music.getUploaderNickname())
                .playCount(music.getPlayCount())
                .lyrics(lyricsContent) // ⭐ 추가: lyrics 필드 설정 ⭐
                .releaseYear(music.getReleaseYear()) // ⭐ 추가: releaseYear 필드 설정 ⭐
                .category(categoryList) // ⭐ 추가: category 필드 설정 ⭐
                .tags(tagsList)     // ⭐ 추가: tags 필드 설정 ⭐
                .build();
    }
  

    @Transactional(readOnly = true)
    public Page<MusicDTO> getAllMusic(Pageable pageable) {
        log.info("MusicService - getAllMusic 호출 (페이지네이션)");
        return musicRepository.findAll(pageable)
                .map(music -> {
                    String lyricsContent = null;
                    if (music.getLyrics() != null && !music.getLyrics().trim().isEmpty()) {
<<<<<<< HEAD
                        try {
                            // ⭐ 파일 경로에서 실제 가사 내용을 읽어옴 ⭐
                            lyricsContent = fileStorageService.readLyricsFile(music.getLyrics());
                        } catch (IOException e) {
                            log.error("getAllMusic: 가사 파일 읽기 실패 for music ID {}: {}", music.getId(), e.getMessage());
=======
                        // FileStorageService.readLyricsFile은 이제 IOException을 던지지 않습니다.
                        lyricsContent = fileStorageService.readLyricsFile(music.getLyrics());
                        if (lyricsContent == null) {
                            log.warn("getAllMusic: 가사 파일 읽기 실패 또는 파일 없음 for music ID {}: {}", music.getId(), music.getLyrics());
>>>>>>> main
                            lyricsContent = "[가사를 불러올 수 없습니다]"; // 또는 null, 빈 문자열로 처리
                        }
                    }
                    // ⭐ convertToDtoWithLyricsContent 헬퍼 메서드를 사용하여 DTO 반환 ⭐
                    return convertToDtoWithLyricsContent(music, lyricsContent);
                });
    }
<<<<<<< HEAD
//    public Page<MusicDTO> getAllMusic(Pageable pageable) {
//        log.info("MusicService - getAllMusic 호출 (페이지네이션)");
//        // getAllMusic에서는 목록 조회를 위해 실제 가사 내용 대신 lyrics 필드(URL)를 포함한 DTO를 반환
//        return musicRepository.findAll(pageable)
//                .map(music -> MusicDTO.fromEntity(music)); // MusicDTO.fromEntity는 lyrics 필드에 URL을 매핑함
//    }
=======
>>>>>>> main

    @Transactional(readOnly = true)
    public Resource loadMusicFileAsResource(Long musicId) {
        log.info("MusicService - loadMusicFileAsResource 호출: Music ID={}", musicId);
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> {
                    log.error("Music not found with id: {}", musicId);
                    return new MyFileNotFoundException("Music not found with id " + musicId);
                });
        log.info("DB에서 조회된 파일 경로 (filePath): {}", music.getFilePath());
        return fileStorageService.loadFileAsResource(music.getFilePath());
    }

    public byte[] getMusicFile(Long id) throws IOException {
        Optional<Music> musicOptional = musicRepository.findById(id);
        if (musicOptional.isEmpty()) {
            throw new RuntimeException("Music not found with id: " + id);
        }
        Path filePath = Paths.get(musicOptional.get().getFilePath());
        return Files.readAllBytes(filePath);
    }

    @Transactional
    public void deleteMusic(Long id) { // IOException 제거 (FileStorageService에서 처리)
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Music not found with id: " + id));

        musicRepository.delete(music);
        musicSolrService.deleteMusicDocument(id.toString()); // Solr에서도 삭제
    }

    @Transactional
    public MusicDTO updateMusic(Long id, MusicDTO musicDto, MultipartFile musicFile, MultipartFile coverImageFile) {
        Music existingMusic = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Music not found with id: " + id));

        String updatedMusicFilePath = existingMusic.getFilePath();
        String updatedCoverImagePath = existingMusic.getCoverImagePath();
        String updatedLyricsDbPath = existingMusic.getLyrics(); // DB에 저장될 최종 가사 파일 경로
        String lyricsContentForSolr = null; // Solr에 색인할 실제 가사 내용

<<<<<<< HEAD
        try {
=======
        try { // 이 try-catch는 storeLyricsFile 등에서 발생할 수 있는 IOException을 위해 유지됩니다.
>>>>>>> main
            // 1. 음악 파일 업데이트
            if (musicFile != null && !musicFile.isEmpty()) {
                if (existingMusic.getFilePath() != null) {
                    fileStorageService.deleteFile(existingMusic.getFilePath()); // 기존 파일 삭제
                }
                updatedMusicFilePath = fileStorageService.storeFile(musicFile, "music");
                log.info("음악 파일 업데이트 완료. newPath: {}", updatedMusicFilePath);
            }

            // 2. 커버 이미지 업데이트
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                if (existingMusic.getCoverImagePath() != null) {
                    fileStorageService.deleteFile(existingMusic.getCoverImagePath()); // 기존 파일 삭제
                }
                updatedCoverImagePath = fileStorageService.storeFile(coverImageFile, "cover-image");
                log.info("커버 이미지 파일 업데이트 완료. newPath: {}", updatedCoverImagePath);
            }

            // ⭐ 3. 가사 내용 업데이트 (새 가사가 제공되면 파일 갱신) ⭐
            if (musicDto.getLyrics() != null && !musicDto.getLyrics().trim().isEmpty()) {
                // 새 가사 내용이 제공되었을 때: 기존 파일 삭제 후 새 파일 저장
                if (existingMusic.getLyrics() != null) {
                    fileStorageService.deleteFile(existingMusic.getLyrics());
                }
<<<<<<< HEAD
                updatedLyricsDbPath = fileStorageService.storeLyricsFile(musicDto.getLyrics());
=======
                updatedLyricsDbPath = fileStorageService.storeLyricsFile(musicDto.getLyrics()); // storeLyricsFile은 IOException을 던집니다.
>>>>>>> main
                lyricsContentForSolr = musicDto.getLyrics(); // Solr에 색인할 내용
                log.info("가사 파일 업데이트 완료. newPath: {}", updatedLyricsDbPath);
            } else if (existingMusic.getLyrics() != null) {
                // DTO에 가사가 없지만 DB에는 가사가 있을 경우: 기존 가사 파일 내용 읽어와 Solr에 재색인
<<<<<<< HEAD
                // (가사 내용을 지우는 경우가 아니라면, 기존 내용을 다시 색인)
                lyricsContentForSolr = fileStorageService.readLyricsFile(existingMusic.getLyrics());
=======
                lyricsContentForSolr = fileStorageService.readLyricsFile(existingMusic.getLyrics()); // 이 메서드는 IOException을 던지지 않습니다.
                if (lyricsContentForSolr == null) {
                    log.warn("updateMusic: 기존 가사 파일 읽기 실패 또는 파일 없음 for music ID {}: {}", existingMusic.getId(), existingMusic.getLyrics());
                    // 필요하다면 lyricsContentForSolr에 기본값을 설정할 수 있습니다.
                }
>>>>>>> main
            }
            // else: DTO에도 가사가 없고 DB에도 가사가 없으면 lyricsContentForSolr는 null

            // Music 엔티티 정보 업데이트
            existingMusic.updateMusicInfo(
                    musicDto.getTitle(),
                    musicDto.getArtist() != null ? String.join(", ", musicDto.getArtist()) : null,
                    musicDto.getAlbum(),
                    updatedLyricsDbPath, // ⭐ 업데이트된 가사 파일 경로 전달 ⭐
                    musicDto.getGenre() != null ? String.join(", ", musicDto.getGenre()) : null,
                    musicDto.getReleaseYear(),
                    musicDto.getCategory() != null ? String.join(", ", musicDto.getCategory()) : null,
                    musicDto.getTags() != null ? String.join(", ", musicDto.getTags()) : null,
                    musicDto.getDuration()
            );
            existingMusic.setFilePath(updatedMusicFilePath); // 음악 파일 경로 업데이트
            existingMusic.setFileName(musicFile != null ? musicFile.getOriginalFilename() : existingMusic.getFileName()); // 파일명 업데이트
            existingMusic.setCoverImagePath(updatedCoverImagePath); // 커버 이미지 경로 업데이트
            existingMusic.setPlayCount(musicDto.getPlayCount());


            Music updatedMusic = musicRepository.save(existingMusic);

            // ⭐ Solr 문서 업데이트 및 재색인 (실제 가사 내용 전달) ⭐
            MusicDocument musicDocument = createMusicDocumentForSolrIndexing(updatedMusic, lyricsContentForSolr);
            boolean solrUpdated = musicSolrService.indexMusicDocument(musicDocument);
            if (!solrUpdated) {
                log.error("Failed to re-index music document in Solr after update for music ID: {}", updatedMusic.getId());
            }
            return convertToDtoWithLyricsContent(updatedMusic, lyricsContentForSolr); // DTO로 변환하여 반환
<<<<<<< HEAD
        } catch (IOException e) {
            log.error("Music update failed", e);
            throw new RuntimeException("음악 업데이트 실패: " + e.getMessage(), e);
=======
        } catch (IOException e) { // storeLyricsFile이 IOException을 던지므로 이 catch는 여전히 필요합니다.
            log.error("Music update failed", e);
            throw new RuntimeException("음악 업데이트 실패: " + e.getMessage(), e);
        } catch (Exception e) { // 기타 예외 처리
            log.error("음악 업데이트 및 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("음악 업데이트 실패: " + e.getMessage(), e);
>>>>>>> main
        }
    }

    @Transactional(readOnly = true)
    public Page<MusicDTO> searchMusic(String keyword, Pageable pageable) {
        log.info("MusicService - Solr 검색 호출: 키워드='{}', 페이지={}", keyword, pageable.getPageNumber());

        Page<MusicDocument> solrResults = musicSolrService.searchMusicDocuments(keyword, pageable);

        // SolrDocument에서 DTO로 변환 시에는 Solr의 lyrics 필드(내용)를 그대로 사용
        return solrResults.map(doc -> MusicDTO.builder()
                .id(Long.valueOf(doc.getId()))
                .title(doc.getTitle())
                .artist(doc.getArtist())
                .album(doc.getAlbum())
                .lyrics(doc.getLyrics()) // Solr에서 가져온 실제 가사 내용
                .genre(doc.getGenre())
                .releaseYear(doc.getReleaseYear())
                .playCount(doc.getPlayCount())
                .category(doc.getCategory())
                .tags(doc.getTags())
                .musicUrl("/api/music/stream/" + doc.getId())
                .duration(doc.getDuration() != null ? doc.getDuration().longValue() : null)
                .coverImagePath(doc.getCoverImagePath())
                .uploadDate(doc.getUploadDate() != null ? doc.getUploadDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .uploaderId(doc.getUploaderId())
                .uploaderNickname(doc.getUploaderNickname())
                .build());
    }

    // Music 엔티티에서 MusicDTO로 변환하는 헬퍼 메서드 (기존 MusicDTO.fromEntity 재활용)
    // 이 메서드는 Music 엔티티의 lyrics 필드(URL)를 그대로 DTO에 매핑합니다.
    // 목록 조회 등 실제 가사 내용이 필요 없는 경우 사용
    private MusicDTO convertToDto(Music music) {
        return MusicDTO.fromEntity(music);
    }

    // Music 엔티티와 실제 가사 내용을 받아 MusicDTO로 변환하는 헬퍼 메서드
    // 주로 상세 조회나 업로드/업데이트 응답 시 실제 가사 내용을 DTO에 담을 때 사용
    private MusicDTO convertToDtoWithLyricsContent(Music music, String lyricsContent) {
        return MusicDTO.builder()
                .id(music.getId())
                .title(music.getTitle())
                .artist(music.getArtist() != null ? Arrays.asList(music.getArtist().split(",\\s*")) : new ArrayList<>())
                .album(music.getAlbum())
                .lyrics(lyricsContent) // ⭐ 실제 가사 내용 설정 ⭐
                .genre(music.getGenre() != null ? Arrays.asList(music.getGenre().split(",\\s*")) : new ArrayList<>())
                .releaseYear(music.getReleaseYear())
                .category(music.getCategory() != null ? Arrays.asList(music.getCategory().split(",\\s*")) : new ArrayList<>())
                .tags(music.getTags() != null ? Arrays.asList(music.getTags().split(",\\s*")) : new ArrayList<>())
                .musicUrl("/api/music/stream/" + music.getId())
                .coverImagePath(music.getCoverImagePath())
                .fileName(music.getFileName())
                .duration(music.getDuration())
                .uploadDate(music.getUploadDate())
                .uploaderId(music.getUploaderId())
                .uploaderNickname(music.getUploaderNickname())
                .playCount(music.getPlayCount())
                .build();
    }


    // Music 엔티티와 실제 가사 내용을 받아 Solr MusicDocument로 변환하는 헬퍼 메서드
    private MusicDocument createMusicDocumentForSolrIndexing(Music music, String lyricsContentForSolr) {
        // Music 엔티티의 String 필드들을 List<String>으로 변환
        String singleArtist = music.getArtist();
        List<String> artistList = (singleArtist != null && !singleArtist.trim().isEmpty())
                                ? Arrays.asList(singleArtist.split(",\\s*")) : new ArrayList<>();

        List<String> genreList = (music.getGenre() != null && !music.getGenre().trim().isEmpty())
                                ? Arrays.asList(music.getGenre().split(",\\s*")) : new ArrayList<>();
        List<String> categoryList = (music.getCategory() != null && !music.getCategory().trim().isEmpty())
                                  ? Arrays.asList(music.getCategory().split(",\\s*")) : new ArrayList<>();
        List<String> tagsList = (music.getTags() != null && !music.getTags().trim().isEmpty())
                              ? Arrays.asList(music.getTags().split(",\\s*")) : new ArrayList<>();

        // LocalDateTime -> java.util.Date 변환
        Date uploadDateAsUtilDate = music.getUploadDate() != null ? Date.from(music.getUploadDate().atZone(ZoneId.systemDefault()).toInstant()) : null;

        return MusicDocument.builder()
                .id(String.valueOf(music.getId()))
                .title(music.getTitle())
                .artist(artistList)
                .album(music.getAlbum())
                .lyrics(lyricsContentForSolr) // ⭐ Solr 문서에는 실제 가사 내용을 색인 ⭐
                .genre(genreList)
                .releaseYear(music.getReleaseYear())
                .playCount(music.getPlayCount())
                .category(categoryList)
                .tags(tagsList)
                .musicUrl(music.getFilePath())
                .duration(music.getDuration() != null ? music.getDuration().intValue() : null)
                .coverImagePath(music.getCoverImagePath())
                .uploadDate(uploadDateAsUtilDate)
                .createdAt(uploadDateAsUtilDate)
                .uploaderId(music.getUploaderId())
                .uploaderNickname(music.getUploaderNickname())
                .build();
    }

    // MusicDocument에서 MusicDTO로 변환하는 헬퍼 메서드 (searchMusic에서 사용)
    private MusicDTO convertMusicDocumentToDto(MusicDocument doc) {
        Long dtoId = null;
        try {
            if (doc.getId() != null) {
                dtoId = Long.valueOf(doc.getId());
            }
        } catch (NumberFormatException e) {
            log.warn("Solr Document ID '{}' cannot be converted to Long. Setting to null.", doc.getId());
        }

        // Solr의 uploadDate(java.util.Date)를 DTO의 uploadDate(LocalDateTime)로 변환
        java.time.LocalDateTime dtoUploadDate = null;
        if (doc.getUploadDate() != null) {
            dtoUploadDate = doc.getUploadDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return MusicDTO.builder()
                .id(dtoId)
                .title(doc.getTitle())
                .artist(doc.getArtist())
                .album(doc.getAlbum())
                .lyrics(doc.getLyrics()) // Solr에서 가져온 실제 가사 내용
                .genre(doc.getGenre())
                .releaseYear(doc.getReleaseYear())
                .playCount(doc.getPlayCount())
                .category(doc.getCategory())
                .tags(doc.getTags())
                .musicUrl("/api/music/stream/" + doc.getId())
                .duration(doc.getDuration() != null ? doc.getDuration().longValue() : null)
                .coverImagePath(doc.getCoverImagePath())
                .uploadDate(dtoUploadDate)
                .uploaderId(doc.getUploaderId())
                .uploaderNickname(doc.getUploaderNickname())
                .build();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> main
