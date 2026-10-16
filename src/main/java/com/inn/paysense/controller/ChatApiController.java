package com.inn.paysense.controller;

import com.inn.paysense.model.ChatRequest;
import com.inn.paysense.model.ChatResponse;
import com.inn.paysense.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatApiController implements ChatApi{

    @Autowired
    private ChatService chatService;

    @Override
    public ResponseEntity<ChatResponse> ragChat(@RequestBody ChatRequest chatRequest){
        ChatResponse chatReply = chatService.ragChat(chatRequest.query(), chatRequest.fileCategory());
        return ResponseEntity.ok(chatReply);
    }
}
