package vn.rikkei.exam.trainingroom.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.rikkei.exam.trainingroom.dto.ChatRequest;
import vn.rikkei.exam.trainingroom.dto.ChatResponse;
import vn.rikkei.exam.trainingroom.service.rag.ChatWithRAGAndToolService;
import vn.rikkei.exam.trainingroom.service.rag.IngestDocumentService;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatWithRAGAndToolService chatService;
    private final IngestDocumentService ingestDocumentService;

    @PostMapping("/ingest")
    public String ingest() {
        return ingestDocumentService.ingestDocument();
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(
                request.getMessage(),
                request.getConversationId()
        );
    }

    @GetMapping
    public String chatGet(@RequestParam @Valid String conversationId,@RequestParam @Valid String message) {
        return chatService.chatRAGTool(message,conversationId);
    }
}