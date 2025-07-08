// CakeAIController.java
package com.cakequake.cakequakeback.cakeAI.controller;

import com.cakequake.cakequakeback.cakeAI.entities.CakeAI;
import com.cakequake.cakequakeback.cakeAI.service.CakeAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class CakeAIController {

    private final CakeAIService cakeAIService;

    @PostMapping("/chat")
    @PreAuthorize("hasRole('BUYER')")
    public String generateAnswer(@RequestParam String question, @RequestParam(required = false) String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        return cakeAIService.generateAnswer(question, currentSessionId);
    }

    @PostMapping("/chat/options")
    @PreAuthorize("hasRole('BUYER')")
    public String recommendCakeOptions(@RequestParam String question, @RequestParam(required = false) String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        return cakeAIService.recommendCakeOptions(question, currentSessionId);
    }

    @PostMapping("/chat/lettering")
    @PreAuthorize("hasRole('BUYER')")
    public String recommendCakeLettering(@RequestParam String question, @RequestParam(required = false) String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        return cakeAIService.recommendCakeLettering(question, currentSessionId);
    }

    @PostMapping("/chat/design")
    @PreAuthorize("hasRole('BUYER')")
    public String recommendCakeDesign(@RequestParam String question, @RequestParam(required = false) String sessionId) {
        String currentSessionId = (sessionId == null || sessionId.isEmpty()) ? UUID.randomUUID().toString() : sessionId;
        return cakeAIService.recommendCakeDesign(question, currentSessionId);
    }

    @GetMapping("/chat/history")
    @PreAuthorize("hasRole('BUYER')")
    public List<CakeAI> getChatHistory(@RequestParam String sessionId) {
        return cakeAIService.getChatHistory(sessionId);
    }
}
