package com.ferreteria.protech.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String apiKey;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Configuramos el modelo, si la api-key es "demo" fallará al hacer peticiones, pero permitirá iniciar Spring Boot.
        return OpenAiChatModel.builder()
                .apiKey(apiKey.equals("demo") ? "demo" : apiKey)
                .modelName("gpt-4o-mini")
                .build();
    }

    @Bean
    public InventoryAssistant inventoryAssistant(ChatLanguageModel chatLanguageModel, InventoryDatabaseTools tools) {
        // Construimos el servicio proxy de LangChain4j inyectándole el modelo y las herramientas de BD
        return AiServices.builder(InventoryAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(tools)
                .build();
    }
}
