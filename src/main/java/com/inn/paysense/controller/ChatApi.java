package com.inn.paysense.controller;

import com.inn.paysense.model.ChatRequest;
import com.inn.paysense.model.ChatResponse;
import com.inn.paysense.utility.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ChatApi {

    @PostMapping(Constants.RAG_CHAT_URL)
    ResponseEntity<ChatResponse> ragChat(@RequestBody ChatRequest chatRequest);

}
