package com.boot.service;

import com.boot.config.FileStorageProperties;
import com.boot.exception.FileStorageException;
import com.boot.exception.MyFileNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

<<<<<<< HEAD
	private final Path fileStorageLocation; // 파일이 저장될 실제 기본 경로 (예: C:/uploads)
	private final String uploadBaseDir; // application.properties에서 설정한 경로 (예: uploads/)
=======
	private final Path fileStorageLocation;
	private final String uploadBaseDir;
>>>>>>> main

	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.uploadBaseDir = fileStorageProperties.getUploadDir();
		this.fileStorageLocation = Paths.get(this.uploadBaseDir).toAbsolutePath().normalize();
		log.info("FileStorageService 초기화. 파일 저장 기본 경로 (절대 경로): {}", this.fileStorageLocation);

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("파일을 저장할 디렉토리(" + this.fileStorageLocation + ")를 생성할 수 없습니다.", ex);
		}
	}

<<<<<<< HEAD
	/**
	 * 파일 저장 (업로드) - ⭐ 저장된 파일의 전체 절대 경로를 반환하도록 수정 ⭐
	 * 
	 * @param file   업로드할 MultipartFile 객체
	 * @param subDir 파일을 저장할 하위 디렉토리 (예: "music", "images", "cover-image")
	 * @return 저장된 파일의 **전체 파일 시스템 절대 경로** 문자열 (예: "C:/path/to/your/project/uploads/music/uuid-filename.mp3")
	 */
=======
>>>>>>> main
	public String storeFile(MultipartFile file, String subDir) {
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		log.info("파일 저장 요청: 원본 파일명={}, 서브디렉토리={}", originalFileName, subDir);

		try {
			if (originalFileName.contains("..")) {
				throw new FileStorageException("파일명에 유효하지 않은 경로 시퀀스가 포함되어 있습니다 " + originalFileName);
			}

			String fileExtension = "";
			int dotIndex = originalFileName.lastIndexOf('.');
			if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
				fileExtension = originalFileName.substring(dotIndex);
			}
			String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

<<<<<<< HEAD
			// 최종 저장될 파일의 절대 경로를 계산합니다.
			Path targetDirectory = this.fileStorageLocation.resolve(subDir).normalize();
			Path targetLocation = targetDirectory.resolve(uniqueFileName);

			// 서브 디렉토리가 없으면 생성
			Files.createDirectories(targetDirectory); // 파일을 포함한 경로가 아니라 디렉토리만 생성하도록 변경
=======
			Path targetDirectory = this.fileStorageLocation.resolve(subDir).normalize();
			Path targetLocation = targetDirectory.resolve(uniqueFileName);

			Files.createDirectories(targetDirectory);
>>>>>>> main
			log.info("파일 저장 타겟 경로 (절대 경로): {}", targetLocation);

			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

<<<<<<< HEAD
			// ⭐ 저장된 파일의 전체 절대 경로를 반환합니다. ⭐
			// Windows 환경에서 경로 구분자가 '\'로 나올 수 있으므로 '/'로 통일하여 저장
=======
>>>>>>> main
			return targetLocation.toString().replace("\\", "/");
		} catch (IOException ex) {
			log.error("파일 저장 실패: 원본 파일명={}, 서브디렉토리={}, 오류={}", originalFileName, subDir, ex.getMessage(), ex);
			throw new FileStorageException("파일 " + originalFileName + "을 저장할 수 없습니다. 다시 시도해 주세요!", ex);
		}
	}

