package vn.rikkei.exam.trainingroom.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestDocumentService {

    private final VectorStore vectorStore;

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(
            ".pdf",
            ".txt",
            ".md"
    );
    public String ingestDocument() {
        log.info("Starting document ingestion...");
        List<Path> files = findDocuments();
        if (files.isEmpty()) {
            return "Không tìm thấy tài liệu trong thư mục documents";
        }
        TokenTextSplitter splitter = new TokenTextSplitter();
        int totalChunks = 0;
        int skippedFiles = 0;
        int successFiles = 0;
        for (Path path : files) {
            String fileName = path.getFileName().toString();
            try {
                log.info("Checking document: {}", fileName);
                if (isAlreadyIndexed(fileName)) {
                    log.info(
                            "Document already indexed, skip: {}",
                            fileName
                    );
                    skippedFiles++;
                    continue;
                }
                log.info("Processing document: {}", fileName);
                Resource resource =
                        new FileSystemResource(path.toFile());
                TikaDocumentReader reader =
                        new TikaDocumentReader(resource);
                List<Document> documents = reader.get();
                documents.forEach(document ->
                        document.getMetadata().put(
                                "source",
                                fileName
                        )
                );
                List<Document> chunks =
                        splitter.apply(documents);
                if (chunks.isEmpty()) {
                    log.warn(
                            "No chunks generated from {}",
                            fileName
                    );
                    continue;
                }
                vectorStore.add(chunks);
                totalChunks += chunks.size();
                successFiles++;
                log.info(
                        "Indexed {} chunks from {}",
                        chunks.size(),
                        fileName
                );
            } catch (Exception e) {
                log.error(
                        "Failed to ingest {}",
                        fileName,
                        e
                );
            }
        }

        return "Ingest hoàn tất: "
                + successFiles
                + " files mới, "
                + skippedFiles
                + " files đã tồn tại, "
                + totalChunks
                + " chunks mới";
    }

    private boolean isAlreadyIndexed(String fileName) {
        try {
            SearchRequest request =
                    SearchRequest.builder()
                            .query(fileName)
                            .topK(1)
                            .similarityThreshold(0.0)
                            .build();
            List<Document> results =
                    vectorStore.similaritySearch(request);
            if (results == null || results.isEmpty()) {
                return false;
            }
            return results.stream()
                    .anyMatch(document ->
                            fileName.equals(
                                    document.getMetadata()
                                            .get("source")
                            )
                    );
        } catch (Exception e) {
            log.warn(
                    "Cannot check existing document: {}",
                    fileName,
                    e
            );
            return false;
        }
    }

    private List<Path> findDocuments() {
        List<Path> files = new ArrayList<>();
        List<String> directories = List.of(
                "./documents",
                "./data/documents",
                "./src/main/resources/documents"
        );
        for (String directory : directories) {
            Path path = Paths.get(directory);
            if (!Files.exists(path)
                    || !Files.isDirectory(path)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(path)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(this::isSupportedFile)
                        .forEach(files::add);
            } catch (Exception e) {
                log.error(
                        "Cannot scan directory {}",
                        directory,
                        e
                );
            }
        }
        return files;
    }

    private boolean isSupportedFile(Path path) {
        String fileName =
                path.getFileName()
                        .toString()
                        .toLowerCase();
        return SUPPORTED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
    }
}