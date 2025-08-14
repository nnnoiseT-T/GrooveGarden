package com.groovegarden.algo;

import java.util.*;

public class MarkovMelody {
    private int order;
    private Map<String, Map<Integer, Integer>> transitionMatrix;
    private Random random;
    private List<Integer> scaleDegrees;
    
    public MarkovMelody(int order) {
        this.order = order;
        this.transitionMatrix = new HashMap<>();
        this.random = new Random();
        this.scaleDegrees = new ArrayList<>();
    }
    
    public void setScaleDegrees(List<Integer> degrees) {
        this.scaleDegrees = new ArrayList<>(degrees);
        buildTransitionMatrix();
    }
    
    private void buildTransitionMatrix() {
        transitionMatrix.clear();
        
        if (scaleDegrees.size() < order + 1) return;
        
        // Build transition matrix from scale degrees
        for (int i = 0; i <= scaleDegrees.size() - order - 1; i++) {
            String context = buildContext(i);
            int nextDegree = scaleDegrees.get(i + order);
            
            transitionMatrix.computeIfAbsent(context, k -> new HashMap<>());
            Map<Integer, Integer> transitions = transitionMatrix.get(context);
            transitions.put(nextDegree, transitions.getOrDefault(nextDegree, 0) + 1);
        }
    }
    
    private String buildContext(int startIndex) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < order; i++) {
            context.append(scaleDegrees.get(startIndex + i));
            if (i < order - 1) context.append(",");
        }
        return context.toString();
    }
    
    public int generateNextNote(List<Integer> context) {
        if (context.size() < order) {
            // If context is too short, return random scale degree
            return scaleDegrees.get(random.nextInt(scaleDegrees.size()));
        }
        
        String contextStr = buildContextFromList(context);
        Map<Integer, Integer> transitions = transitionMatrix.get(contextStr);
        
        if (transitions == null || transitions.isEmpty()) {
            // If no transitions found, return random scale degree
            return scaleDegrees.get(random.nextInt(scaleDegrees.size()));
        }
        
        // Choose next note based on transition probabilities
        int totalWeight = transitions.values().stream().mapToInt(Integer::intValue).sum();
        int randomValue = random.nextInt(totalWeight);
        
        int cumulativeWeight = 0;
        for (Map.Entry<Integer, Integer> entry : transitions.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }
        
        // Fallback
        return scaleDegrees.get(random.nextInt(scaleDegrees.size()));
    }
    
    private String buildContextFromList(List<Integer> context) {
        StringBuilder contextStr = new StringBuilder();
        for (int i = 0; i < order; i++) {
            contextStr.append(context.get(context.size() - order + i));
            if (i < order - 1) contextStr.append(",");
        }
        return contextStr.toString();
    }
    
    public void setOrder(int order) {
        this.order = order;
        buildTransitionMatrix();
    }
    
    public int getOrder() {
        return order;
    }
    
    public List<Integer> getScaleDegrees() {
        return new ArrayList<>(scaleDegrees);
    }
} 