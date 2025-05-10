package com.ProjectGraduation.product.service;

import com.ProjectGraduation.product.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileService {

    public String uploadFile(String basePath, MultipartFile file, Long userId, String type, String name) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File cannot be null or empty.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileUploadException("Invalid file name.");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".mp4")) {
            throw new FileUploadException("Unsupported file type. Only .jpg, .png, and .mp4 are allowed.");
        }

        String mediaFolder = (extension.equals(".jpg") || extension.equals(".png")) ? "photo" :
                (extension.equals(".mp4")) ? "video" : "other";

        String fullPath;

        if (type.equalsIgnoreCase("profile")) {
            fullPath = basePath + File.separator + "profile_pictures" + File.separator + "user_" + userId + File.separator + mediaFolder;
        } else {
            String merchantFolder = "merchant_" + userId;
            String typeFolder;
            switch (type.toLowerCase()) {
                case "product":
                    typeFolder = "product";
                    break;
                case "auction":
                    typeFolder = "auction";
                    break;
                default:
                    typeFolder = "event";
                    break;
            }
            fullPath = basePath + File.separator + merchantFolder + File.separator + typeFolder + File.separator + mediaFolder;
        }

        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String sanitizedName = name.replaceAll("[^\\p{ASCII}]", "_")
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeFileName = sanitizedName + timestamp + extension;
        String filePath = fullPath + File.separator + safeFileName;

        Files.copy(file.getInputStream(), Paths.get(filePath));

        String relativePath;
        if (type.equalsIgnoreCase("profile")) {
            relativePath = "profile_pictures/user_" + userId + "/" + mediaFolder + "/" + safeFileName;
        } else {
            String typeFolder = switch (type.toLowerCase()) {
                case "product" -> "product";
                case "auction" -> "auction";
                default -> "event";
            };
            relativePath = "merchant_" + userId + "/" + typeFolder + "/" + mediaFolder + "/" + safeFileName;
        }

        return relativePath.replace("\\", "/");
    }



    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileNotFoundException("File name cannot be null or empty.");
        }
        String filePath = path + File.separator + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found at path: " + filePath);
        }
        return new FileInputStream(filePath);
    }
}