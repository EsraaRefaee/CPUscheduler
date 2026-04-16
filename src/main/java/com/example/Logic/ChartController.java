package com.example.Logic;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class ChartController {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private double pixelsPerTick = 34.0;
    private double leftMargin = 20.0; // where time 0 starts
    private double topMargin = 40.0; // where the first lane starts
    private double laneHeight = 34.0; // vertical height for process lane

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

//        gc.setFill(Color.web("#edf3ff"));
//        gc.fillRect(x, y, w, h); // assuming constant square size
//
//        gc.setStroke(Color.web("#a4b5de"));
//        gc.strokeRect(x, y, w, h);

        Color fill = getColorForPid(pid);
        Color stroke = (pid == -1) ? Color.web("#b3b3b3") : fill.deriveColor(0, 1.0, 0.75, 1.0);

        gc.setFill(fill);
        gc.fillRect(x, y, w, h);

        gc.setStroke(stroke);
        gc.strokeRect(x, y, w, h);

        gc.setFill(Color.web("#4a4b44"));
        String label = (pid == -1) ? "Idle" : "P" + pid;

        gc.fillText(label, (label.equals("Idle"))? x + 7.0 : x + 9.0, y + (laneHeight * 0.65));
    }

    // Method for dynamic drawing
    public void drawTick(int endTime, int pid) {
        ensureWidthForTime(endTime); // check canvas's width and resize if needed

        int startTime = endTime - 1;
        double x = leftMargin + (startTime * pixelsPerTick);
        drawBlock(x, pid);

        // Draw tick markers for readability.
        drawTimeMarker(endTime, x);
    }

    // The loop to draw is built-in, only need to call the function when static drawing is needed
    public void drawStatic(List<GanttSegment> segments) {
        reset();
        if (segments == null || segments.isEmpty()) {
            return;
        }

        // Calculate maximum time and adjust canvas width accordingly
        int maxTime = 0;
        for (GanttSegment segment : segments) {
            maxTime = Math.max(maxTime, segment.getEndTime());
        }
        ensureWidthForTime(maxTime);

        // Draw
        for (GanttSegment segment : segments) {
           drawTick(segment.getEndTime(), segment.getPid());
        }
    }

    // Auto-resize width of canvas if needed
    public void ensureWidthForTime(int time) {
        // The 30.0 is a padding so the chart is not touching the canvas's right edge
        double neededWidth = leftMargin + (time * pixelsPerTick) + 30.0;

        // check if the needed width for the chart is more than the canvas's width
        if (neededWidth > canvas.getWidth()) {
            // reset canvas's width to the needed width
            canvas.setWidth(neededWidth);
        }
    }

    public void reset() {
        // Clear the canvas for a new simulation
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawTimeMarker(int endTime, double x) {
        gc.setFill(Color.web("#4a4b44"));
        double Xcorner = x + pixelsPerTick;
        double y = topMargin + laneHeight + 15.0;

        if (endTime - 1 == 0) {
            // draw the 0 marker at the left margin
            gc.fillText("0", leftMargin - 3.0, y); // 3.0 is an offset to center the number
        }

        gc.fillText(String.valueOf(endTime), Xcorner - 3.0, y); // 3.0 is an offset to center the number
    }

    private Color getColorForPid(int pid) {
        if (pid == -1) {
            return Color.web("#d9d9d9"); // Idle = light gray
        }

        // Distinct pastel color per process id (deterministic).
        double hue = (pid * 137) % 360.0;
        return Color.hsb(hue, 0.20, 0.95); // low saturation + high brightness = pastel
    }


}
