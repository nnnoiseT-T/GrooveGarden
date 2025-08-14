package com.groovegarden.ui;

import com.groovegarden.music.MidiEngine;
import com.groovegarden.music.MidiExporter;
import com.groovegarden.model.GridModel;
import com.groovegarden.score.ScoreEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    
    @FXML private ComboBox<String> scaleComboBox;
    @FXML private Spinner<Integer> tempoSpinner;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button exportButton;
    @FXML private Button resetButton;
    
    @FXML private ProgressBar diversityBar;
    @FXML private ProgressBar flowBar;
    @FXML private ProgressBar harmonyBar;
    @FXML private Label diversityLabel;
    @FXML private Label flowLabel;
    @FXML private Label harmonyLabel;
    
    @FXML private GridPane gridPane;
    
    private GridModel gridModel;
    private MidiEngine midiEngine;
    private ScoreEngine scoreEngine;
    private Timeline timeline;
    private boolean isPlaying = false;
    
    private StackPane[][] cells;
    private static final int GRID_SIZE = 8;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComponents();
        setupEventHandlers();
        setupTimeline();
        initializeGrid();
    }
    
    private void initializeComponents() {
        // Initialize scale combo box
        scaleComboBox.getItems().addAll("C Dorian", "C Ionian", "A Minor");
        scaleComboBox.setValue("C Dorian");
        
        // Initialize tempo spinner
        SpinnerValueFactory<Integer> tempoFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(60, 180, 120);
        tempoSpinner.setValueFactory(tempoFactory);
        tempoSpinner.setEditable(true);
        
        // Initialize models
        gridModel = new GridModel();
        midiEngine = new MidiEngine();
        scoreEngine = new ScoreEngine(diversityBar, flowBar, harmonyBar, 
                                    diversityLabel, flowLabel, harmonyLabel);
    }
    
    private void setupEventHandlers() {
        scaleComboBox.setOnAction(e -> handleScaleChange());
        tempoSpinner.valueProperty().addListener((obs, oldVal, newVal) -> handleTempoChange());
    }
    
    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(125), e -> updateMusic()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }
    
    private void initializeGrid() {
        cells = new StackPane[GRID_SIZE][GRID_SIZE];
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                StackPane cell = createCell(row, col);
                cells[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
    }
    
    private StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(50, 50);
        cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
        
        Rectangle indicator = new Rectangle(40, 40);
        indicator.setFill(Color.WHITE);
        indicator.setStroke(Color.TRANSPARENT);
        indicator.setArcWidth(6);
        indicator.setArcHeight(6);
        cell.getChildren().add(indicator);
        
        final int finalRow = row;
        final int finalCol = col;
        
        cell.setOnMouseClicked(event -> handleCellClick(event, finalRow, finalCol));
        
        // Add hover effect
        cell.setOnMouseEntered(e -> {
            if (!gridModel.isCellActive(finalRow, finalCol)) {
                cell.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #007bff; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;");
            }
        });
        
        cell.setOnMouseExited(e -> {
            if (!gridModel.isCellActive(finalRow, finalCol)) {
                cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
            }
        });
        
        return cell;
    }
    
    private void handleCellClick(MouseEvent event, int row, int col) {
        System.out.println("Cell clicked: row=" + row + ", col=" + col + ", button=" + event.getButton());
        
        if (event.getButton() == MouseButton.PRIMARY) {
            // Left click: toggle active state
            System.out.println("Left click - toggling cell");
            gridModel.toggleCell(row, col);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Right click: cycle through layers
            System.out.println("Right click - cycling layer");
            gridModel.cycleCellLayer(row, col);
        }
        
        updateGridDisplay();
    }
    
    private void updateGridDisplay() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                updateCellDisplay(row, col);
            }
        }
    }
    
    private void updateCellDisplay(int row, int col) {
        StackPane cell = cells[row][col];
        Rectangle indicator = (Rectangle) cell.getChildren().get(0);
        
        if (!gridModel.isCellActive(row, col)) {
            indicator.setFill(Color.WHITE);
            cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
            return;
        }
        
        int layer = gridModel.getCellLayer(row, col);
        switch (layer) {
            case 0: // Rhythm layer
                indicator.setFill(Color.web("#007bff"));
                cell.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #007bff; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;");
                break;
            case 1: // Melody layer
                indicator.setFill(Color.web("#28a745"));
                cell.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #28a745; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;");
                break;
            case 2: // Both layers
                indicator.setFill(Color.web("#dc3545"));
                cell.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dc3545; -fx-border-width: 2; -fx-background-radius: 4; -fx-border-radius: 4;");
                break;
            default:
                indicator.setFill(Color.WHITE);
                cell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");
        }
    }
    
    @FXML
    private void handleStart() {
        if (!isPlaying) {
            isPlaying = true;
            startButton.setDisable(true);
            stopButton.setDisable(false);
            
            // Calculate interval based on tempo (16th note duration)
            double intervalMs = 60000.0 / (tempoSpinner.getValue() * 4);
            timeline.stop();
            timeline = new Timeline(new KeyFrame(Duration.millis(intervalMs), e -> updateMusic()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
            
            midiEngine.start();
        }
    }
    
    @FXML
    private void handleStop() {
        if (isPlaying) {
            isPlaying = false;
            startButton.setDisable(false);
            stopButton.setDisable(true);
            
            timeline.stop();
            midiEngine.stop();
        }
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export MIDI");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("MIDI Files", "*.mid")
        );
        fileChooser.setInitialFileName("groove_garden_export.mid");
        
        File file = fileChooser.showSaveDialog(startButton.getScene().getWindow());
        if (file != null) {
            MidiExporter exporter = new MidiExporter();
            exporter.export(gridModel, midiEngine, file.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleReset() {
        // Clear the grid model
        gridModel.clear();
        // Update the display
        updateGridDisplay();
        System.out.println("Grid reset - all cells cleared");
    }
    
    private void handleScaleChange() {
        String selectedScale = scaleComboBox.getValue();
        midiEngine.setScale(selectedScale);
        gridModel.setScale(selectedScale);
    }
    
    private void handleTempoChange() {
        if (isPlaying) {
            double intervalMs = 60000.0 / (tempoSpinner.getValue() * 4);
            timeline.stop();
            timeline = new Timeline(new KeyFrame(Duration.millis(intervalMs), e -> updateMusic()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }
    
    private void updateMusic() {
        // Update grid state
        gridModel.update();
        
        // Generate music
        midiEngine.tick(gridModel);
        
        // Update scoring (now handled automatically by ScoreEngine)
        // Add some sample events for demonstration
        scoreEngine.addPitchEvent(Math.random() * 127);
        scoreEngine.addRhythmEvent(Math.random());
        
        // Update UI
        updateGridDisplay();
    }
    
    private void updateScoreDisplay() {
        // Scores are now updated automatically by ScoreEngine
        // This method is kept for compatibility but no longer needed
    }
} 