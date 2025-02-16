package com.ProjectGraduation.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    public String uploadFile(String path, MultipartFile file) throws IOException {
        // Get the original file name
        String originalFileName = file.getOriginalFilename();

        // Extract the file extension (if present)
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate a unique file name using UUID
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Get the full file path
        String filePath = path + File.separator + uniqueFileName;

        // Create the directory if it doesn't exist
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Copy the file to the specified path
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return uniqueFileName;
    }

    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;

        return new FileInputStream(filePath);
    }
}
