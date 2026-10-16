package com.inn.paysense.service.impl;

import com.inn.paysense.exception.PaySenseException;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.service.DocumentDeleteService;
import com.inn.paysense.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentDeleteServiceImpl implements DocumentDeleteService {

    private Logger logger = LoggerFactory.getLogger(DocumentDeleteServiceImpl.class);

    @Autowired
    private VectorStore vectorStore;

    @Override
    public String deleteDocument(FileCategory fileCategory) throws PaySenseException{

        logger.info("deleteDocument fileCategory {}", fileCategory);
        try{
            FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();

            List<Document> documentList = vectorStore.similaritySearch(SearchRequest.builder().query("*").topK(Integer.MAX_VALUE)
                    .similarityThresholdAll().filterExpression(new FilterExpressionBuilder()
                            .eq(Constants.FILE_CATEGORY, fileCategory.getName()).build()).build());

            logger.info("going to delete {} documents for file category {}", documentList.size(), fileCategory.getName());

            vectorStore.delete(filterExpressionBuilder.eq(Constants.FILE_CATEGORY, fileCategory.getName()).build());
            logger.info("document deleted successfully");
            return Constants.DELETE_DOCUMENT_SUCCESS_MESSAGE;
        }catch (Exception e){
            logger.error("exception occurred @method deleteDocument", e);
            throw new PaySenseException(Constants.DELETE_DOCUMENT_FAILURE_MESSAGE, e, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
