package com.groovegarden.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Memory monitoring and management utility
 */
public class MemoryMonitor {
    private static final Logger LOGGER = Logger.getLogger(MemoryMonitor.class.getName());
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
    private static volatile boolean isMonitoring = false;
    
    // Memory thresholds
    private static final double WARNING_THRESHOLD = 0.8; // 80%
    private static final double CRITICAL_THRESHOLD = 0.9; // 90%
    
    // Memory usage history for leak detection
    private static final int HISTORY_SIZE = 100;
    private static final long[] memoryHistory = new long[HISTORY_SIZE];
    private static int historyIndex = 0;
    private static long lastGCTime = 0;
    
    /**
     * Start memory monitoring
     */
    public static void startMonitoring() {
        if (isMonitoring) return;
        
        isMonitoring = true;
        monitorExecutor.scheduleAtFixedRate(() -> {
            try {
                monitorMemory();
            } catch (Exception e) {
                LOGGER.warning("Error in memory monitoring: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS); // Check every 30 seconds
        
        LOGGER.info("Memory monitoring started");
    }
    
    /**
     * Stop memory monitoring
     */
    public static void stopMonitoring() {
        isMonitoring = false;
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Memory monitoring stopped");
    }
    
    /**
     * Get current memory usage statistics
     */
    public static MemoryStats getMemoryStats() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        return new MemoryStats(
            heapUsage.getUsed(),
            heapUsage.getMax(),
            nonHeapUsage.getUsed(),
            nonHeapUsage.getMax(),
            Runtime.getRuntime().totalMemory(),
            Runtime.getRuntime().freeMemory()
        );
    }
    
    /**
     * Perform garbage collection if needed
     */
    public static void performGCIfNeeded() {
        MemoryStats stats = getMemoryStats();
        double heapUsageRatio = (double) stats.heapUsed / stats.heapMax;
        
        if (heapUsageRatio > CRITICAL_THRESHOLD) {
            LOGGER.warning("Critical memory usage detected: " + 
                          String.format("%.1f%%", heapUsageRatio * 100));
            System.gc();
            lastGCTime = System.currentTimeMillis();
        } else if (heapUsageRatio > WARNING_THRESHOLD) {
            LOGGER.info("High memory usage detected: " + 
                       String.format("%.1f%%", heapUsageRatio * 100));
        }
    }
    
    /**
     * Check for potential memory leaks
     */
    public static boolean checkForMemoryLeak() {
        if (historyIndex < HISTORY_SIZE) {
            return false; // Not enough data yet
        }
        
        // Calculate trend over the last 50 measurements
        int startIndex = (historyIndex - 50 + HISTORY_SIZE) % HISTORY_SIZE;
        int endIndex = (historyIndex - 1 + HISTORY_SIZE) % HISTORY_SIZE;
        
        long startMemory = memoryHistory[startIndex];
        long endMemory = memoryHistory[endIndex];
        
        // If memory usage increased by more than 10% without GC, potential leak
        if (startMemory > 0 && endMemory > startMemory * 1.1) {
            long timeSinceGC = System.currentTimeMillis() - lastGCTime;
            if (timeSinceGC > 300000) { // 5 minutes
                LOGGER.warning("Potential memory leak detected. Memory increased by " +
                              String.format("%.1f%%", ((double)(endMemory - startMemory) / startMemory) * 100));
                return true;
            }
        }
        
        return false;
    }
    
    private static void monitorMemory() {
        MemoryStats stats = getMemoryStats();
        
        // Store memory usage in history
        memoryHistory[historyIndex] = stats.heapUsed;
        historyIndex = (historyIndex + 1) % HISTORY_SIZE;
        
        // Log memory usage periodically
        if (historyIndex % 10 == 0) { // Every 10 measurements (5 minutes)
            LOGGER.info(String.format("Memory usage - Heap: %.1f%% (%.1f MB), " +
                                    "Non-heap: %.1f%% (%.1f MB)",
                (double) stats.heapUsed / stats.heapMax * 100,
                stats.heapUsed / (1024.0 * 1024.0),
                (double) stats.nonHeapUsed / stats.nonHeapMax * 100,
                stats.nonHeapUsed / (1024.0 * 1024.0)));
        }
        
        // Check for memory issues
        performGCIfNeeded();
        checkForMemoryLeak();
    }
    
    /**
     * Memory statistics container
     */
    public static class MemoryStats {
        public final long heapUsed;
        public final long heapMax;
        public final long nonHeapUsed;
        public final long nonHeapMax;
        public final long totalMemory;
        public final long freeMemory;
        
        public MemoryStats(long heapUsed, long heapMax, long nonHeapUsed, long nonHeapMax,
                          long totalMemory, long freeMemory) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.nonHeapMax = nonHeapMax;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
        }
        
        public double getHeapUsageRatio() {
            return heapMax > 0 ? (double) heapUsed / heapMax : 0.0;
        }
        
        public double getNonHeapUsageRatio() {
            return nonHeapMax > 0 ? (double) nonHeapUsed / nonHeapMax : 0.0;
        }
        
        public long getAvailableMemory() {
            return totalMemory - heapUsed;
        }
    }
} 