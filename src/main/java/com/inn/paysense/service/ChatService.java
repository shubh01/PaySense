package com.inn.paysense.service;

import com.inn.paysense.model.ChatResponse;
import com.inn.paysense.model.FileCategory;

public interface ChatService {
    ChatResponse ragChat(String query, FileCategory fileCategory);
}