<<<<<<< HEAD
	/**
	 * 가사 내용을 파일로 저장하고 그 파일의 전체 절대 경로를 반환합니다.
	 *
	 * @param lyricsContent 저장할 가사 내용 문자열
	 * @return 저장된 가사 파일의 전체 파일 시스템 절대 경로 문자열
	 */
	public String storeLyricsFile(String lyricsContent) throws IOException {
		String subDir = "lyrics"; // 가사 파일 저장용 서브 디렉토리
		String uniqueFileName = UUID.randomUUID().toString() + ".txt"; // 가사는 .txt 파일로 저장
=======
	public String storeLyricsFile(String lyricsContent) throws IOException {
		String subDir = "lyrics";
		String uniqueFileName = UUID.randomUUID().toString() + ".txt";
>>>>>>> main

		Path targetDirectory = this.fileStorageLocation.resolve(subDir).normalize();
		Path targetLocation = targetDirectory.resolve(uniqueFileName);

		try {
			Files.createDirectories(targetDirectory);
<<<<<<< HEAD
			Files.write(targetLocation, lyricsContent.getBytes()); // 가사 내용을 바이트 배열로 변환하여 파일에 쓰기
=======
			Files.write(targetLocation, lyricsContent.getBytes());
>>>>>>> main
			log.info("가사 파일 저장 완료. 경로: {}", targetLocation);
			return targetLocation.toString().replace("\\", "/");
		} catch (IOException ex) {
			log.error("가사 파일 저장 실패: 오류={}", ex.getMessage(), ex);
			throw new FileStorageException("가사 파일을 저장할 수 없습니다.", ex);
		}
	}

<<<<<<< HEAD
	/**
	 * 가사 파일의 내용을 읽어와 문자열로 반환합니다.
	 *
	 * @param fullFilePath 가사 파일의 전체 절대 경로
	 * @return 가사 파일의 내용 문자열
	 */
	public String readLyricsFile(String fullFilePath) throws IOException {
		log.info("가사 파일 읽기 요청: fullFilePath={}", fullFilePath);
=======
	public String readLyricsFile(String fullFilePath) {
		log.info("가사 파일 읽기 요청: fullFilePath={}", fullFilePath);
		if (fullFilePath == null || fullFilePath.isEmpty()) {
			log.warn("가사 파일 경로가 유효하지 않습니다: {}", fullFilePath);
			return null;
		}
>>>>>>> main
		try {
			Path filePath = Paths.get(fullFilePath).normalize();
			if (Files.exists(filePath) && Files.isReadable(filePath)) {
				String content = new String(Files.readAllBytes(filePath));
				log.info("가사 파일 읽기 성공: {}", filePath);
				return content;
			} else {
<<<<<<< HEAD
				log.warn("가사 파일을 찾을 수 없거나 읽을 수 없습니다: {}", fullFilePath);
				throw new MyFileNotFoundException("가사 파일을 찾을 수 없거나 읽을 수 없습니다. " + fullFilePath);
			}
		} catch (IOException ex) {
			log.error("가사 파일 읽기 중 오류 발생: fullFilePath={}, 오류={}", fullFilePath, ex.getMessage(), ex);
			throw ex; // 호출하는 쪽에서 처리하도록 예외 다시 던지기
		}
	}


	/**
	 * 파일 로드 (조회) - ⭐ fullFilePath 파라미터 사용 ⭐
	 * 
	 * @param fullFilePath Music 엔티티의 filePath 컬럼에 저장된 전체 경로 (이제는 절대 경로)
	 * @return 파일에 대한 Resource 객체
	 */
	public Resource loadFileAsResource(String fullFilePath) {
		log.info("파일 로드 요청 (DB 저장된 절대 경로): {}", fullFilePath);
		try {
			// DB에 저장된 fullFilePath는 이미 실제 파일 시스템의 절대 경로입니다.
			Path file = Paths.get(fullFilePath).normalize(); // 해당 경로를 그대로 Path 객체로 변환합니다.
=======
				log.warn("가사 파일을 찾을 수 없거나 읽을 수 없습니다. null 반환: {}", fullFilePath);
				return null;
			}
		} catch (IOException ex) {
			log.error("가사 파일 읽기 중 오류 발생 (null 반환): fullFilePath={}, 오류={}", fullFilePath, ex.getMessage(), ex);
			return null;
		}
	}

	public Resource loadFileAsResource(String fullFilePath) {
		log.info("파일 로드 요청 (DB 저장된 절대 경로): {}", fullFilePath);
		try {
			Path file = Paths.get(fullFilePath).normalize();
>>>>>>> main
			log.info("변환된 Path 객체: {}", file.toAbsolutePath().toString());
			log.info("파일 존재 여부 (Files.exists): {}", Files.exists(file));
			log.info("파일 읽기 가능 여부 (Files.isReadable): {}", Files.isReadable(file));
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() && resource.isReadable()) {
				log.info("파일 로드 성공: {}", file);
				return resource;
			} else {
				log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", file);
				throw new MyFileNotFoundException("파일을 찾을 수 없거나 읽을 수 없습니다. " + fullFilePath);
			}
		} catch (MalformedURLException ex) {
			log.error("잘못된 파일 경로 URL: fullFilePath={}, 오류={}", fullFilePath, ex.getMessage(), ex);
			throw new MyFileNotFoundException("파일 경로 URL이 잘못되었습니다. " + fullFilePath, ex);
		}
	}

