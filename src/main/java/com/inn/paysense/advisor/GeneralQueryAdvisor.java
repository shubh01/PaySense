package com.inn.paysense.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Optional;

public class GeneralQueryAdvisor implements CallAdvisor {

    private Logger logger = LoggerFactory.getLogger(GeneralQueryAdvisor.class);

    private final GeneralQueryResolver generalQueryResolver;

    public GeneralQueryAdvisor(GeneralQueryResolver generalQueryResolver) {
        this.generalQueryResolver = generalQueryResolver;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {

        logger.info("checking for general query");
        String userQuery = chatClientRequest.prompt().getUserMessage().getText();

        Optional<String> resolvedResponse = generalQueryResolver.resolve(userQuery);

        if(resolvedResponse.isPresent()){
            logger.info("prompt is general query");
            ChatResponse chatResponse = ChatResponse.builder().generations(List.of(new Generation(new AssistantMessage(resolvedResponse.get())))).build();
            return ChatClientResponse.builder().chatResponse(chatResponse).build();
        }
        logger.info("prompt is not general query");
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public String getName() {
        return "GeneralQueryAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+100;
    }
}
