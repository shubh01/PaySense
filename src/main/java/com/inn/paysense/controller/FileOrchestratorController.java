package com.inn.paysense.controller;

import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.service.FileOrchestratorService;
import com.inn.paysense.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileOrchestratorController implements FileOrchestrator {

    @Autowired
    private FileOrchestratorService fileOrchestratorService;

    @Override
    public ResponseEntity processFile(@RequestParam("file") MultipartFile file, @RequestParam("fileCategory") FileCategory fileCategory) throws PaySenseException {
        fileOrchestratorService.processFile(file, fileCategory);
        return ResponseEntity.ok(Constants.FILE_PROCESSED_SUCCESSFULLY);
    }
}
