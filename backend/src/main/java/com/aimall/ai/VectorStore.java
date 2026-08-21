package com.aimall.ai;

import cn.hutool.json.JSONUtil;
import com.aimall.entity.KnowledgeChunk;
import com.aimall.mapper.KnowledgeChunkMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.Arrays;

@Service
public class VectorStore {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final DeepSeekClient deepSeekClient;

    public VectorStore(KnowledgeChunkMapper knowledgeChunkMapper, DeepSeekClient deepSeekClient) {
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.deepSeekClient = deepSeekClient;
    }

    public List<KnowledgeChunk> search(String query, int topK) {
        return search(query, topK, null);
    }

    public List<KnowledgeChunk> search(String query, int topK, Set<Long> allowedDocIds) {
        double[] queryVec = deepSeekClient.embed(query);
        List<KnowledgeChunk> chunks = knowledgeChunkMapper.findAllChunks();
        String normalizedQuery = normalize(query);
        Set<String> queryTokens = tokens(normalizedQuery);
        return chunks.stream()
                .filter(chunk -> allowedDocIds == null || allowedDocIds.contains(chunk.getDocId()))
                .map(chunk -> {
                    double vectorScore = cosine(queryVec, parseVector(chunk.getEmbeddingJson()));
                    double lexicalScore = lexical(queryTokens, chunk.getContent());
                    // Hybrid retrieval improves exact product/specification matches while preserving semantic recall.
                    double score = vectorScore * 0.78 + lexicalScore * 0.22;
                    chunk.setEmbeddingJson(null);
                    return new ScoredChunk(chunk, score, vectorScore, lexicalScore);
                })
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(Math.max(topK * 3, topK))
                .sorted(Comparator.comparingDouble(ScoredChunk::rerankScore).reversed())
                .limit(topK)
                .map(ScoredChunk::chunk)
                .toList();
    }

    private double lexical(Set<String> queryTokens, String content) {
        if (queryTokens.isEmpty() || content == null) return 0;
        Set<String> contentTokens = tokens(normalize(content));
        long overlap = queryTokens.stream().filter(contentTokens::contains).count();
        return (double) overlap / queryTokens.size();
    }

    private Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        if (text == null || text.isBlank()) return result;
        result.addAll(Arrays.asList(text.split("\\s+")));
        for (int i = 0; i + 1 < text.length(); i++) {
            char a = text.charAt(i), b = text.charAt(i + 1);
            if (isCjk(a) && isCjk(b)) result.add("" + a + b);
        }
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", " ").trim();
    }

    private boolean isCjk(char c) { return c >= '\u4e00' && c <= '\u9fa5'; }

    private double[] parseVector(String json) {
        if (json == null || json.isBlank()) return new double[256];
        try {
            List<Double> list = JSONUtil.toList(json, Double.class);
            double[] arr = new double[list.size()];
            for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
            return arr;
        } catch (Exception e) {
            return new double[256];
        }
    }

    private double cosine(double[] a, double[] b) {
        int len = Math.min(a.length, b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private record ScoredChunk(KnowledgeChunk chunk, double score, double vectorScore, double lexicalScore) {
        double rerankScore() { return score + lexicalScore * 0.08; }
    }
}
