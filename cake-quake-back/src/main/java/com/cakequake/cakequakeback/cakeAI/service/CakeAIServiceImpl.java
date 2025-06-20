package com.cakequake.cakequakeback.cakeAI.service;

import com.cakequake.cakequakeback.cakeAI.DTO.AIRequestDTO;
import com.cakequake.cakequakeback.cakeAI.validator.CakeAIValidator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.cakequake.cakequakeback.cakeAI.util.AIUtils.handleAIProcessing;

@Service
public class CakeAIServiceImpl implements CakeAIService {

    ChatClient chatClient;
    ImageModel imageModel;
    CakeAIValidator cakeAIValidator;

    @Value("classpath:/prompts/cake-options.st")
    private Resource cakePromptResource;
    @Value("classpath:/prompts/cake-lettering.st")
    private Resource cakeLetteringPromptResource;

    public CakeAIServiceImpl(ImageModel imageModel, ChatClient.Builder builder, CakeAIValidator cakeAIValidator) {

        this.imageModel = imageModel;

        chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))    // 대화 저장소
                .build();

        this.cakeAIValidator = cakeAIValidator;
    }

    @Override
    // 간단한 질의응답
    public String generateAnswer(String question) {

        cakeAIValidator.validateCommonAI(new AIRequestDTO(question));

        return handleAIProcessing(() ->
                chatClient.prompt(question).call().content()
        );
    }

    @Override
    // 케이크 옵션 추천
    public String recommendCakeOptions(String question) {

        cakeAIValidator.validateCommonAI(new AIRequestDTO(question));

        return handleAIProcessing(() -> {
            PromptTemplate template = new PromptTemplate(cakePromptResource);
            Prompt prompt = template.create(Map.of("question", question));

            return chatClient.prompt(prompt).call().content();
        });
    }

    @Override
    // 케이크 문구 추천
    public String recommendCakeLettering(String question) {

        cakeAIValidator.validateCommonAI(new AIRequestDTO(question));

        return handleAIProcessing(() -> {
            PromptTemplate template = new PromptTemplate(cakeLetteringPromptResource);
            Prompt prompt = template.create(Map.of("question", question));

            return chatClient.prompt(prompt).call().content();
        });
    }

    @Override
    // 케이크 디자인 추천
    public String recommendCakeDesign(String question) {

        cakeAIValidator.validateCommonAI(new AIRequestDTO(question));

        return handleAIProcessing(() -> {
            // 현실적이고 실제 제작 가능한 케이크 디자인을 요청하는 문장 추가
            String promptText = question +
                    ", realistic cake design suitable for bakery, simple and elegant, " +
                    "no fantasy elements, no surreal or abstract styles, " +
                    "natural colors, edible decorations only, photographed in natural lighting, " +
                    "professional bakery photo style, no cartoon or CGI effects";

            ImageResponse response = imageModel.call(
                    new ImagePrompt(promptText,
                            OpenAiImageOptions.builder()
                                    .withN(1)    // 이미지 갯수. 1 ~ 10 사이 지정,  DALL-E-3는 1개만 지원됨.  유료는 여러개 지정 가능.
                                    .withHeight(1024)
                                    .withWidth(1024)
                                    .withQuality("hd")
                                    .build()
                    ));

            return response.getResult().getOutput().getUrl();
        });
    }


}
