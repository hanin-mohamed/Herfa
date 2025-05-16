//package com.ProjectGraduation.product.controller;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@RestController
//@RequestMapping("/media")
//public class FileController {
//
//    @Value("${project.poster}")
//    private String baseStoragePath;
//
//    @GetMapping("/**")
//    public ResponseEntity<Resource> serveFile(@RequestHeader(value = "User-Agent", required = false) String userAgent,
//                                              @RequestParam(required = false) String download,
//                                              @RequestAttribute(required = false, name = "org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping") String fullPath,
//                                              @RequestAttribute(required = false, name = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern") String pattern,
//                                              @RequestParam(required = false) String inline) throws IOException {
//
//        // استخراج الـ path من Request URI
//        String relativePath = extractRelativePath();
//
//        Path filePath = Paths.get(baseStoragePath).resolve(relativePath).normalize();
//
//        if (!Files.exists(filePath)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Resource resource = new UrlResource(filePath.toUri());
//        String contentType = Files.probeContentType(filePath);
//        contentType = contentType != null ? contentType : "application/octet-stream";
//
//        String fileName = filePath.getFileName().toString();
//
//        // تحديد الـ Content-Disposition: inline or attachment
//        String contentDisposition = (inline != null && inline.equals("true"))
//                ? "inline"
//                : "attachment";
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + "; filename=\"" + fileName + "\"")
//                .body(resource);
//    }
//
//    private String extractRelativePath() {
//        // هذه الطريقة تعمل لو انت عامل تسجيل للمسارات بشكل ديناميكي
//        // لكن طالما عامل static-path-pattern، فـ ممكن تستخرج المسار من الـ HttpServletRequest بدل كده
//        return ""; // حط هنا طريقة استخراج الـ path لو محتاجها
//    }
//}
