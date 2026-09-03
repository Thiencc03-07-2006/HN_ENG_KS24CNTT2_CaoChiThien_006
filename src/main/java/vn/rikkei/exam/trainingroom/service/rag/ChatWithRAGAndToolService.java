package vn.rikkei.exam.trainingroom.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vn.rikkei.exam.trainingroom.dto.ChatResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatWithRAGAndToolService {

        private final VectorStore vectorStore;
        private final ChatClient chatClient;

        private static final String SYSTEM_PROMPT = """
                        Bạn là trợ lý AI nội bộ của doanh nghiệp.

                        Nhiệm vụ của bạn:

                        - Trả lời bằng tiếng Việt.
                        - Ưu tiên sử dụng CONTEXT từ tài liệu nội bộ.
                        - Khi cần dữ liệu nhân viên hoặc booking,
                          hãy tự động sử dụng TOOL phù hợp.
                        - Không được bịa thông tin.

                        Nếu không tìm thấy thông tin trong tài liệu hoặc tool,
                        hãy trả lời:

                        "Xin lỗi, thông tin nội bộ hiện chưa đủ để trả lời câu hỏi này."

                        CONTEXT:
                        {context}
                        """;

        public ChatResponse chat(String userMessage, String conversationId) {
                final String finalConversationId = (conversationId == null || conversationId.isBlank())
                                ? UUID.randomUUID().toString()
                                : conversationId;
                log.info(
                                "Chat request - conversationId={}, message={}",
                                finalConversationId,
                                userMessage);
                SearchRequest searchRequest = SearchRequest.builder()
                                .query(userMessage)
                                .topK(5)
                                .similarityThreshold(0.3)
                                .build();
                List<Document> documents = vectorStore.similaritySearch(searchRequest);
                String context = documents.stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n\n---\n\n"));
                List<String> sources = documents.stream()
                                .map(document -> String.valueOf(
                                                document.getMetadata()
                                                                .getOrDefault("source", "unknown")))
                                .distinct()
                                .toList();
                SystemPromptTemplate template = new SystemPromptTemplate(SYSTEM_PROMPT);
                Message systemMessage = template.createMessage(
                                Map.of("context", context));
                Prompt prompt = new Prompt(
                                systemMessage,
                                new UserMessage(userMessage));
                String answer = chatClient
                                .prompt(prompt)
                                .advisors(advisorSpec -> advisorSpec.param(
                                                "chat_memory_conversation_id",
                                                finalConversationId))
                                .call()
                                .content();
                log.info(
                                "Chat completed - conversationId={}",
                                finalConversationId);
                return ChatResponse.builder()
                                .conversationId(finalConversationId)
                                .answer(answer)
                                .sources(sources)
                                .build();
        }

        public String chatRAGTool(String userMessage, String conversationId) {
                return chat(userMessage, conversationId).getAnswer();
        }
}