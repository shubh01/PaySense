package com.inn.paysense.service.impl;

import com.inn.paysense.exception.ErrorMessageConstants;
import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.service.FileUploaderService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class FileUploaderServiceImpl implements FileUploaderService {

    private static String SEPARATOR = "_";
    private static String DIR_PREFIX = "paysense_ingest_";
    private static String DEFAULT_DOCUMENT_NAME = "document.pdf";

    @Override
    public Resource uploadFile(MultipartFile multipartFile) throws PaySenseException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new PaySenseException(ErrorMessageConstants.EMPTY_NULL_FILE_ERROR_MESSAGE, HttpStatus.BAD_REQUEST.value());
        }
        Path tempDir = null;
        try {
            String dirPrefix = DIR_PREFIX + UUID.randomUUID().toString() + SEPARATOR;
            tempDir = Files.createTempDirectory(dirPrefix);
            String originalFilename = multipartFile.getOriginalFilename();
            String safeFileName = (originalFilename != null) ? Path.of(originalFilename).getFileName().toString() : DEFAULT_DOCUMENT_NAME;
            Path targetPath = tempDir.resolve(safeFileName);
            Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            Resource tempResource = new FileSystemResource(targetPath.toFile());
            return tempResource;
        } catch (IOException e) {
            throw new PaySenseException(ErrorMessageConstants.FAILED_TO_PROCESS_FILE_ERROR_MESSAGE, e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public void deleteTempFile(Resource resource) throws PaySenseException{
        try{
            if (resource != null && resource.exists()) {
                File file = resource.getFile();
                if (file.isDirectory()) {
                    org.springframework.util.FileSystemUtils.deleteRecursively(file);
                } else {
                    Files.deleteIfExists(file.toPath());
                }
            }
        }catch (IOException ioException){
            throw new PaySenseException(ErrorMessageConstants.DELETE_FILE_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
