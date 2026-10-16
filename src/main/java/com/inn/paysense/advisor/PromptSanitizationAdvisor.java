package com.inn.paysense.advisor;

import com.inn.paysense.model.PromptSanitizationResponse;
import com.inn.paysense.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptSanitizationAdvisor implements CallAdvisor {

    private Logger logger = LoggerFactory.getLogger(PromptSanitizationAdvisor.class);

    private final ChatClient guardModelChatClient;

    @Value("classpath:/templates/paySensePromptSanitizationPrompt.st")
    private Resource paySensePromptSanitizationPrompt;

    private final String SENSITIVE_CATEGORY_PROMPT= "S1";
    private final String SPACE_SEPARATOR = " ";

    public PromptSanitizationAdvisor(@Qualifier("guardChatModel") OllamaChatModel guardChatModel) {
        this.guardModelChatClient = ChatClient.builder(guardChatModel).defaultAdvisors(List.of(new SimpleLoggerAdvisor())).build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {

        String prompt = chatClientRequest.prompt().getUserMessage().getText();
        logger.info("adviseCall prompt {}",prompt);
        String sanitizationResponse = guardModelChatClient.prompt().options(ChatOptions.builder().topK(1).temperature(0.0))
                .system(paySensePromptSanitizationPrompt).user(prompt)
                .call().content();
        logger.info("sanitizationResponse {}", sanitizationResponse);

        if(sanitizationResponse == null || sanitizationResponse.isBlank()){
            return callAdvisorChain.nextCall(chatClientRequest);
        }else {
            String[] responseArray = sanitizationResponse.replaceAll("\\r?\\n", SPACE_SEPARATOR).replaceAll("\\s+", SPACE_SEPARATOR).trim().split(SPACE_SEPARATOR);
            PromptSanitizationResponse promptSanitizationResponse = new PromptSanitizationResponse(responseArray[0], responseArray[1]);
            if(promptSanitizationResponse.result().equalsIgnoreCase(Constants.SAFE_RESPONSE)){
                return callAdvisorChain.nextCall(chatClientRequest);
            }else{
                String promptResponseMessage = Constants.INCORRECT_PROMPT_RESPONSE;
                if(promptSanitizationResponse.reason().equalsIgnoreCase(SENSITIVE_CATEGORY_PROMPT)){
                    promptResponseMessage = Constants.SENSITIVE_INFO_PROMPT_RESPONSE;
                }
                ChatResponse chatResponse = ChatResponse.builder().generations(List.of(new Generation(new AssistantMessage(promptResponseMessage)))).build();
                return ChatClientResponse.builder().chatResponse(chatResponse).build();
            }
        }
    }

    @Override
    public String getName() {
        return "PromptSanitizationAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }
}