<<<<<<< HEAD
	/**
	 * 파일 삭제 - ⭐ fullFilePath 파라미터 사용 ⭐
	 * 
	 * @param fullFilePath 삭제할 파일의 전체 경로 (이제는 절대 경로)
	 * @return 삭제 성공 여부
	 */
	public boolean deleteFile(String fullFilePath) {
		log.info("파일 삭제 요청: fullFilePath={}", fullFilePath);
		try {
			// DB에 저장된 fullFilePath는 이미 실제 파일 시스템의 절대 경로입니다.
=======
	public boolean deleteFile(String fullFilePath) {
		log.info("파일 삭제 요청: fullFilePath={}", fullFilePath);
		try {
>>>>>>> main
			Path filePathToDelete = Paths.get(fullFilePath).normalize();
			boolean deleted = Files.deleteIfExists(filePathToDelete);
			if (deleted) {
				log.info("파일 삭제 성공: {}", filePathToDelete);
			} else {
				log.warn("파일이 존재하지 않아 삭제할 수 없습니다: {}", filePathToDelete);
			}
			return deleted;
		} catch (IOException ex) {
			log.error("파일 삭제 중 오류 발생: fullFilePath={}, 오류={}", fullFilePath, ex.getMessage(), ex);
<<<<<<< HEAD
			// 파일 삭제 실패 시에도 true/false 반환보다는 예외를 던져서 명확하게 처리하도록 변경하는 것도 고려 가능
=======
>>>>>>> main
			return false;
		}
	}

<<<<<<< HEAD
	// ⭐ getStoredFilePath 메서드는 더 이상 MusicService에서 직접 호출될 필요가 없어졌습니다. ⭐
	// ⭐ storeFile과 storeLyricsFile이 직접 전체 절대 경로를 반환하기 때문입니다. ⭐
	 public String getFilePath(String subDir, String uniqueFileName) {
	     Path absolutePath = this.fileStorageLocation.resolve(subDir).resolve(uniqueFileName).normalize();
	     String fullPathString = absolutePath.toString();
	     log.info("DB에 저장될 절대 경로 (파일): {}", fullPathString);
	     return fullPathString.replace("\\", "/");
	 }
	 public String readTextFile(String filePath) throws IOException {
	        if (filePath == null || filePath.isEmpty()) {
	            return null;
	        }
	        Path path = Paths.get(filePath);
	        if (Files.exists(path) && Files.isReadable(path)) {
	            return Files.readString(path);
	        }
	        return null;
	    }
}
=======
	 public String getFilePath(String subDir, String uniqueFileName) {
		 Path absolutePath = this.fileStorageLocation.resolve(subDir).resolve(uniqueFileName).normalize();
		 String fullPathString = absolutePath.toString();
		 log.info("DB에 저장될 절대 경로 (파일): {}", fullPathString);
		 return fullPathString.replace("\\","/");
		 }
	 public String readTextFile(String filePath) throws IOException {
		 if (filePath == null || filePath.isEmpty()) {
			 return null;
		 }
		 Path path = Paths.get(filePath);
		 if (Files.exists(path) && Files.isReadable(path)) {
			 return Files.readString(path);
		 }
		 return null; 
	 }
}

>>>>>>> main
