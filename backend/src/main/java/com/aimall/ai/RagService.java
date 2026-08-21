package com.aimall.ai;

import com.aimall.entity.KnowledgeChunk;
import com.aimall.entity.KnowledgeDoc;
import com.aimall.mapper.KnowledgeChunkMapper;
import com.aimall.mapper.KnowledgeDocMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RagService {
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final VectorStore vectorStore;
    private final DeepSeekClient deepSeekClient;

    public RagService(KnowledgeDocMapper knowledgeDocMapper, KnowledgeChunkMapper knowledgeChunkMapper,
                      VectorStore vectorStore, DeepSeekClient deepSeekClient) {
        this.knowledgeDocMapper = knowledgeDocMapper;
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.vectorStore = vectorStore;
        this.deepSeekClient = deepSeekClient;
    }

    public String buildContext(String question, Long productId, int topK) {
        StringBuilder sb = new StringBuilder();
        Set<Long> allowedDocIds = null;
        if (productId != null) {
            allowedDocIds = new LinkedHashSet<>();
            for (KnowledgeDoc doc : knowledgeDocMapper.findAll()) {
                if (doc.getProductId() == null || productId.equals(doc.getProductId())) allowedDocIds.add(doc.getId());
            }
        }
        List<KnowledgeChunk> chunks = vectorStore.search(question, topK, allowedDocIds);
        Set<Long> includedDocIds = new LinkedHashSet<>();
        for (KnowledgeChunk chunk : chunks) {
            KnowledgeDoc doc = knowledgeDocMapper.findById(chunk.getDocId());
            if (doc == null) continue;
            includedDocIds.add(doc.getId());
            sb.append("[").append(doc.getTitle()).append("] ").append(chunk.getContent()).append("\n");
        }
        // Newly added documents remain usable before their embedding index is rebuilt.
        if (productId != null) {
            for (KnowledgeDoc doc : knowledgeDocMapper.findByProductId(productId)) {
                if (!includedDocIds.contains(doc.getId()) && doc.getContent() != null && !doc.getContent().isBlank()) {
                    String content = doc.getContent();
                    sb.append("[").append(doc.getTitle()).append("] ")
                            .append(content, 0, Math.min(content.length(), 1200)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void reindexAll() {
        List<KnowledgeDoc> docs = knowledgeDocMapper.findAll();
        for (KnowledgeDoc doc : docs) {
            knowledgeChunkMapper.deleteByDocId(doc.getId());
            String content = doc.getContent();
            if (content == null || content.isBlank()) continue;
            int step = 200;
            for (int i = 0; i < content.length(); i += step) {
                int end = Math.min(i + step, content.length());
                String slice = content.substring(i, end);
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocId(doc.getId());
                chunk.setContent(slice);
                chunk.setEmbeddingJson(DeepSeekClient.toJson(toList(vectorStoreEmbed(doc.getTitle() + " " + slice))));
                knowledgeChunkMapper.insert(chunk);
            }
        }
    }

    private double[] vectorStoreEmbed(String text) { return deepSeekClient.embed(text); }

    private List<Double> toList(double[] arr) {
        List<Double> list = new java.util.ArrayList<>();
        for (double v : arr) list.add(v);
        return list;
    }
}
