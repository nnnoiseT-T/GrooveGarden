package com.groovegarden.util;

import java.util.*;

public class Entropy {
    
    public static double calculateEntropy(List<?> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        
        // Count frequencies
        Map<Object, Integer> frequencies = new HashMap<>();
        for (Object item : data) {
            frequencies.put(item, frequencies.getOrDefault(item, 0) + 1);
        }
        
        // Calculate entropy
        double entropy = 0.0;
        int total = data.size();
        
        for (int frequency : frequencies.values()) {
            if (frequency > 0) {
                double probability = (double) frequency / total;
                entropy -= probability * Math.log(probability) / Math.log(2.0);
            }
        }
        
        return entropy;
    }
    
    public static double calculateEntropy(int[] data) {
        if (data == null || data.length == 0) {
            return 0.0;
        }
        
        List<Integer> list = new ArrayList<>();
        for (int value : data) {
            list.add(value);
        }
        
        return calculateEntropy(list);
    }
    
    public static double calculateEntropy(double[] data) {
        if (data == null || data.length == 0) {
            return 0.0;
        }
        
        List<Double> list = new ArrayList<>();
        for (double value : data) {
            list.add(value);
        }
        
        return calculateEntropy(list);
    }
} 