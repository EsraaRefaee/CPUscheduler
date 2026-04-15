package com.example.Logic;

import com.example.Logic.Algorithms.Scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// HABIBA COOL GDANNNN

// Manages the simulation
public class SimulationManager {
    private List<Process> allProcesses = new ArrayList<>();
    private Queue<Process> readyQueue = new LinkedList<>();
    private List<GanttSegment> chartSegments = new ArrayList<>();
    private Scheduler scheduler;
    private int currentTime = 0;
    private int arrivalIdx = 0;
    private Process currentRunning= null;
    private int segmentStartTime;

    public void setAlgorithm(Scheduler algo) {
        this.scheduler = algo;
    }
    public List<GanttSegment> getChartSegments(){ return chartSegments; }

    // TODO: Assumed allProcesses list must be sorted ascendingly by arrival time every time a new process is added
    private void updateReadyQueue() {
        // Doesn't loop unless there are processes that have arrived at the current time
        while (arrivalIdx < allProcesses.size() && allProcesses.get(arrivalIdx).getArrivalTime() <= currentTime) {
            readyQueue.add(allProcesses.get(arrivalIdx));
            arrivalIdx++;
        }
    }

    // Simulate one tick of the CPU scheduler (choose next process and dispatch it)
    public void tick() {
    updateReadyQueue();
    Process nextProcess = scheduler.getNextProcess(readyQueue, currentTime);

    // 1. Handle Context Switching / Gantt Segment Start
    if (nextProcess != currentRunning) {
        // Before switching, if someone was running, close their segment
        if (currentRunning != null) {
            chartSegments.add(new GanttSegment(segmentStartTime, currentTime, currentRunning.getId()));
        }
        
        contextSwitch(nextProcess);
        segmentStartTime = currentTime; // The new process starts exactly now
    }

    // 2. Increment time for the work about to be done
    currentTime++; 

    // 3. Execute the process
    if (currentRunning != null) {
        currentRunning.decrementTime();

        // 4. Check if finished AFTER the decrement
        if (currentRunning.isFinished()) {
            // Since we already incremented currentTime, this is the exact finish time
            currentRunning.setCompletionTime(currentTime); 
            currentRunning.terminateProcess();
            
            readyQueue.remove(currentRunning);

            // Close the segment in the Gantt chart
            chartSegments.add(new GanttSegment(segmentStartTime, currentTime, currentRunning.getId()));

            currentRunning = null; 
            segmentStartTime = currentTime; // Reset for potential idle time or next process
        }
    }
}

    private void contextSwitch(Process nextProcess) {
        // if there is a currently running process, we need to end its segment in the Gantt chart
        if (currentRunning != null) {
            chartSegments.add(new GanttSegment(segmentStartTime, currentTime, currentRunning.getId()));
        }

        // Start a new segment for the next process
        segmentStartTime = currentTime;
        currentRunning = nextProcess;
    }
    
    /////////////////////////////////////////////////////////////
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