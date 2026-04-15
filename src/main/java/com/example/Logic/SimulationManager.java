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
    private Process currentRunning= null;

    public void setAlgorithm(Scheduler algo) {
        this.scheduler = algo;
    }
    public List<GanttSegment> getChartSegments(){ return chartSegments; }
    public static void resetCurrentTime() { currentTime = 0; }
    public int getCurrentTime() { return currentTime; }
    public static void incrementTime() { currentTime++; }

    private void updateReadyQueue() {
        // Doesn't loop unless there are processes that have arrived at the current time
        while (arrivalIdx < allProcesses.size() && allProcesses.get(arrivalIdx).getArrivalTime() <= currentTime) {
            readyQueue.add(allProcesses.get(arrivalIdx));
            arrivalIdx++;
        }
    }

    // Simulate one tick of the CPU scheduler (choose next process and dispatch it)
    public Process tick() {
        updateReadyQueue();
        currentRunning = scheduler.getNextProcess(readyQueue, currentTime);
        Process executedThisTick = currentRunning; // Store the process that will run in this tick for Gantt chart segment creation

        if (currentRunning != null) {
            currentRunning.decrementTime();
            if (currentRunning.isFinished()) {
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
            chartSegments.add(new GanttSegment(currentTime - 1, currentTime, thisTickProcess != null ?
                    thisTickProcess.getId() : -1)); // -1 for idle

        }
    }

    public void addProcess(Process p) {
        allProcesses.add(p);
        // Requirement: Must be sorted by arrival time so updateReadyQueue works 
        allProcesses.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));
    }
    public boolean isAllFinished() {
        // Finished if: 
        // 1. All processes have been added to the ready queue (arrivalIdx reaches list size)
        // 2. The ready queue is empty
        // 3. No process is currently on the CPU
        return arrivalIdx >= allProcesses.size() && readyQueue.isEmpty() && currentRunning == null;
    }
}