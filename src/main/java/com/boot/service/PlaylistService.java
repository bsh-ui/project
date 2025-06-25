package com.boot.service;

import com.boot.domain.Music;
import com.boot.domain.Playlist;
import com.boot.domain.PlaylistDocument;
import com.boot.domain.PlaylistMusic;
import com.boot.domain.User;
import com.boot.dto.MusicDTO;
import com.boot.dto.PlaylistDTO;
import com.boot.dto.PlaylistDTO.CreateRequest;
import com.boot.dto.PlaylistDTO.PlaylistResponse;
import com.boot.dto.PlaylistDTO.UpdateRequest;
import com.boot.repository.MusicRepository;
import com.boot.repository.PlaylistMusicRepository;
import com.boot.repository.PlaylistRepository;
import com.boot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections; // Collections 임포트 (사용하지 않을 예정이나 기존 코드에 있었음)
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

	private final PlaylistRepository playlistRepository;
	private final UserRepository userRepository;
	private final PlaylistMusicRepository playlistMusicRepository;
	private final MusicRepository musicRepository;
	private final PlaylistSolrService playlistSolrService;

	/**
	 * 새로운 플레이리스트를 생성합니다.
	 * @param createRequest 플레이리스트 생성 요청 DTO (제목, 설명)
	 * @param userId 플레이리스트를 생성하는 사용자 ID
	 * @return 생성된 플레이리스트의 DTO
	 */
	@Transactional
	public PlaylistResponse createPlaylist(CreateRequest createRequest, Long userId) {
		log.info("Creating playlist: userId={}, title='{}'", userId, createRequest.getTitle());

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

		Playlist playlist = Playlist.builder().title(createRequest.getTitle())
				.description(createRequest.getDescription()).user(user).isPublic(true) // Default to public
				.build();

		Playlist savedPlaylist = playlistRepository.save(playlist);
		log.info("Playlist created: ID={}, title='{}', creator='{}'", savedPlaylist.getId(), savedPlaylist.getTitle(),
				user.getNickname());

		// Solr에 색인할 때 PlaylistDocument 생성 시점에 user.getNickname()을 포함
		boolean solrIndexed = playlistSolrService.indexPlaylistDocument(convertToPlaylistDocument(savedPlaylist));
		if (!solrIndexed) {
			log.warn("Failed to index playlist in Solr for ID: {}", savedPlaylist.getId());
			// 실제 운영에서는 Solr 색인 실패에 대한 추가적인 처리 (예: 재시도 큐) 고려
		}
		return convertToPlaylistResponse(savedPlaylist);
	}

	/**
	 * Playlist 엔티티를 PlaylistResponse DTO로 변환하는 헬퍼 메서드.
	 * 이 메서드는 플레이리스트에 포함된 음악 목록까지 함께 로드하여 DTO에 담습니다.
	 * @param playlist 변환할 Playlist 엔티티
	 * @return 변환된 PlaylistResponse DTO
	 */
	private PlaylistResponse convertToPlaylistResponse(Playlist playlist) {
		List<MusicDTO> musicList = new ArrayList<>();
		// playlist.getPlaylistMusics()가 Lazy Loading일 수 있으므로 null 체크 및 실제 데이터 로드 보장
		// findByIdWithMusics()를 통해 이미 로드되었다고 가정
		if (playlist.getPlaylistMusics() != null) {
			musicList = playlist.getPlaylistMusics().stream()
					// 음악 순서(musicOrder) 기준으로 정렬
					.sorted((pm1, pm2) -> Integer.compare(pm1.getMusicOrder(), pm2.getMusicOrder()))
					.map(pm -> MusicDTO.fromEntity(pm.getMusic())) // ⭐ MusicDTO.fromEntity 재사용 ⭐
					.collect(Collectors.toList());
		}

		return PlaylistResponse.builder().id(playlist.getId()).title(playlist.getTitle())
				.description(playlist.getDescription()).userNickname(playlist.getUser().getNickname())
				.createdAt(playlist.getCreatedAt()).updatedAt(playlist.getUpdatedAt()).isPublic(playlist.getIsPublic())
				.musics(musicList).build();
	}

	/**
	 * PlaylistMusic 엔티티에서 Music 엔티티를 추출하여 MusicDTO로 변환하는 헬퍼 메서드
	 * MusicDTO.fromEntity를 사용하므로 이 메서드는 더 이상 필요하지 않습니다.
	 * 하지만 기존 코드 구조 유지를 위해 주석 처리하고 위에 직접 MusicDTO.fromEntity 호출
	 */
	/*
	private MusicDTO convertToMusicDTO(PlaylistMusic playlistMusic) {
		Music music = playlistMusic.getMusic();
		// MusicDTO.fromEntity 메서드를 재사용하여 Music 엔티티를 MusicDTO로 변환합니다.
		// 이 메서드 내부에서 Music 엔티티의 String 필드를 MusicDTO의 List<String> 필드로 변환하는 로직을 처리합니다.
		// 또한, 모든 필드(lyrics, releaseYear, category, tags, uploaderNickname 등)가 정확히 매핑되도록 보장합니다.
		return MusicDTO.fromEntity(music);
	}
	*/


	/**
	 * 사용자 ID로 플레이리스트 목록을 조회합니다.
	 * @param userId 조회할 사용자의 ID
	 * @return 해당 사용자의 플레이리스트 DTO 목록
	 */
	@Transactional(readOnly = true)
	public List<PlaylistResponse> getPlaylistsByUserId(Long userId) {
		log.info("Fetching playlists for user ID: {}", userId);
		List<Playlist> playlists = playlistRepository.findByUserId(userId);
		log.info("Found {} playlists for user ID: {}", playlists.size(), userId);
		return playlists.stream().map(this::convertToPlaylistResponse).collect(Collectors.toList());
	}

	/**
	 * 모든 공개 플레이리스트를 조회합니다.
	 * @return 모든 공개 플레이리스트 DTO 목록
	 */
	@Transactional(readOnly = true)
	public List<PlaylistResponse> getAllPublicPlaylists() {
		log.info("Fetching all public playlists.");
		List<Playlist> playlists = playlistRepository.findByIsPublicTrue();
		log.info("Found {} public playlists.", playlists.size());
		return playlists.stream().map(this::convertToPlaylistResponse).collect(Collectors.toList());
	}

	/**
	 * 특정 플레이리스트를 상세 조회합니다. 이 메서드는 플레이리스트에 포함된 음악 목록도 함께 로드합니다.
	 * @param playlistId 조회할 플레이리스트 ID
	 * @return 플레이리스트의 상세 DTO
	 */
	@Transactional(readOnly = true)
	public PlaylistResponse getPlaylistById(Long playlistId) {
		log.info("Fetching playlist details for ID: {}", playlistId);
		Playlist playlist = playlistRepository.findByIdWithMusics(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

		log.info("Playlist details fetched successfully for ID: {}, title='{}'", playlistId, playlist.getTitle());
		return convertToPlaylistResponse(playlist);
	}

	/**
	 * 플레이리스트를 수정합니다. (소유자만 가능)
	 * @param playlistId 수정할 플레이리스트 ID
	 * @param updateRequest 플레이리스트 수정 요청 DTO (제목, 설명, 공개 여부)
	 * @param currentUserId 현재 로그인된 사용자 ID
	 * @return 수정된 플레이리스트의 DTO
	 * @throws SecurityException 권한이 없을 경우
	 * @throws IllegalArgumentException 플레이리스트를 찾을 수 없을 경우
	 */
	@Transactional
	public PlaylistResponse updatePlaylist(Long playlistId, UpdateRequest updateRequest, Long currentUserId) {
		log.info("Attempting to update playlist ID: {} by user ID: {}", playlistId, currentUserId);

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

		if (!playlist.getUser().getId().equals(currentUserId)) {
			log.warn("Update playlist ID: {} failed. Permission denied for user ID: {}", playlistId, currentUserId);
			throw new SecurityException("You do not have permission to modify this playlist.");
		}

		playlist.updatePlaylist(updateRequest.getTitle(), updateRequest.getDescription(), updateRequest.getIsPublic());
		playlist.setUpdatedAt(LocalDateTime.now()); // 업데이트 시간 명시적 설정

		Playlist updatedPlaylist = playlistRepository.save(playlist);
		log.info("Playlist ID: {} updated successfully. Title='{}', isPublic={}", updatedPlaylist.getId(),
				updatedPlaylist.getTitle(), updatedPlaylist.getIsPublic());

		// Solr 문서 업데이트 시, 최신 정보(특히 업데이트 시간) 반영
		boolean solrIndexed = playlistSolrService.indexPlaylistDocument(convertToPlaylistDocument(updatedPlaylist));
		if (!solrIndexed) {
			log.warn("Failed to re-index playlist in Solr after update for ID: {}", updatedPlaylist.getId());
		}

		return convertToPlaylistResponse(updatedPlaylist);
	}

	/**
	 * 플레이리스트를 삭제합니다. (소유자만 가능)
	 * @param playlistId 삭제할 플레이리스트 ID
	 * @param currentUserId 현재 로그인된 사용자 ID
	 * @throws SecurityException 권한이 없을 경우
	 * @throws IllegalArgumentException 플레이리스트를 찾을 수 없을 경우
	 */
	@Transactional
	public void deletePlaylist(Long playlistId, Long currentUserId) {
		log.info("Attempting to delete playlist ID: {} by user ID: {}", playlistId, currentUserId);

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

		if (!playlist.getUser().getId().equals(currentUserId)) {
			log.warn("Delete playlist ID: {} failed. Permission denied for user ID: {}", playlistId, currentUserId);
			throw new SecurityException("You do not have permission to delete this playlist.");
		}

		playlistRepository.delete(playlist);
		log.info("Playlist ID: {} deleted successfully.", playlistId);

		boolean solrDeleted = playlistSolrService.deletePlaylistDocument(String.valueOf(playlistId));
		if (!solrDeleted) {
			log.warn("Failed to delete playlist from Solr for ID: {}", playlistId);
		}
	}

	/**
	 * 플레이리스트에 음악을 추가합니다.
	 * @param playlistId 음악을 추가할 플레이리스트 ID
	 * @param musicId 추가할 음악 ID
	 * @param currentUserId 현재 로그인된 사용자 ID
	 * @return 업데이트된 플레이리스트의 DTO (음악 목록 포함)
	 * @throws SecurityException 권한이 없을 경우
	 * @throws IllegalArgumentException 플레이리스트나 음악을 찾을 수 없거나 이미 추가된 경우
	 */
	@Transactional
	public PlaylistResponse addMusicToPlaylist(Long playlistId, Long musicId, Long currentUserId) {
		log.info("Attempting to add music ID: {} to playlist ID: {} by user ID: {}", musicId, playlistId,
				currentUserId);

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

		if (!playlist.getUser().getId().equals(currentUserId)) {
			log.warn("Adding music to playlist ID: {} failed. Permission denied for user ID: {}", playlistId,
					currentUserId);
			throw new SecurityException("You do not have permission to add music to this playlist.");
		}

		Music music = musicRepository.findById(musicId)
				.orElseThrow(() -> new IllegalArgumentException("Music not found with ID: " + musicId));

		Optional<PlaylistMusic> existingPlaylistMusic = playlistMusicRepository.findByPlaylistAndMusic(playlist, music);
		if (existingPlaylistMusic.isPresent()) {
			log.warn("Music ID: {} is already in playlist ID: {}.", musicId, playlistId);
			throw new IllegalArgumentException("This music is already in the playlist.");
		}

		Integer maxOrder = playlistMusicRepository.findMaxMusicOrderByPlaylist(playlist);
		int nextOrder = (maxOrder != null) ? maxOrder + 1 : 0;

		PlaylistMusic playlistMusic = PlaylistMusic.builder().playlist(playlist).music(music).musicOrder(nextOrder)
				.addedAt(LocalDateTime.now())
				.build();

		playlistMusicRepository.save(playlistMusic);
		log.info("Music ID: {} added to playlist ID: {}. PlaylistMusic ID: {}", musicId, playlistId,
				playlistMusic.getId());

		// 음악 추가 후 Solr 문서 업데이트를 위해 Playlist를 다시 로드하여 최신 Music 목록 포함
		Playlist updatedPlaylistForSolr = playlistRepository.findByIdWithMusics(playlistId).orElseThrow(
				() -> new IllegalStateException("Playlist not found after music add for Solr update: " + playlistId));
		boolean solrIndexed = playlistSolrService
				.indexPlaylistDocument(convertToPlaylistDocument(updatedPlaylistForSolr));
		if (!solrIndexed) {
			log.warn("Failed to re-index playlist in Solr after adding music for ID: {}", playlistId);
		}

		// 최종 응답을 위해 업데이트된 플레이리스트를 다시 로드
		return convertToPlaylistResponse(playlistRepository.findByIdWithMusics(playlistId).orElseThrow(
				() -> new IllegalStateException("Playlist not found for final response after music add: " + playlistId)));
	}

	/**
	 * 플레이리스트에서 음악을 삭제합니다.
	 * @param playlistId 음악을 삭제할 플레이리스트 ID
	 * @param musicId 삭제할 음악 ID
	 * @param currentUserId 현재 로그인된 사용자 ID
	 * @throws SecurityException 권한이 없을 경우
	 * @throws IllegalArgumentException 플레이리스트에 해당 음악이 없거나 플레이리스트를 찾을 수 없을 경우
	 */
	@Transactional
	public void removeMusicFromPlaylist(Long playlistId, Long musicId, Long currentUserId) {
		log.info("Attempting to remove music ID: {} from playlist ID: {} by user ID: {}", musicId, playlistId,
				currentUserId);

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found with ID: " + playlistId));

		if (!playlist.getUser().getId().equals(currentUserId)) {
			log.warn("Removing music from playlist ID: {} failed. Permission denied for user ID: {}", playlistId,
					currentUserId);
			throw new SecurityException("You do not have permission to remove music from this playlist.");
		}

		PlaylistMusic playlistMusicToDelete = playlistMusicRepository
				.findByPlaylistAndMusic(playlist, musicRepository.getReferenceById(musicId))
				.orElseThrow(() -> new IllegalArgumentException(
						"Music ID: " + musicId + " is not found in playlist ID: " + playlistId));

		int deletedMusicOrder = playlistMusicToDelete.getMusicOrder();

		playlistMusicRepository.delete(playlistMusicToDelete);
		log.info("Music ID: {} removed from playlist ID: {}. PlaylistMusic ID: {}", musicId, playlistId,
				playlistMusicToDelete.getId());

		playlistMusicRepository.decrementMusicOrderGreaterThan(playlist, deletedMusicOrder);
		log.info("Reordered music in playlist ID: {} after removing music at order {}.", playlistId, deletedMusicOrder);

		// 음악 삭제 후 Solr 문서 업데이트를 위해 Playlist를 다시 로드하여 최신 Music 목록 포함
		Playlist updatedPlaylistForSolr = playlistRepository.findByIdWithMusics(playlistId).orElseThrow(
				() -> new IllegalStateException("Playlist not found after music remove for Solr update: " + playlistId));
		boolean solrIndexed = playlistSolrService.indexPlaylistDocument(convertToPlaylistDocument(updatedPlaylistForSolr));
		if (!solrIndexed) {
			log.warn("Failed to re-index playlist in Solr after removing music for ID: {}", playlistId);
		}
	}

	/**
	 * Playlist 엔티티를 PlaylistDocument (Solr용)로 변환하는 헬퍼 메서드.
	 * @param playlist 변환할 Playlist 엔티티
	 * @return 변환된 PlaylistDocument
	 */
	private PlaylistDocument convertToPlaylistDocument(Playlist playlist) {
		List<String> musicIds = new ArrayList<>();
		// playlist.getPlaylistMusics()가 null이 아닐 경우에만 스트림 처리
		if (playlist.getPlaylistMusics() != null) {
			musicIds = playlist.getPlaylistMusics().stream()
					// Solr에 음악 ID를 저장할 때 순서가 중요하지 않다면 정렬은 필수는 아님
					// .sorted((pm1, pm2) -> Integer.compare(pm1.getMusicOrder(), pm2.getMusicOrder()))
					.map(pm -> String.valueOf(pm.getMusic().getId()))
					.collect(Collectors.toList());
		}

		// LocalDateTime을 java.util.Date로 변환 (Solr의 pdate 타입에 맞춤)
		Date createdAtDate = Date.from(playlist.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
		// updatedAt은 Optional일 수 있으므로 null 체크 후 변환
		Date updatedAtDate = playlist.getUpdatedAt() != null ?
				Date.from(playlist.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()) : null;


		return PlaylistDocument.builder()
				.id(String.valueOf(playlist.getId()))
				.title(playlist.getTitle())
				.description(playlist.getDescription())
				.creatorId(String.valueOf(playlist.getUser().getId()))
				.creatorNickname(playlist.getUser().getNickname()) // ⭐ 추가: creatorNickname 설정 ⭐
				.createdAt(createdAtDate)
				.updatedAt(updatedAtDate) // ⭐ 추가: updatedAt 설정 ⭐
				.isPublic(playlist.getIsPublic())
				.musicIds(musicIds)
				.build();
	}
}