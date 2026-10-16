package com.inn.paysense.controller;


import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.utility.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface FileOrchestrator {

    @PostMapping(Constants.PROCESS_FILE_URL)
    ResponseEntity processFile(@RequestParam("file") MultipartFile file, @RequestParam("fileCategory") FileCategory fileCategory) throws PaySenseException;

}
