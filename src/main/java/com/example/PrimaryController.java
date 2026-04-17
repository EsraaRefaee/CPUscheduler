/*
 * CPU Scheduling Simulator 2026
 * Developed using JavaFX and standard scheduling algorithms.
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

    @FXML private Spinner<Integer> quantumSpinner;
    @FXML private Spinner<Integer> arrivalSpinner;
    @FXML private Spinner<Integer> burstSpinner;
    @FXML private Spinner<Integer> prioritySpinner;
    @FXML private ChoiceBox<String> algoChoiceBox;
    @FXML private ChoiceBox<String> modeChoiceBox;

    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, Integer> idColumn;
    @FXML private TableColumn<Process, Integer> arrivalColumn;
    @FXML private TableColumn<Process, Integer> burstColumn;
    @FXML private TableColumn<Process, Integer> remainingTimeColumn;
    @FXML private TableColumn<Process, Integer> turnaroundColumn;
    @FXML private TableColumn<Process, Integer> waitingColumn;
    @FXML private TableColumn<Process, Integer> priorityColumn;

    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button addButton;
    @FXML private Button removeButton;

    @FXML private Canvas ganttCanvas;

    // --- Logic & Simulation Variables ---
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

        if (isRunning && isPaused) {
            startButton.setDisable(true);
        } else {
            startButton.setDisable(!isReadyToStart());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chartController = new ChartController(ganttCanvas);

        // 1. Initialize ChoiceBox Options
        algoChoiceBox.setItems(FXCollections.observableArrayList(
                "Choose Algorithm...", "FCFS", "SJF (Non Preemptive)", "SRJF (Preemptive)",
                "Priority (Non Preemptive)", "Priority (Preemptive)", "Round Robin"));
        algoChoiceBox.setValue("Choose Algorithm...");

        modeChoiceBox.setItems(FXCollections.observableArrayList(
                "Select Mode...", "Dynamic (Live 1s/unit)", "Static (Instant)"));
        modeChoiceBox.setValue("Select Mode...");

        // 2. Setup Algorithm Selection Listeners
        algoChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Enable/Disable spinners based on algorithm requirements
                prioritySpinner.setDisable(!newVal.contains("Priority"));
                quantumSpinner.setDisable(!newVal.equals("Round Robin"));

                handleAlgorithmStrategy(newVal);
                processTable.refresh();
                updateStartButtonState();
            }
        });

        // 3. Link Table Columns to Process class properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("stringId"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        burstColumn.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        remainingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));
        turnaroundColumn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        waitingColumn.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));

        processTable.setItems(processList);

        // 4. Configure Input Spinners
        arrivalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        arrivalSpinner.setEditable(true);
        burstSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        burstSpinner.setEditable(true);
        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        prioritySpinner.setDisable(true);
        quantumSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        quantumSpinner.setDisable(true);

        // 5. Custom Cell Factory for Priority Column (Hides priority if not applicable)
        priorityColumn.setCellFactory(column -> new TableCell<Process, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String selectedAlgo = algoChoiceBox.getValue();
                    if (selectedAlgo != null && selectedAlgo.contains("Priority")) {
                        setText(item.toString());
                    } else {
                        setText("-");
                    }
                }
            }
        });

        // 6. Mode Selection Listener (Handles re-indexing for Static mode)
        modeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.contains("Static")) {
                for (int i = 0; i < processList.size(); i++) {
                    processList.get(i).setId(i);
                }
                Process.setIdCounter(processList.size());
                processTable.refresh();
            }
            updateStartButtonState();
        });

        // Set initial button states
        startButton.setDisable(true);
        pauseButton.setDisable(true);
        processList.addListener((javafx.collections.ListChangeListener<Process>) c -> updateStartButtonState());
    }

    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);

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
                RoundRobin rr = new RoundRobin();
                rr.setTimeQuantum(quantumSpinner.getValue());
                simulationManager.setAlgorithm(rr);
                break;
        }
    }

    @FXML
    private void handleAddProcess() {
        int arrival = arrivalSpinner.getValue();
        int burst = burstSpinner.getValue();
        int priority = prioritySpinner.isDisabled() ? 0 : prioritySpinner.getValue();

        Process newP = new Process(burst, arrival, priority);
        processList.add(newP);
        simulationManager.addProcess(newP);

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

            String mode = modeChoiceBox.getValue();
            if (mode != null && mode.contains("Static")) {
                // Keep IDs sequential in Static mode
                for (int i = 0; i < processList.size(); i++) {
                    processList.get(i).setId(i);
                }
                Process.setIdCounter(processList.size());
                processTable.refresh();
            }

            if (processList.isEmpty()) {
                algoChoiceBox.setDisable(false);
                modeChoiceBox.setDisable(false);
                Process.resetIdCounter();
            }
            updateStartButtonState();
        }
    }

    @FXML
    private void handleClear() {
        processList.clear();
        simulationManager = new SimulationManager();
        Process.resetIdCounter();
        clearStatistics();
        chartController.reset();
        simulationManager.clearAll();
    }

    @FXML
    private void handleStart() {
        simulationManager = new SimulationManager();
        handleAlgorithmStrategy(algoChoiceBox.getValue());

        for (Process p : processList) {
            p.setRemainingTime(p.getBurstTime());
            simulationManager.addProcess(p);
        }

        chartController.reset();
        SimulationManager.resetCurrentTime();
        isPaused = false;
        pauseButton.setText("Pause");
        isRunning = true;
        updateButtonStates(true, false);

        if (timer != null) {
            timer.stop();
        }

        if (modeChoiceBox.getValue().contains("Dynamic")) {
            // Live Simulation: Tick by tick
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (isPaused) return;

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
                processTable.refresh();
            }));

            timer.setCycleCount(Timeline.INDEFINITE);
            timer.playFromStart();

        } else {
            // Instant Simulation: Calculate and Draw entire chart
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

    // --- Statistics and Calculations ---
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

    private void reindexProcesses() {
        for (int i = 0; i < processList.size(); i++) {
            processList.get(i).setId(i);
        }
        Process.setIdCounter(processList.size());
        processTable.refresh();
    }
}