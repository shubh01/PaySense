package com.inn.paysense.config;

import com.inn.paysense.advisor.GeneralQueryAdvisor;
import com.inn.paysense.advisor.GeneralQueryResolver;
import com.inn.paysense.advisor.PromptSanitizationAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Value("${prompt.sanitization.model:qwen3.5:4b}")
    private String promptSanitizationModel;

    @Value("${paysense.chat.model:mistral:7b}")
    private String paySenseChatModel;

    @Bean("mainChatModel")
    @Primary
    public OllamaChatModel mainChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder().ollamaApi(ollamaApi).options(OllamaChatOptions.builder().model(paySenseChatModel).temperature(0.0).build()).build();
    }

    @Bean("primaryChatClient")
    @Primary
    public ChatClient primaryChatClient(VectorStore vectorStore, ChatClient.Builder builder, PromptSanitizationAdvisor promptSanitizationAdvisor) {

        SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor(Ordered.HIGHEST_PRECEDENCE);
        GeneralQueryAdvisor generalQueryAdvisor = new GeneralQueryAdvisor(new GeneralQueryResolver());
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = getRetrievalAugmentationAdvisor(vectorStore);
        return builder.defaultAdvisors(List.of(simpleLoggerAdvisor, generalQueryAdvisor, promptSanitizationAdvisor, retrievalAugmentationAdvisor)).build();
    }

    @Bean
    public RetrievalAugmentationAdvisor getRetrievalAugmentationAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder().order(Ordered.HIGHEST_PRECEDENCE + 300).documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).build()).queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(false).build()).build();
    }

    @Bean("guardChatModel")
    public OllamaChatModel guardChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder().ollamaApi(ollamaApi).options(OllamaChatOptions.builder().model(promptSanitizationModel).temperature(0.0).build()).build();
    }
}