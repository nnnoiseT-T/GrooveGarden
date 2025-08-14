package com.groovegarden.ui;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import com.groovegarden.model.GridModel;
import com.groovegarden.config.AppConfig;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Optimized grid update manager to reduce unnecessary redraws
 */
public class GridUpdateManager {
    private static final Logger LOGGER = Logger.getLogger(GridUpdateManager.class.getName());
    
    private final GridModel gridModel;
    private final StackPane[][] cells;
    private final ScheduledExecutorService updateExecutor;
    private final ConcurrentLinkedQueue<UpdateRequest> updateQueue;
    
    private volatile boolean isUpdating = false;
    private volatile long lastUpdateTime = 0;
    private final long minUpdateInterval;
    
    public GridUpdateManager(GridModel gridModel, StackPane[][] cells) {
        this.gridModel = gridModel;
        this.cells = cells;
        this.updateExecutor = Executors.newSingleThreadScheduledExecutor();
        this.updateQueue = new ConcurrentLinkedQueue<>();
        this.minUpdateInterval = AppConfig.getInt("performance.update.interval");
        
        // Start the update scheduler
        startUpdateScheduler();
    }
    
    /**
     * Request a grid update
     */
    public void requestUpdate() {
        updateQueue.offer(new UpdateRequest(System.currentTimeMillis()));
    }
    
    /**
     * Request a specific cell update
     */
    public void requestCellUpdate(int row, int col) {
        updateQueue.offer(new UpdateRequest(System.currentTimeMillis(), row, col));
    }
    
    /**
     * Request a full grid update
     */
    public void requestFullUpdate() {
        updateQueue.offer(new UpdateRequest(System.currentTimeMillis(), true));
    }
    
    private void startUpdateScheduler() {
        updateExecutor.scheduleAtFixedRate(() -> {
            try {
                processUpdates();
            } catch (Exception e) {
                LOGGER.warning("Error in grid update scheduler: " + e.getMessage());
            }
        }, 0, minUpdateInterval, TimeUnit.MILLISECONDS);
    }
    
    private void processUpdates() {
        if (isUpdating || updateQueue.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < minUpdateInterval) {
            return;
        }
        
        isUpdating = true;
        try {
            UpdateRequest request = updateQueue.poll();
            if (request != null) {
                if (request.isFullUpdate) {
                    performFullUpdate();
                } else if (request.hasSpecificCell) {
                    performCellUpdate(request.row, request.col);
                } else {
                    performBatchUpdate();
                }
                lastUpdateTime = currentTime;
            }
        } finally {
            isUpdating = false;
        }
    }
    
    private void performFullUpdate() {
        Platform.runLater(() -> {
            for (int row = 0; row < cells.length; row++) {
                for (int col = 0; col < cells[0].length; col++) {
                    updateCellDisplay(row, col);
                }
            }
        });
    }
    
    private void performCellUpdate(int row, int col) {
        Platform.runLater(() -> updateCellDisplay(row, col));
    }
    
    private void performBatchUpdate() {
        // Process multiple updates in batch
        final int maxBatchSize = 16; // Process up to 16 cells at once
        
        Platform.runLater(() -> {
            int batchSize = 0;
            while (!updateQueue.isEmpty() && batchSize < maxBatchSize) {
                UpdateRequest request = updateQueue.poll();
                if (request != null && request.hasSpecificCell) {
                    updateCellDisplay(request.row, request.col);
                    batchSize++;
                }
            }
        });
    }
    
    private void updateCellDisplay(int row, int col) {
        if (row < 0 || row >= cells.length || col < 0 || col >= cells[0].length) {
            return;
        }
        
        StackPane cell = cells[row][col];
        if (cell == null) return;
        
        // Update cell appearance based on grid model state
        boolean isActive = gridModel.isCellActive(row, col);
        int layer = gridModel.getCellLayer(row, col);
        
        // Apply visual updates
        updateCellVisuals(cell, isActive, layer);
    }
    
    private void updateCellVisuals(StackPane cell, boolean isActive, int layer) {
        // This method will be implemented to update the visual appearance
        // of cells based on their state
        // For now, we'll just mark that an update is needed
    }
    
    /**
     * Shutdown the update manager
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
    }
    
    private static class UpdateRequest {
        final long timestamp;
        final boolean isFullUpdate;
        final boolean hasSpecificCell;
        final int row;
        final int col;
        
        UpdateRequest(long timestamp) {
            this.timestamp = timestamp;
            this.isFullUpdate = false;
            this.hasSpecificCell = false;
            this.row = -1;
            this.col = -1;
        }
        
        UpdateRequest(long timestamp, int row, int col) {
            this.timestamp = timestamp;
            this.isFullUpdate = false;
            this.hasSpecificCell = true;
            this.row = row;
            this.col = col;
        }
        
        UpdateRequest(long timestamp, boolean isFullUpdate) {
            this.timestamp = timestamp;
            this.isFullUpdate = isFullUpdate;
            this.hasSpecificCell = false;
            this.row = -1;
            this.col = -1;
        }
    }
} 