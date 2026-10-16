package com.inn.paysense.service.impl;

import com.inn.paysense.model.ChatResponse;
import com.inn.paysense.model.FileCategory;
import com.inn.paysense.service.ChatService;
import com.inn.paysense.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private VectorStore vectorStore;

    private final ChatClient chatClient;

    @Value("classpath:/templates/paySenseSystemPrompt.st")
    private Resource paySenseSystemPrompt;

    private final static String JOINING_DELIMITER = "\n--\n";

    public ChatServiceImpl(@Qualifier("primaryChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public ChatResponse ragChat(String query, FileCategory fileCategory) {
        logger.info("@method ragChat query {}", query);
        SearchRequest searchRequest = SearchRequest.builder().query(query).topK(4).similarityThreshold(0.60).build();
        List<Document> documentList = vectorStore.similaritySearch(searchRequest);
        logger.info("@method ragChat documentList {}", documentList);
        String contextForLlm = documentList.stream().filter( docs -> docs.getMetadata()
                        .get(Constants.FILE_CATEGORY).toString().equalsIgnoreCase(fileCategory.getName()))
                .map(document -> (String) document.getMetadata().get(Constants.PARENT_TEXT_CONTEXT))
                .distinct()
                .collect(Collectors.joining(JOINING_DELIMITER));
        logger.info("@method ragChat contextForLlm {}", contextForLlm);
        String llmResponse = chatClient.prompt().system(promptSystemSpec ->
                promptSystemSpec.text(paySenseSystemPrompt).param(Constants.QUESTION_ANSWER_CONTEXT, contextForLlm))
                .user(query).call().content();
        logger.info("@method ragChat llmResponse {}", llmResponse);
        return new ChatResponse(query,llmResponse, LocalDateTime.now());

    }
}
