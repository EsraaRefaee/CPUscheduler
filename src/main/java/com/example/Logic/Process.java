package com.example.Logic;

public class Process {
    private static int idCounter = 0;
    private final int id;
    private final int burstTime;
    private final int arrivalTime;
    private int remainingTime;
    private int completionTime;
    private int priority = 0; // Default priority
    private int waitingTime;
    private int turnaroundTime;

    public Process(int burstTime, int arrivalTime) {
        this.id = idCounter++;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.remainingTime = burstTime;
    }

    public Process(int burstTime, int arrivalTime, int priority) {
        this.id = idCounter++;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getBurstTime() { return burstTime; }
    public int getArrivalTime() { return arrivalTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getCompletionTime() { return completionTime; }
    public int getPriority() { return priority; }
    public int getWaitingTime() { return waitingTime;}
    public int getTurnaroundTime() { return turnaroundTime; }
    public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime;}
    
    public void decrementTime() {
        this.remainingTime--;
    }
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }
    public boolean isFinished() {
        return this.remainingTime == 0;
    }
    public void terminateProcess() {
        this.turnaroundTime = completionTime - arrivalTime;
        this.waitingTime = turnaroundTime - burstTime;
    }
    public static void resetIdCounter() { idCounter = 0; }
}
