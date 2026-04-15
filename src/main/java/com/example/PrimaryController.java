/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package com.example;

import com.example.Logic.Process;
import com.example.Logic.SimulationManager;
import com.example.Logic.Algorithms.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {

    // Input Fields
    @FXML
    private Spinner<Integer> arrivalSpinner;
    @FXML
    private Spinner<Integer> burstSpinner;
    @FXML
    private Spinner<Integer> prioritySpinner;
    @FXML
    private ChoiceBox<String> algoChoiceBox;
    @FXML
    private ChoiceBox<String> modeChoiceBox;

    // Table
    @FXML
    private TableView<Process> processTable;
    @FXML
    private TableColumn<Process, Integer> idColumn;
    @FXML
    private TableColumn<Process, Integer> arrivalColumn;
    @FXML
    private TableColumn<Process, Integer> burstColumn;
    @FXML
    private TableColumn<Process, Integer> remainingTimeColumn;

    // MUST match the fx:id in FXML file exactly
    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;

    private ObservableList<Process> processList = FXCollections.observableArrayList();
    private SimulationManager simulationManager = new SimulationManager();
    private Timeline timer;
    private int currentTime = 0;
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Populate Selection Boxes with Placeholders [cite: 10, 11]
        algoChoiceBox.setItems(FXCollections.observableArrayList(
                "Choose Algorithm...", "FCFS", "SJF (Non Preemptive)", "SRJF (Preemptive)",
                "Priority (Non Preemptive)", "Priority (Preemptive)", "Round Robin"
        ));
        algoChoiceBox.setValue("Choose Algorithm...");

        modeChoiceBox.setItems(FXCollections.observableArrayList(
                "Select Mode...", "Dynamic (Live 1s/unit)", "Static (Instant)"
        ));
        modeChoiceBox.setValue("Select Mode...");

        // 2. Link Table Columns (Matches getters in your Process class)
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        burstColumn.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        remainingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));

        processTable.setItems(processList);

        // 3. Initialize Spinners
        arrivalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        burstSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));

        // 4. Algorithm Selection Listener (Handles Requirement #10) 
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
        stopButton.setDisable(true);

        // Reactive UI: Enable Start button only when list size >= 2
        // Listen to list changes
        processList.addListener((javafx.collections.ListChangeListener<Process>) c -> updateStartButtonState());

        // Listen to ChoiceBox changes
        algoChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newV) -> updateStartButtonState());
        modeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newV) -> updateStartButtonState());

        // Set initial state
        updateStartButtonState();
    }

    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        // When running: Start is disabled, Pause and Stop are enabled
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);
        stopButton.setDisable(!isRunning);

        // When running: Modifying the list (Add/Remove) is forbidden
        addButton.setDisable(isRunning);
        removeButton.setDisable(isRunning);
    }

    private void handleAlgorithmStrategy(String algo) {
        switch (algo) {
            case "FCFS":
                simulationManager.setAlgorithm(new FCFS());
                break;
            case "Round Robin":
                simulationManager.setAlgorithm(new RoundRobin());
                break; // 3s quantum 
            case "SJF (Preemptive)":
                simulationManager.setAlgorithm(new SRJF());
                break;
            case "Priority (Preemptive)":
                simulationManager.setAlgorithm(new PreemptivePriority());
                break;
        }
    }

    @FXML
    private void handleAddProcess() {
        // 1. Collect inputs from UI spinners
        int arrival = arrivalSpinner.getValue();
        int burst = burstSpinner.getValue();

        // Requirement #11: Only use priority if the algorithm requires it 
        int priority = prioritySpinner.isDisabled() ? 0 : prioritySpinner.getValue();

        // 2. Create the new process object
        Process newP = new Process(burst, arrival, priority);

        // 3. Add to UI List (Updates the TableView automatically)
        processList.add(newP);

        // 4. Add to Logic Manager (Makes it available for the scheduler)
        simulationManager.addProcess(newP);

        System.out.println("Process " + newP.getId() + " added successfully.");
    }

    @FXML
    private void handleRemoveProcess() {
        Process selected = processTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            processList.remove(selected);
            // simulationManager.removeProcess(selected); 
        }
    }

    @FXML
    private void handleClear() {
        processList.clear();
        // simulationManager.clearAll();
    }

    @FXML
    private void handleStart() {
        // Condition: Check Algorithm and Mode (the size check is handled by the button state)
        if (algoChoiceBox.getValue().equals("Choose Algorithm...") || modeChoiceBox.getValue().equals("Select Mode...")) {
            showError("Please select both an Algorithm and a Simulation Mode.");
            return;
        }

        // Update State
        isRunning = true;
        updateButtonStates(true, false);

        // Trigger Live or Static Simulation 
        if (modeChoiceBox.getValue().contains("Dynamic")) {
            // Start your JavaFX Timeline here
        } else {
            // Run Static Loop
        }
    }

    @FXML
    private void handlePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
    }

    @FXML
    private void handleStop() {
        isRunning = false;
        isPaused = false;
        updateButtonStates(false, false);
        if (timer != null) {
            timer.stop();
        }
        processList.forEach(p -> p.setRemainingTime(p.getBurstTime())); // Reset
        processTable.refresh();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
