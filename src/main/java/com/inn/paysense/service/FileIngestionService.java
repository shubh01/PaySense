package com.inn.paysense.service;

import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileIngestionService {
    void ingestDocument(Resource resource, FileCategory fileCategory) throws PaySenseException;
}
