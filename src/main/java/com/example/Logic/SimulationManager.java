package com.example.Logic;

import com.example.Logic.Algorithms.Scheduler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Manages the CPU simulation logic, process transitions, and statistics.
 */
public class SimulationManager {
    private List<Process> allProcesses = new ArrayList<>();
    private Queue<Process> readyQueue = new LinkedList<>();
    private List<GanttSegment> chartSegments = new ArrayList<>();
    private Scheduler scheduler;
    private static int currentTime = 0;
    private int arrivalIdx = 0;
    private Process currentRunning = null;
    private boolean needsReinitialization = false;

    // --- Configuration Methods ---

    public void setAlgorithm(Scheduler algo) {
        this.scheduler = algo;
    }

    public List<GanttSegment> getChartSegments() {
        return chartSegments;
    }

    public static void resetCurrentTime() {
        currentTime = 0;
    }

    public void resetCurrentProcess() {
        this.currentRunning = null;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * Checks for processes that have arrived at the current time and adds them to the ready queue.
     */
    private void updateReadyQueue() {
        while (arrivalIdx < allProcesses.size() && allProcesses.get(arrivalIdx).getArrivalTime() <= currentTime) {
            readyQueue.add(allProcesses.get(arrivalIdx));
            arrivalIdx++;
        }
    }

    // --- Core Simulation Logic ---

    /**
     * Executes one tick of the CPU.
     * Handles process selection, execution, and termination.
     */
    public Process tick() {
        if (needsReinitialization) {
            currentRunning = null;
            needsReinitialization = false;
        }

        updateReadyQueue();

        // 1. Clean Queue: Remove any finished processes before the scheduler runs
        readyQueue.removeIf(p -> p.isFinished());

        // 2. Schedule: Ask the selected algorithm which process should run now
        currentRunning = scheduler.getNextProcess(readyQueue, currentTime);

        // Reference for the Gantt Chart segment
        Process executedThisTick = currentRunning;

        if (currentRunning != null) {
            currentRunning.decrementTime();

            // 3. Termination: Handle process completion logic
            if (currentRunning.isFinished()) {
                currentRunning.setCompletionTime(currentTime + 1);
                currentRunning.terminateProcess();

                // Cleanup current running state
                readyQueue.remove(currentRunning);
                currentRunning = null;
            }
        }

        currentTime++;
        return executedThisTick;
    }

    /**
     * Generates all Gantt segments for the entire simulation (used in Static mode).
     */
    public void generateSegments() {
        chartSegments.clear();

        while (!isAllFinished()) {
            Process thisTickProcess = tick();
            chartSegments.add(new GanttSegment(currentTime - 1, currentTime,
                    thisTickProcess != null ? thisTickProcess.getId() : -1)); // -1 represents CPU Idle
        }
    }

    /**
     * Rebuilds the ready queue based on the current simulation time.
     */
    public void rebuildReadyQueue() {
        readyQueue.clear();
        arrivalIdx = 0;

        int processesNum = allProcesses.size();
        for (int i = 0; i < processesNum; i++) {
            Process p = allProcesses.get(i);

            if (p.getArrivalTime() <= currentTime && !p.isFinished())
                readyQueue.add(p);

            if (p.getArrivalTime() <= currentTime)
                arrivalIdx = i + 1;
        }
    }

    /**
     * Adds a new process and keeps the list sorted by arrival time for correct queueing.
     */
    public void addProcess(Process p) {
        allProcesses.add(p);
        allProcesses.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));
    }

    /**
     * Checks if all processes have completed their execution.
     */
    public boolean isAllFinished() {
        return arrivalIdx >= allProcesses.size() && readyQueue.isEmpty() && currentRunning == null;
    }

    /**
     * Removes a process from the simulation pool and resets internal pointers.
     */
    public void removeProcess(Process p) {
        if (allProcesses.contains(p)) {
            allProcesses.remove(p);
            if (readyQueue.contains(p)) {
                readyQueue.remove(p);
            }

            if (currentRunning != null && currentRunning.equals(p)) {
                currentRunning = null;
            }

            arrivalIdx = 0;
            needsReinitialization = true;
        }
    }

    // --- Statistics Calculations ---

    public double getAverageWaitingTime() {
        if (allProcesses.isEmpty())
            return 0;
        double sum = 0;
        for (Process p : allProcesses)
            sum += p.getWaitingTime();
        return sum / allProcesses.size();
    }

    public double getAverageTurnaroundTime() {
        if (allProcesses.isEmpty())
            return 0;
        double sum = 0;
        for (Process p : allProcesses)
            sum += p.getTurnaroundTime();
        return sum / allProcesses.size();
    }

    /**
     * Resets the manager state for a completely new simulation.
     */
    public void clearAll() {
        allProcesses.clear();
        readyQueue.clear();
        chartSegments.clear();
        currentRunning = null;
        currentTime = 0;
        arrivalIdx = 0;
    }
}