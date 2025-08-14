package com.groovegarden.score;

import com.groovegarden.music.Scale;
import com.groovegarden.util.Entropy;
import com.groovegarden.util.Similarity;
import com.groovegarden.config.AppConfig;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Optimized scoring engine with performance improvements
 */
public class ScoreEngine {
    private static final Logger LOGGER = Logger.getLogger(ScoreEngine.class.getName());
    
    private final ProgressBar diversityBar;
    private final ProgressBar flowBar;
    private final ProgressBar harmonyBar;
    private final Label diversityLabel;
    private final Label flowLabel;
    private final Label harmonyLabel;
    
    private final List<Double> recentPitches = new ArrayList<>();
    private final List<Double> recentRhythms = new ArrayList<>();
    private final List<double[]> recentBars = new ArrayList<>();
    
    // Performance optimization: batch updates and caching
    private final ScheduledExecutorService updateExecutor;
    private volatile boolean isUpdating = false;
    private volatile long lastUpdateTime = 0;
    private final long updateInterval;
    
    // Cached scores to avoid recalculation
    private volatile double cachedDiversity = 0.0;
    private volatile double cachedFlow = 0.0;
    private volatile double cachedHarmony = 0.0;
    private volatile long lastScoreCalculation = 0;
    private final long scoreCacheTimeout;
    
    private static final int MAX_HISTORY = 100;
    private static final int BAR_LENGTH = 16;
    
    public ScoreEngine(ProgressBar diversityBar, ProgressBar flowBar, ProgressBar harmonyBar,
                      Label diversityLabel, Label flowLabel, Label harmonyLabel) {
        this.diversityBar = diversityBar;
        this.flowBar = flowBar;
        this.harmonyBar = harmonyBar;
        this.diversityLabel = diversityLabel;
        this.harmonyLabel = harmonyLabel;
        this.flowLabel = flowLabel;
        
        // Initialize performance settings from config
        this.updateInterval = AppConfig.getInt("performance.score.update.interval");
        this.scoreCacheTimeout = updateInterval * 2; // Cache for 2 update cycles
        
        // Create update executor for batched updates
        this.updateExecutor = Executors.newSingleThreadScheduledExecutor();
        startUpdateScheduler();
        
        LOGGER.info("ScoreEngine initialized with update interval: " + updateInterval + "ms");
    }
    
