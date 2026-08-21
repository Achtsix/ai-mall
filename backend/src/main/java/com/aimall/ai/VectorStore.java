package com.aimall.ai;

import cn.hutool.json.JSONUtil;
import com.aimall.entity.KnowledgeChunk;
import com.aimall.mapper.KnowledgeChunkMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        return chunks.stream()
                .filter(chunk -> allowedDocIds == null || allowedDocIds.contains(chunk.getDocId()))
                .map(chunk -> {
                    double score = cosine(queryVec, parseVector(chunk.getEmbeddingJson()));
                    chunk.setEmbeddingJson(null);
                    return new ScoredChunk(chunk, score);
                })
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .map(ScoredChunk::chunk)
                .toList();
    }

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

    private record ScoredChunk(KnowledgeChunk chunk, double score) {}
}
