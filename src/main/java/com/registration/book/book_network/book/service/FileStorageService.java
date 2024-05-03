package com.registration.book.book_network.book.service;

import com.registration.book.book_network.core.models.Book;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    @Value("${application.file.upload.path}")
    private String fileUploadPath;

    public String saveFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull Integer userId)
    {
        final String fileUploadSubPath = "users" + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    )
    {
        final String fileUploadPath = this.fileUploadPath + separator + fileUploadSubPath;
        final File targetFolder = new File(fileUploadPath);

        if (!targetFolder.exists()) {
            var folderCreated = targetFolder.mkdirs();
            if(!folderCreated) {
                log.warn("Failed to create the target folder");
                return null;
            }
        }
        final String fileName = sourceFile.getOriginalFilename();
        final String fileExtension = getFileExtension(fileName);
        // .upload/users/1/4669446566744.jpg
        var targetFilePath = fileUploadPath + separator + currentTimeMillis() + '.' + fileExtension;
        var targetPath = Paths.get(targetFilePath);

        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved successfully to " + targetFilePath);
            return targetFilePath;
        } catch (Exception e) {
            log.error("File was not saved", e);
        }
        return fileUploadSubPath + separator + fileName; // or null
    }

    private String getFileExtension(String fileName) {
        if(fileName == null || fileName.isEmpty()){
            return null;
        }
        // something.jpg
        int lastDotIndex = fileName.lastIndexOf('.');
        if(lastDotIndex == -1){
            return  "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
