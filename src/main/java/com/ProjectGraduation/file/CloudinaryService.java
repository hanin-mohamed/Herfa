package com.ProjectGraduation.file;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folderType, Long merchantId) throws IOException {
        String folderPath = folderType + "/merchant_" + merchantId;

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderPath,
                        "resource_type", "auto"
                )
        );

        return (String) uploadResult.get("secure_url");
    }

}