    /**
     * Start the update scheduler
     */
    private void startUpdateScheduler() {
        updateExecutor.scheduleAtFixedRate(() -> {
            try {
                if (shouldUpdateScores()) {
                    updateScores();
                }
            } catch (Exception e) {
                LOGGER.warning("Error in score update scheduler: " + e.getMessage());
            }
        }, 0, updateInterval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Check if scores should be updated
     */
    private boolean shouldUpdateScores() {
        long currentTime = System.currentTimeMillis();
        return !isUpdating && 
               (currentTime - lastUpdateTime >= updateInterval) &&
               (currentTime - lastScoreCalculation >= scoreCacheTimeout);
    }
    
    public void addPitchEvent(double pitch) {
        synchronized (recentPitches) {
            recentPitches.add(pitch);
            if (recentPitches.size() > MAX_HISTORY) {
                recentPitches.remove(0);
            }
        }
    }
    
    public void addRhythmEvent(double rhythm) {
        synchronized (recentRhythms) {
            recentRhythms.add(rhythm);
            if (recentRhythms.size() > MAX_HISTORY) {
                recentRhythms.remove(0);
            }
        }
    }
    
    public void addBar(double[] bar) {
        if (bar.length == BAR_LENGTH) {
            synchronized (recentBars) {
                recentBars.add(bar);
                if (recentBars.size() > 10) {
                    recentBars.remove(0);
                }
            }
        }
    }
    
    /**
     * Update scores with performance optimization
     */
    public void updateScores() {
        if (isUpdating) return;
        
        isUpdating = true;
        try {
            // Calculate scores in background thread
            double diversity = calculateDiversity();
            double flow = calculateFlow();
            double harmony = calculateHarmony();
            
            // Cache the results
            cachedDiversity = diversity;
            cachedFlow = flow;
            cachedHarmony = harmony;
            lastScoreCalculation = System.currentTimeMillis();
            
            // Update UI on JavaFX thread
            Platform.runLater(() -> updateUI(diversity, flow, harmony));
            
            lastUpdateTime = System.currentTimeMillis();
            
        } finally {
            isUpdating = false;
        }
    }
    
    /**
     * Update UI elements
     */
    private void updateUI(double diversity, double flow, double harmony) {
        try {
            diversityBar.setProgress(diversity / 100.0);
            flowBar.setProgress(flow / 100.0);
            harmonyBar.setProgress(harmony / 100.0);
            
            diversityLabel.setText(String.format("%.0f%%", diversity));
            flowLabel.setText(String.format("%.0f%%", flow));
            harmonyLabel.setText(String.format("%.0f%%", harmony));
        } catch (Exception e) {
            LOGGER.warning("Error updating UI: " + e.getMessage());
        }
    }
    
    /**
     * Get cached diversity score
     */
    public double getCachedDiversity() {
        return cachedDiversity;
    }
    
    /**
     * Get cached flow score
     */
    public double getCachedFlow() {
        return cachedFlow;
    }
    
    /**
     * Get cached harmony score
     */
    public double getCachedHarmony() {
        return cachedHarmony;
    }
    
    private double calculateDiversity() {
        List<Double> pitches, rhythms;
        synchronized (recentPitches) {
            pitches = new ArrayList<>(recentPitches);
        }
        synchronized (recentRhythms) {
            rhythms = new ArrayList<>(recentRhythms);
        }
        
        if (pitches.isEmpty() || rhythms.isEmpty()) {
            return 0.0;
        }
        
        double pitchEntropy = Entropy.calculateEntropy(pitches);
        double rhythmEntropy = Entropy.calculateEntropy(rhythms);
        
        // Normalize to 0-100 scale
        return Math.min(100.0, (pitchEntropy + rhythmEntropy) * 50.0);
    }
    
    private double calculateFlow() {
        List<double[]> bars;
        synchronized (recentBars) {
            bars = new ArrayList<>(recentBars);
        }
        
        if (bars.size() < 2) {
            return 0.0;
        }
        
        double[] currentBar = bars.get(bars.size() - 1);
        double[] previousBar = bars.get(bars.size() - 2);
        
        // Convert double arrays to Integer lists for similarity calculation
        List<Integer> currentBarList = new ArrayList<>();
        List<Integer> previousBarList = new ArrayList<>();
        
        for (double value : currentBar) {
            currentBarList.add((int) value);
        }
        for (double value : previousBar) {
            previousBarList.add((int) value);
        }
        
        double similarity = Similarity.calculateSimilarity(currentBarList, previousBarList);
        
        // Moderate similarity gets best score (around 0.3-0.7)
        double optimalSimilarity = 0.5;
        double distance = Math.abs(similarity - optimalSimilarity);
        
        // Convert to 0-100 scale where optimal similarity = 100
        return Math.max(0.0, 100.0 - distance * 200.0);
    }
    
    private double calculateHarmony() {
        List<Double> pitches;
        synchronized (recentPitches) {
            pitches = new ArrayList<>(recentPitches);
        }
        
        if (pitches.isEmpty()) {
            return 0.0;
        }
        
        Scale currentScale = Scale.getScale("C Dorian"); // Default scale
        int inScaleCount = 0;
        int totalCount = pitches.size();
        
        for (double pitch : pitches) {
            if (currentScale.isInScale((int) pitch)) {
                inScaleCount++;
            }
        }
        
        double inScalePercentage = (double) inScaleCount / totalCount * 100.0;
        
        // Small bonus for non-chord tones (adds variety)
        double nonChordBonus = Math.min(10.0, (100.0 - inScalePercentage) * 0.2);
        
        return Math.min(100.0, inScalePercentage + nonChordBonus);
    }
    
    /**
     * Shutdown the score engine
     */
    public void shutdown() {
        updateExecutor.shutdown();
        try {
            if (!updateExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                updateExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            updateExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("ScoreEngine shutdown complete");
    }
} 