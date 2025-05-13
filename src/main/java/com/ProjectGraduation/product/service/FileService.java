package com.ProjectGraduation.product.service;

import com.ProjectGraduation.product.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@Service
public class FileService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(".mp4");
    private static final Set<String> ALLOWED_EXTENSIONS;

    static {
        ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".mp4");
    }

    @Value("${project.poster}")
    private String baseStoragePath;

    @Value("${base.url}")
    private String baseUrl;

    public String uploadFile(String basePath, MultipartFile file, Long userId, String type, String name) throws IOException {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String mediaType = getMediaType(extension);

        Path filePath = buildFilePath(userId, type, name, extension, mediaType);
        createDirectories(filePath);

        Files.copy(file.getInputStream(), filePath);

        return buildFileUrl(userId, type, filePath.getFileName().toString(), mediaType);
    }


    public InputStream getResourceFile(String relativePath) throws FileNotFoundException {
        Path filePath = Paths.get(baseStoragePath, relativePath).normalize();

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found at path: " + filePath);
        }

        return new FileInputStream(filePath.toFile());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File cannot be null or empty.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileUploadException("Invalid file name.");
        }

        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException("Unsupported file type. Only .jpg, .png, and .mp4 are allowed.");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private String getMediaType(String extension) {
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "images";
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return "videos";
        }
        return "other";
    }

    private Path buildFilePath(Long userId, String type, String name, String extension, String mediaType) {
        String sanitizedName = sanitizeFileName(name);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeFileName = "_" + timestamp + extension;

        if ("profile".equalsIgnoreCase(type)) {
            return Paths.get(baseStoragePath, "profiles", "user_" + userId, mediaType, safeFileName);
        } else {
            String typeFolder = switch (type.toLowerCase()) {
                case "product" -> "products";
                case "auction" -> "auctions";
                default -> "events";
            };
            return Paths.get(baseStoragePath, "merchants", "merchant_" + userId, typeFolder, mediaType, safeFileName);
        }
    }

    private void createDirectories(Path filePath) throws IOException {
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    private String buildFileUrl(Long userId, String type, String filename, String mediaType) {
        if ("profile".equalsIgnoreCase(type)) {
            return String.format("%s/profiles/user_%d/%s/%s", baseUrl, userId, mediaType, filename);
        } else {
            String typeFolder = switch (type.toLowerCase()) {
                case "product" -> "products";
                case "auction" -> "auctions";
                default -> "events";
            };
            return String.format("%s/merchants/merchant_%d/%s/%s/%s",
                    baseUrl, userId, typeFolder, mediaType, filename);
        }
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return "file";
        }

         return originalName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .trim();
    }
}