package com.inn.paysense.service;

import com.inn.paysense.exception.PaySenseException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploaderService {
    Resource uploadFile(MultipartFile multipartFile) throws PaySenseException;
    void deleteTempFile(Resource resource) throws PaySenseException;
}
