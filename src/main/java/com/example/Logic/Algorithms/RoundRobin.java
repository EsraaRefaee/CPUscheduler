package com.example.Logic.Algorithms;

import com.example.Logic.Process;
import java.util.Queue;

public class RoundRobin implements Scheduler {
    private int timeQuantum = 3;
    private int quantumTracker = 0;
    private Process currentlyRunning = null;

    // This allows the Controller to set the value from the Spinner
    public void setTimeQuantum(int tq) {
        this.timeQuantum = tq;
    }

    @Override
    public Process getNextProcess(Queue<Process> readyQueue, int currentTime) {
        if (currentlyRunning != null) {
            quantumTracker++;

            // 1. IF FINISHED: Do NOT add it back to the queue
            if (currentlyRunning.isFinished()) {
                currentlyRunning = null;
                quantumTracker = 0;
            }
            // 2. ONLY PREEMPT IF NOT FINISHED:
            else if (quantumTracker >= timeQuantum) {
                if (!readyQueue.isEmpty()) {
                    readyQueue.add(currentlyRunning); // Only add back if remainingTime > 0
                    currentlyRunning = null;
                    quantumTracker = 0;
                } else {
                    quantumTracker = 0;
                }
            }
        }

        if (currentlyRunning == null && !readyQueue.isEmpty()) {
            currentlyRunning = readyQueue.poll();
            quantumTracker = 0;
        }

        return currentlyRunning;
    }
}