package com.proj1.ailoganlzr.AILayer.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeBaseLoader {
    public List<Document> initializeKnowledgeBase() {

        List<Document> documents = new ArrayList<>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:Knowledge-base/*.md");
            for (Resource resource : resources) {

                TextReader reader = new TextReader(resource);

                documents.addAll(reader.get());

            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load knowledge base.", e);
        }

        return documents;
    }
}
