package com.cakequake.cakequakeback.cakeAI.service;

import com.cakequake.cakequakeback.cakeAI.dto.AIRequestDTO;
import com.cakequake.cakequakeback.cakeAI.entities.CakeAI;
import com.cakequake.cakequakeback.cakeAI.repo.CakeAIRepository;
import com.cakequake.cakequakeback.cakeAI.validator.CakeAIValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cakequake.cakequakeback.cakeAI.util.AIUtils.handleAIProcessing;

@Service
@Transactional
@RequiredArgsConstructor
public class CakeAIServiceImpl implements CakeAIService {

    private final ChatClient chatClient;
    private final ImageModel imageModel;
    private final CakeAIValidator cakeAIValidator;
    private final CakeAIRepository cakeAIRepository;

    @Value("classpath:/prompts/cake-chat.st")
    private Resource cakeChatResource;
    @Value("classpath:/prompts/cake-options.st")
    private Resource cakeOptionResource;
    @Value("classpath:/prompts/cake-lettering.st")
    private Resource cakeLetteringPromptResource;
    @Value("classpath:/prompts/cake-design.st")
    private Resource cakeDesignPromptResource;

    @Override
    public String generateAnswer(String question, String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        cakeAIValidator.validateCommonAI(new AIRequestDTO(question, sessionId));

        return handleAIProcessing(() -> {
            List<CakeAI> chatLogs = cakeAIRepository.findBySessionIdOrderByRegDateAsc(currentSessionId);

            List<Message> conversationHistory = new ArrayList<>();
            for (CakeAI log : chatLogs) {
                conversationHistory.add(new UserMessage(log.getQuestion()));
                conversationHistory.add(new AssistantMessage(log.getAnswer()));
            }
            conversationHistory.add(new UserMessage(question));

            PromptTemplate template = new PromptTemplate(cakeChatResource);
            String templatedQuestion = template.render(Map.of("question", question));

            String aiResponseContent = chatClient.prompt()
                    .messages(conversationHistory)
                    .user(templatedQuestion)
                    .call()
                    .content();

            cakeAIRepository.save(CakeAI.builder()
                    .question(question)
                    .answer(aiResponseContent)
                    .category("chat")
                    .sessionId(currentSessionId)
                    .build());

            return aiResponseContent;
        });
    }

    @Override
    public String recommendCakeOptions(String question, String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        cakeAIValidator.validateCommonAI(new AIRequestDTO(question, sessionId));

        return handleAIProcessing(() -> {
            List<CakeAI> chatLogs = cakeAIRepository.findBySessionIdOrderByRegDateAsc(currentSessionId);

            List<Message> conversationHistory = new ArrayList<>();
            for (CakeAI log : chatLogs) {
                conversationHistory.add(new UserMessage(log.getQuestion()));
                conversationHistory.add(new AssistantMessage(log.getAnswer()));
            }
            conversationHistory.add(new UserMessage(question));

            PromptTemplate template = new PromptTemplate(cakeOptionResource);
            String templatedQuestion = template.render(Map.of("question", question));

            String aiResponseContent = chatClient.prompt()
                    .messages(conversationHistory)
                    .user(templatedQuestion)
                    .call()
                    .content();

            cakeAIRepository.save(CakeAI.builder()
                    .question(question)
                    .answer(aiResponseContent)
                    .category("recommend_options")
                    .sessionId(currentSessionId)
                    .build());

            return aiResponseContent;
        });
    }

    @Override
    public String recommendCakeLettering(String question, String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        cakeAIValidator.validateCommonAI(new AIRequestDTO(question, sessionId));

        return handleAIProcessing(() -> {
            List<CakeAI> chatLogs = cakeAIRepository.findBySessionIdOrderByRegDateAsc(currentSessionId);

            List<Message> conversationHistory = new ArrayList<>();
            for (CakeAI log : chatLogs) {
                conversationHistory.add(new UserMessage(log.getQuestion()));
                conversationHistory.add(new AssistantMessage(log.getAnswer()));
            }
            conversationHistory.add(new UserMessage(question));

            PromptTemplate template = new PromptTemplate(cakeLetteringPromptResource);
            String templatedQuestion = template.render(Map.of("question", question));

            String aiResponseContent = chatClient.prompt()
                    .messages(conversationHistory)
                    .user(templatedQuestion)
                    .call()
                    .content();

            cakeAIRepository.save(CakeAI.builder()
                    .question(question)
                    .answer(aiResponseContent)
                    .category("recommend_lettering")
                    .sessionId(currentSessionId)
                    .build());

            return aiResponseContent;
        });
    }

    @Override
    public String recommendCakeDesign(String question, String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        cakeAIValidator.validateCommonAI(new AIRequestDTO(question, sessionId));

        return handleAIProcessing(() -> {
            PromptTemplate template = new PromptTemplate(cakeDesignPromptResource);
            var prompt = template.create(Map.of("question", question));

            ImageResponse response = imageModel.call(
                    new ImagePrompt(prompt.getContents(),
                            OpenAiImageOptions.builder()
                                    .withN(1)
                                    .withHeight(1024)
                                    .withWidth(1024)
                                    .withQuality("hd")
                                    .build()
                    ));

            String imageUrl = response.getResult().getOutput().getUrl();

            cakeAIRepository.save(CakeAI.builder()
                    .question(question)
                    .answer(imageUrl)
                    .category("recommend_image")
                    .sessionId(currentSessionId)
                    .build());

            return imageUrl;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CakeAI> getChatHistory(String sessionId) {
        return cakeAIRepository.findBySessionIdOrderByRegDateAsc(sessionId);
    }
}
