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

    public String uploadFile(String basePath, MultipartFile file, Long merchantId, String type, String name) throws IOException {
        // Validate the file
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File cannot be null or empty.");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileUploadException("Invalid file name.");
        }
        // Determine the media type (photo or video) based on the file extension
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".mp4")) {
            throw new FileUploadException("Unsupported file type. Only .jpg, .png, and .mp4 are allowed.");
        }

        String mediaFolder = (extension.equals(".jpg") || extension.equals(".png")) ? "photo" :
                (extension.equals(".mp4")) ? "video" : "other";

        // Define the directory structure
        String merchantFolder = "merchant_" + merchantId;
        String typeFolder = type.equalsIgnoreCase("product") ? "product" : "event";

        // Create the full directory path
        String fullPath = basePath + File.separator + merchantFolder + File.separator + typeFolder + File.separator + mediaFolder;
        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate a unique file name using the product/event name and timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeFileName = name + timestamp + extension;

        // Define the full file path
        String filePath = fullPath + File.separator + safeFileName;

        // Save the file
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // Return the relative path for storage in the database
        return merchantFolder + " " + typeFolder + " " + mediaFolder + " " + safeFileName;
    }

    //    private String generateUniqueFileName(String merchantPath, String originalFileName) {
    //////////////////        String uniqueFileName = generateUniqueFileName(merchantPath, originalFileName);
    //        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
    //        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
    //        String uniqueFileName = originalFileName;
    //
    //        File file = new File(merchantPath + File.separator + uniqueFileName);
    //        int counter = 1;
    //
    //
    //        while (file.exists()) {
    //            uniqueFileName = baseName + "_" + counter + extension;
    //            file = new File(merchantPath + File.separator + uniqueFileName);
    //            counter++;
    //        }
    //
    //        return uniqueFileName;
    //    }

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