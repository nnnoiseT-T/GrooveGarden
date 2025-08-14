package com.groovegarden.util;

import java.util.*;

public class Similarity {
    
    public static double calculateSimilarity(List<Integer> list1, List<Integer> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return 0.0;
        }
        
        // Use the shorter list length
        int minLength = Math.min(list1.size(), list2.size());
        
        // Calculate cosine similarity
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < minLength; i++) {
            double val1 = list1.get(i);
            double val2 = list2.get(i);
            
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    public static double calculateSimilarity(int[] array1, int[] array2) {
        if (array1 == null || array2 == null || array1.length == 0 || array2.length == 0) {
            return 0.0;
        }
        
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        
        for (int value : array1) {
            list1.add(value);
        }
        
        for (int value : array2) {
            list2.add(value);
        }
        
        return calculateSimilarity(list1, list2);
    }
    
    public static double calculateAutoCorrelation(List<Integer> data, int lag) {
        if (data == null || data.size() < lag + 1) {
            return 0.0;
        }
        
        double mean = data.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = data.stream().mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0.0);
        
        if (variance == 0.0) {
            return 0.0;
        }
        
        double correlation = 0.0;
        for (int i = 0; i < data.size() - lag; i++) {
            correlation += (data.get(i) - mean) * (data.get(i + lag) - mean);
        }
        
        return correlation / ((data.size() - lag) * variance);
    }
    
    public static double calculateDTWSimilarity(List<Integer> sequence1, List<Integer> sequence2) {
        if (sequence1 == null || sequence2 == null || sequence1.isEmpty() || sequence2.isEmpty()) {
            return 0.0;
        }
        
        int n = sequence1.size();
        int m = sequence2.size();
        
        // Create distance matrix
        double[][] dtw = new double[n + 1][m + 1];
        
        // Initialize with infinity
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dtw[i], Double.POSITIVE_INFINITY);
        }
        
        dtw[0][0] = 0.0;
        
        // Fill the matrix
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.abs(sequence1.get(i - 1) - sequence2.get(j - 1));
                dtw[i][j] = cost + Math.min(Math.min(dtw[i - 1][j], dtw[i][j - 1]), dtw[i - 1][j - 1]);
            }
        }
        
        // Return normalized distance (lower is more similar)
        return 1.0 / (1.0 + dtw[n][m]);
    }
} 