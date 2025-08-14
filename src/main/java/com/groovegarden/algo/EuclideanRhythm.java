package com.groovegarden.algo;

public class EuclideanRhythm {
    private int steps;
    private int pulses;
    
    public EuclideanRhythm(int steps, int pulses) {
        this.steps = steps;
        this.pulses = Math.min(pulses, steps);
    }
    
    public boolean[] generate() {
        if (pulses == 0) {
            return new boolean[steps];
        }
        
        if (pulses == steps) {
            boolean[] result = new boolean[steps];
            for (int i = 0; i < steps; i++) {
                result[i] = true;
            }
            return result;
        }
        
        // Bjorklund's algorithm
        int[] buckets = new int[steps];
        int bucketSize = steps / pulses;
        int remainder = steps % pulses;
        
        int currentBucket = 0;
        for (int i = 0; i < pulses && currentBucket < steps; i++) {
            int size = bucketSize + (i < remainder ? 1 : 0);
            for (int j = 0; j < size && currentBucket < steps; j++) {
                buckets[currentBucket++] = 1;
            }
            if (currentBucket < steps) {
                buckets[currentBucket++] = 0;
            }
        }
        
        // Convert to boolean array
        boolean[] result = new boolean[steps];
        for (int i = 0; i < steps; i++) {
            result[i] = (buckets[i] == 1);
        }
        
        return result;
    }
    
    public void setSteps(int steps) {
        this.steps = steps;
    }
    
    public void setPulses(int pulses) {
        this.pulses = Math.min(pulses, steps);
    }
    
    public int getSteps() {
        return steps;
    }
    
    public int getPulses() {
        return pulses;
    }
    
    public static boolean[] generate(int steps, int pulses) {
        EuclideanRhythm rhythm = new EuclideanRhythm(steps, pulses);
        return rhythm.generate();
    }
} 