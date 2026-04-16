package com.example.Logic;

import com.example.Logic.Algorithms.Scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Manages the simulation
public class SimulationManager {
    private List<Process> allProcesses = new ArrayList<>();
    private Queue<Process> readyQueue = new LinkedList<>();
    private List<GanttSegment> chartSegments = new ArrayList<>();
    private Scheduler scheduler;
    private static int currentTime = 0;
    private int arrivalIdx = 0;
    private Process currentRunning = null;
    private boolean needsReinitialization = false;

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

    private void updateReadyQueue() {
        // Doesn't loop unless there are processes that have arrived at the current time
        while (arrivalIdx < allProcesses.size() && allProcesses.get(arrivalIdx).getArrivalTime() <= currentTime) {
            readyQueue.add(allProcesses.get(arrivalIdx));
            arrivalIdx++;
        }
    }

    // Simulate one tick of the CPU scheduler (choose next process and dispatch it)
    public Process tick() {
        if (needsReinitialization) {
            // Force the scheduler to clear any potential stale state
            // or just set your internal pointers to null
            currentRunning = null;
            needsReinitialization = false;
        }
        updateReadyQueue();
        if (currentRunning != null && !allProcesses.contains(currentRunning)) {
            currentRunning = null;
        }
        currentRunning = scheduler.getNextProcess(readyQueue, currentTime);
        Process executedThisTick = currentRunning; // Store the process that will run in this tick for Gantt chart
                                                   // segment creation

        if (currentRunning != null) {
            currentRunning.decrementTime();
            if (currentRunning.isFinished()) {
                currentRunning.setCompletionTime(currentTime + 1);
                currentRunning.terminateProcess();
                readyQueue.remove(currentRunning);
                currentRunning = null;
            }
        }
        currentTime++;
        return executedThisTick; // Process that ran in this tick (To deal with idle state)
    }

    public void generateSegments() {
        // Clear list first to avoid multiple generations if called for than once
        chartSegments.clear();

        while (!isAllFinished()) {
            Process thisTickProcess = tick();
            chartSegments.add(new GanttSegment(currentTime - 1, currentTime,
                    thisTickProcess != null ? thisTickProcess.getId() : -1)); // -1 for idle

        }
    }

    public void addProcess(Process p) {
        allProcesses.add(p);
        // Requirement: Must be sorted by arrival time so updateReadyQueue works
        allProcesses.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));
    }

    public boolean isAllFinished() {
        // Finished if:
        // 1. All processes have been added to the ready queue (arrivalIdx reaches list
        // size)
        // 2. The ready queue is empty
        // 3. No process is currently on the CPU
        return arrivalIdx >= allProcesses.size() && readyQueue.isEmpty() && currentRunning == null;
    }

    // when remove process from gui remove from simulation manager
    public void removeProcess(Process p) {
        if (allProcesses.contains(p)) {
            allProcesses.remove(p);
            if (readyQueue.contains(p)) {
                readyQueue.remove(p);
            }
            // CRITICAL: If the removed process is the one currently running,
            // nullify it so tick() doesn't try to operate on it.
            if (currentRunning != null && currentRunning.equals(p)) {
                currentRunning = null;
            }
            // Important: Reset the pointer so the manager re-scans
            // the pool for arrivals correctly when the simulation starts.
            arrivalIdx = 0;
            needsReinitialization = true;
        }
    }

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

    public void clearAll() {
        allProcesses.clear();
        readyQueue.clear();
        chartSegments.clear();
        currentRunning = null;
        currentTime = 0;
        arrivalIdx = 0;
    }
}