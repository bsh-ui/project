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

	private final Path fileStorageLocation;
	private final String uploadBaseDir;

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

			Path targetDirectory = this.fileStorageLocation.resolve(subDir).normalize();
			Path targetLocation = targetDirectory.resolve(uniqueFileName);

			Files.createDirectories(targetDirectory);
			log.info("파일 저장 타겟 경로 (절대 경로): {}", targetLocation);

			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return targetLocation.toString().replace("\\", "/");
		} catch (IOException ex) {
			log.error("파일 저장 실패: 원본 파일명={}, 서브디렉토리={}, 오류={}", originalFileName, subDir, ex.getMessage(), ex);
			throw new FileStorageException("파일 " + originalFileName + "을 저장할 수 없습니다. 다시 시도해 주세요!", ex);
		}
	}

	public String storeLyricsFile(String lyricsContent) throws IOException {
		String subDir = "lyrics";
		String uniqueFileName = UUID.randomUUID().toString() + ".txt";

		Path targetDirectory = this.fileStorageLocation.resolve(subDir).normalize();
		Path targetLocation = targetDirectory.resolve(uniqueFileName);

		try {
			Files.createDirectories(targetDirectory);
			Files.write(targetLocation, lyricsContent.getBytes());
			log.info("가사 파일 저장 완료. 경로: {}", targetLocation);
			return targetLocation.toString().replace("\\", "/");
		} catch (IOException ex) {
			log.error("가사 파일 저장 실패: 오류={}", ex.getMessage(), ex);
			throw new FileStorageException("가사 파일을 저장할 수 없습니다.", ex);
		}
	}

	public String readLyricsFile(String fullFilePath) {
		log.info("가사 파일 읽기 요청: fullFilePath={}", fullFilePath);
		if (fullFilePath == null || fullFilePath.isEmpty()) {
			log.warn("가사 파일 경로가 유효하지 않습니다: {}", fullFilePath);
			return null;
		}
		try {
			Path filePath = Paths.get(fullFilePath).normalize();
			if (Files.exists(filePath) && Files.isReadable(filePath)) {
				String content = new String(Files.readAllBytes(filePath));
				log.info("가사 파일 읽기 성공: {}", filePath);
				return content;
			} else {
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

	public boolean deleteFile(String fullFilePath) {
		log.info("파일 삭제 요청: fullFilePath={}", fullFilePath);
		try {
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
			return false;
		}
	}

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

