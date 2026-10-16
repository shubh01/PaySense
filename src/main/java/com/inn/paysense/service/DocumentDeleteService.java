package com.inn.paysense.service;

import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;

public interface DocumentDeleteService {
    String deleteDocument(FileCategory fileCategory) throws PaySenseException;
}
