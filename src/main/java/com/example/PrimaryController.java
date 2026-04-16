/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package com.example;

import com.example.Logic.ChartController;
import com.example.Logic.Process;
import com.example.Logic.SimulationManager;
import com.example.Logic.Algorithms.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import javafx.util.Duration;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {

    // Input Fields
    @FXML private Spinner<Integer> arrivalSpinner;
    @FXML private Spinner<Integer> burstSpinner;
    @FXML private Spinner<Integer> prioritySpinner;
    @FXML private ChoiceBox<String> algoChoiceBox;
    @FXML private ChoiceBox<String> modeChoiceBox;

    // Table
    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, Integer> idColumn;
    @FXML private TableColumn<Process, Integer> arrivalColumn;
    @FXML private TableColumn<Process, Integer> burstColumn;
    @FXML private TableColumn<Process, Integer> remainingTimeColumn;

    // MUST match the fx:id in FXML file exactly
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button addButton;
    @FXML private Button removeButton;

    // Canvas
    @FXML private Canvas ganttCanvas;

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private SimulationManager simulationManager = new SimulationManager();
    private ChartController chartController;
    private Timeline timer;
    private boolean isPaused = false;
    private boolean isRunning = false;

    private boolean isReadyToStart() {
        boolean hasEnoughProcesses = processList.size() >= 2;
        boolean hasValidAlgo = !algoChoiceBox.getValue().equals("Choose Algorithm...");
        boolean hasValidMode = !modeChoiceBox.getValue().equals("Select Mode...");

        return hasEnoughProcesses && hasValidAlgo && hasValidMode;
    }

    private void updateStartButtonState() {
        startButton.setDisable(!isReadyToStart());

        // If we are paused, we don't want to allow a restart
        if (isRunning && isPaused) {
            startButton.setDisable(true);
        } else {
            startButton.setDisable(!isReadyToStart());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chartController = new ChartController(ganttCanvas);

        // 1. Populate Selection Boxes with Placeholders
        algoChoiceBox.setItems(FXCollections.observableArrayList(
                "Choose Algorithm...", "FCFS", "SJF (Non Preemptive)", "SRJF (Preemptive)",
                "Priority (Non Preemptive)", "Priority (Preemptive)", "Round Robin"));
        algoChoiceBox.setValue("Choose Algorithm...");

        modeChoiceBox.setItems(FXCollections.observableArrayList(
                "Select Mode...", "Dynamic (Live 1s/unit)", "Static (Instant)"));
        modeChoiceBox.setValue("Select Mode...");

        // 2. Link Table Columns (Matches getters in your Process class)
        idColumn.setCellValueFactory(new PropertyValueFactory<>("stringId"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        burstColumn.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        remainingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));

        processTable.setItems(processList);

        // 3. Initialize Spinners
        arrivalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        burstSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        arrivalSpinner.setEditable(true);
        burstSpinner.setEditable(true);
        prioritySpinner.setEditable(true);

        // 4. Algorithm Selection Listener
        algoChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("Choose Algorithm...")) {
                boolean needsPriority = newVal.contains("Priority");
                prioritySpinner.setDisable(!needsPriority); //
                handleAlgorithmStrategy(newVal);
            }
        });

        // Initial state: Start, Pause, and Stop buttons disabled
        startButton.setDisable(true);
        pauseButton.setDisable(true);

        // Enable Start button only when list size >= 2
        // Listen to list changes
        processList.addListener((javafx.collections.ListChangeListener<Process>) c -> updateStartButtonState());

        // Listen to ChoiceBox changes
        algoChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newV) -> updateStartButtonState());
        modeChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, newV) -> updateStartButtonState());

        // Set initial state
        updateStartButtonState();
    }

    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        // When running: Start is disabled, Pause and Stop are enabled
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);

        // When running: Modifying the list (Add/Remove) is forbidden
        addButton.setDisable(isRunning);
        removeButton.setDisable(isRunning);

        boolean allowEditing = !isRunning || isPaused;

        addButton.setDisable(!allowEditing);
        removeButton.setDisable(!allowEditing);
    }

    private void handleAlgorithmStrategy(String algo) {
        switch (algo) {
            case "FCFS":
                simulationManager.setAlgorithm(new FCFS());
                break;
            case "SJF (Non Preemptive)":
                simulationManager.setAlgorithm(new SJF());
                break;
            case "SRJF (Preemptive)":
                simulationManager.setAlgorithm(new SRJF());
                break;
            case "Priority (Non Preemptive)":
                simulationManager.setAlgorithm(new NonPreemptivePriority());
                break;
            case "Priority (Preemptive)":
                simulationManager.setAlgorithm(new PreemptivePriority());
                break;
            case "Round Robin":
                simulationManager.setAlgorithm(new RoundRobin());
                break;
        }
    }

    @FXML
    private void handleAddProcess() {
        // Collect inputs from UI spinners
        int arrival = arrivalSpinner.getValue();
        int burst = burstSpinner.getValue();

        // Only use priority if the algorithm requires it
        int priority = prioritySpinner.isDisabled() ? 0 : prioritySpinner.getValue();

        // Create the new process object
        Process newP = new Process(burst, arrival, priority);
        processList.add(newP);
        simulationManager.addProcess(newP); // Add to Logic Manager (Makes it available for the scheduler)

        // If we add a new process during pause, it should be considered in the ready queue immediately
        // and not wait for the next tick
        if (isRunning) {
            simulationManager.rebuildReadyQueue();
        }
    }

    @FXML
    private void handleRemoveProcess() {
        Process selected = processTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            processList.remove(selected);
            simulationManager.removeProcess(selected);
            simulationManager.resetCurrentProcess();
            simulationManager.rebuildReadyQueue();
            updateStartButtonState();
        }
    }

    @FXML
    private void handleClear() {
        processList.clear();
        simulationManager = new SimulationManager();
        Process.resetIdCounter();
        clearStatistics();
        chartController.reset(); // Clears the canvas
        simulationManager.clearAll();
    }

    @FXML
    private void handleStart() {

        // Rebuild simulation every time Start is pressed to reflect any changes in the
        // process list or algorithm selection
        simulationManager = new SimulationManager();
        handleAlgorithmStrategy(algoChoiceBox.getValue());

        for (Process p : processList) {
            p.setRemainingTime(p.getBurstTime()); // Reset remaining time before starting
            simulationManager.addProcess(p);
        }

        // Reset visual state
        chartController.reset();
        SimulationManager.resetCurrentTime();
        isPaused = false;
        pauseButton.setText("Pause");
        isRunning = true;
        updateButtonStates(true, false);

        // Stop any old timeline
        if (timer != null) {
            timer.stop();
        }

        if (modeChoiceBox.getValue().contains("Dynamic")) {
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (isPaused) {
                    return;
                }

                if (simulationManager.isAllFinished()) {
                    timer.stop();
                    isRunning = false;
                    updateButtonStates(false, false);
                    updateStatistics();
                    return;
                }

                Process executed = simulationManager.tick();

                if (executed == null) {
                    chartController.drawTick(simulationManager.getCurrentTime(), -1);
                } else {
                    chartController.drawTick(simulationManager.getCurrentTime(), executed.getId());
                }
                processTable.refresh(); // Update remaining times in the table
            }));

            timer.setCycleCount(Timeline.INDEFINITE);
            timer.playFromStart();

        } else {
            // Static mode: run the whole simulation first
            simulationManager.generateSegments();
            chartController.drawStatic(simulationManager.getChartSegments());

            processTable.refresh();
            updateStatistics();
            isRunning = false;
            updateButtonStates(false, false);
        }
    }

    @FXML
    private void handlePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
        updateButtonStates(isRunning, isPaused);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Statistics
    @FXML private TextField avgWaitingField;
    @FXML private TextField avgTurnaroundField;

    private void updateStatistics() {
        double avgWait = simulationManager.getAverageWaitingTime();
        double avgTurnaround = simulationManager.getAverageTurnaroundTime();
        avgWaitingField.setText(String.format("%.2f", avgWait));
        avgTurnaroundField.setText(String.format("%.2f", avgTurnaround));
    }

    private void clearStatistics() {
        avgWaitingField.clear();
        avgTurnaroundField.clear();
    }

}