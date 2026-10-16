package com.inn.paysense.service;

import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import org.springframework.web.multipart.MultipartFile;

public interface FileOrchestratorService {
   Boolean processFile(MultipartFile multipartFile, FileCategory fileCategory) throws PaySenseException;
}
