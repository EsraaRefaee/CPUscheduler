package com.example.Logic;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartController {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private double pixelsPerTick = 28.0;
    private double leftMargin = 40.0; // where time 0 starts
    private double topMargin = 50.0; // where the first lane starts
    private double laneHeight = 34.0; // vertical height for process lane
    private double axisX = topMargin + laneHeight + 15.0; // y-coordinate for the horizontal time axis

    private final Map<Integer, Color> pidColorCache = new HashMap<>();

    public ChartController(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font("System", 12));
        reset();
    }

    // draw one block of the chart
    private void drawBlock(double x, int pid) {
        double y = topMargin;
        double w = pixelsPerTick;
        double h = laneHeight;

        gc.setFill(Color.web("#edf3ff"));
        gc.fillRect(x, y, w, h); // assuming constant square size

        gc.setStroke(Color.web("#a4b5de"));
        gc.strokeRect(x, y, w, h);

        String label = "P" + pid;
        gc.setFill(Color.web("4a4b44"));
        gc.fillText(label, x + 4.0, y + (laneHeight * 0.65));
    }

    public void drawTick(int currentTime, int pid) {
        int endTime = currentTime + 1;

        ensureWidthForTime(endTime);

        double x = leftMargin + (currentTime * pixelsPerTick);
        double w = (endTime - currentTime) * pixelsPerTick;
        // drawBlock(x, w, pid);

        // Draw tick markers for readability.
        drawTimeMarker(currentTime);
        drawTimeMarker(endTime);
    }

    public void drawStatic(List<GanttSegment> segments) {
        reset();
        if (segments == null || segments.isEmpty()) {
            return;
        }

        int maxTime = 0;
        for (GanttSegment segment : segments) {
            maxTime = Math.max(maxTime, segment.getEndTime());
        }
        ensureWidthForTime(maxTime);

        for (GanttSegment segment : segments) {
           // drawTick(segment.getStartTime(), segment.getEndTime(), segment.getPid());
        }
    }

    // Auto-resize width of canvas if needed
    public void ensureWidthForTime(int time) {
        // neededWidth = startOfChart + spaceForTime + somePadding
        double neededWidth = leftMargin + (time * pixelsPerTick) + 30.0;

        if (neededWidth > canvas.getWidth()) {
            canvas.setWidth(neededWidth);
            // Canvas resize clears content; redraw axis base.
            drawAxes();
        }
    }

    public void reset() {
        // Clear the canvas for a new simulation
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawAxes();
    }

    private void drawAxes() {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.0);
        gc.strokeLine(leftMargin, axisX, Math.max(leftMargin + 1.0, canvas.getWidth() - 10.0), axisX);

        gc.setFill(Color.BLACK);
        gc.fillText("Time", 6.0, axisX + 4.0);
    }

    private void drawTimeMarker(int t) {
        double x = leftMargin + (t * pixelsPerTick);
        gc.setStroke(Color.rgb(180, 180, 180));
        gc.strokeLine(x, axisX - laneHeight - 6.0, x, axisX + 4.0);

        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(t), x - 3.0, axisX + 18.0);
    }


}
