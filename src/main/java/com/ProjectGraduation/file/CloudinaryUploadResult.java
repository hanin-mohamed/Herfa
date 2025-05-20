package com.ProjectGraduation.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CloudinaryUploadResult {

    private String secureUrl;
    private String publicId;
}
