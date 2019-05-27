package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class LineDrawer {
    private static boolean isPoint(double x0, double y0, double xe, double ye) {
        return x0 == xe && y0 == ye;
    }

    private static int round(double value) {
        return (int) (value + 0.5);
    }

    public static void DigitalDiffAnalyzeDraw(Canvas canvas, double x0, double y0, double xe, double ye, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        PixelWriter writer = gc.getPixelWriter();
        if (isPoint(x0, y0, xe, ye)) {
            writer.setColor((int) x0, (int) y0, color);
            return;
        }

        double diffX = Math.abs(xe - x0);
        double diffY = Math.abs(ye - y0);
        double len = diffX > diffY ? diffX : diffY;

        double dX = (xe - x0) / len;
        double dY = (ye - y0) / len;
        double curX = x0;
        double curY = y0;
        for (int i = 0; i < len; i++) {
            int resX = round(curX);
            int resY = round(curY);
            writer.setColor(resX, resY, color);
            curX += dX;
            curY += dY;
        }
    }
}
