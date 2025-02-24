package com.ProjectGraduation.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileService {

    public String uploadFile(String path, MultipartFile file, Long merchantId) throws IOException {

        String merchantPath = path + File.separator + "merchant_" + merchantId;
        File merchantDir = new File(merchantPath);
        if (!merchantDir.exists()) {
            merchantDir.mkdir();
        }


        String originalFileName = file.getOriginalFilename();


        if (originalFileName == null) {
            throw new IllegalStateException("File name cannot be null");
        }


        String uniqueFileName = generateUniqueFileName(merchantPath, originalFileName);


        String filePath = merchantPath + File.separator + uniqueFileName;


        Files.copy(file.getInputStream(), Paths.get(filePath));


        return "merchant_" + merchantId + File.separator + uniqueFileName;
    }

    private String generateUniqueFileName(String merchantPath, String originalFileName) {
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String uniqueFileName = originalFileName;

        File file = new File(merchantPath + File.separator + uniqueFileName);
        int counter = 1;


        while (file.exists()) {
            uniqueFileName = baseName + "_" + counter + extension;
            file = new File(merchantPath + File.separator + uniqueFileName);
            counter++;
        }

        return uniqueFileName;
    }

    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }
}