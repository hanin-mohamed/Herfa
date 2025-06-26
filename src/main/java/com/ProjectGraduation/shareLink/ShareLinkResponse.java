package com.ProjectGraduation.shareLink;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShareLinkResponse {

    private boolean success;
    private String message;
    private String shareLink;
    private Long productId;
    private LocalDateTime timestamp;

}
