package com.aimall.ai;

import com.aimall.entity.KnowledgeChunk;
import com.aimall.entity.KnowledgeDoc;
import com.aimall.mapper.KnowledgeChunkMapper;
import com.aimall.mapper.KnowledgeDocMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

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
            for (String slice : splitSemantic(content)) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocId(doc.getId());
                chunk.setContent(slice);
                chunk.setEmbeddingJson(DeepSeekClient.toJson(toList(vectorStoreEmbed(doc.getTitle() + " " + slice))));
                knowledgeChunkMapper.insert(chunk);
            }
        }
    }

    /** Split by headings/paragraphs/sentences, retaining a small overlap for follow-up questions. */
    private List<String> splitSemantic(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        List<String> units = new ArrayList<>();
        for (String paragraph : normalized.split("\\n\\s*\\n|(?=^#{1,6}\\s)|(?=^问：)|(?=^Q[:：])", -1)) {
            String part = paragraph.trim();
            if (part.isBlank()) continue;
            if (part.length() <= 420) {
                units.add(part);
                continue;
            }
            for (String sentence : part.split("(?<=[。！？!?；;])\\s*")) {
                String s = sentence.trim();
                if (!s.isBlank()) units.add(s);
            }
        }
        List<String> chunks = new ArrayList<>();
        String previous = "";
        StringBuilder current = new StringBuilder();
        for (String unit : units) {
            if (current.length() > 0 && current.length() + unit.length() + 1 > 420) {
                chunks.add(current.toString());
                previous = tail(current.toString(), 60);
                current.setLength(0);
                current.append(previous);
            }
            if (current.length() > 0) current.append(' ');
            current.append(unit);
        }
        if (current.length() > 0) chunks.add(current.toString());
        return chunks;
    }

    private String tail(String value, int length) {
        return value.substring(Math.max(0, value.length() - length)).trim();
    }

    private double[] vectorStoreEmbed(String text) { return deepSeekClient.embed(text); }

    private List<Double> toList(double[] arr) {
        List<Double> list = new java.util.ArrayList<>();
        for (double v : arr) list.add(v);
        return list;
    }
}
