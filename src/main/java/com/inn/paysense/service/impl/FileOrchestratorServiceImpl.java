package com.inn.paysense.service.impl;

import com.inn.paysense.exception.ErrorMessageConstants;
import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.service.DocumentDeleteService;
import com.inn.paysense.service.FileIngestionService;
import com.inn.paysense.service.FileOrchestratorService;
import com.inn.paysense.service.FileUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class FileOrchestratorServiceImpl implements FileOrchestratorService {

    Logger logger = LoggerFactory.getLogger(FileOrchestratorServiceImpl.class);

    @Autowired
    private FileUploaderService fileUploaderService;

    @Autowired
    private FileIngestionService fileIngestionService;

    @Autowired
    private DocumentDeleteService documentDeleteService;

    @Override
    public Boolean processFile(MultipartFile multipartFile, FileCategory fileCategory) throws PaySenseException {
        Resource resource = null;
        try {
            logger.info("processFile fileCategory {}", fileCategory.name());
            documentDeleteService.deleteDocument(fileCategory);
            resource = fileUploaderService.uploadFile(multipartFile);
            logger.info("processFile fileCategory {} uploaded successfully", fileCategory.name());
            if (resource == null)
                return Boolean.FALSE;
            fileIngestionService.ingestDocument(resource, fileCategory);
        } catch (PaySenseException paySenseException) {
            logger.error(paySenseException.getMessage());
            throw new PaySenseException(ErrorMessageConstants.FILE_INGESTION_ERROR_MESSAGE, paySenseException.getStatus());
        } finally {
            fileUploaderService.deleteTempFile(resource);
        }
        return Boolean.TRUE;
    }


}
