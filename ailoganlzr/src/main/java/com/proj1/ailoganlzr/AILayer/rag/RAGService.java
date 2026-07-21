package com.proj1.ailoganlzr.AILayer.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;


@Slf4j
@Service
public class RAGService {

    private final KnowledgeBaseLoader knowledgeBaseLoader;

    private final TokenTextSplitter tokenTextSplitter;

    private final VectorStore vectorStore;

    public RAGService(KnowledgeBaseLoader knowledgeBaseLoader, TokenTextSplitter tokenTextSplitter, VectorStore vectorStore) {
        this.knowledgeBaseLoader = knowledgeBaseLoader;
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void initializeKnowledgeBase() {

        List<Document> documents =
                knowledgeBaseLoader.initializeKnowledgeBase();

        log.info("Knowledge Base Loaded Successfully.");
        log.info("Total Documents Loaded : {}", documents.size());

        List<Document> chunks = tokenTextSplitter.apply(documents);
        vectorStore.add(chunks);

        log.info("Total Chunks : {}", chunks.size());

        log.info("Successfully stored {} chunks into PGVector.", chunks.size());

    }
}
