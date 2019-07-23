package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.LinkedList;

public class Controller {

    @FXML
    Canvas canvas;
    @FXML
    Button addCutterButton;
    @FXML
    Button addLineButton;
    @FXML
    TextField inputY2Field;
    @FXML
    TextField inputX2Field;
    @FXML
    TextField inputY1Field;
    @FXML
    TextField inputX1Field;
    @FXML
    Button clearAllButton;
    @FXML
    ColorPicker visiblePicker;
    @FXML
    ColorPicker cutterPicker;
    @FXML
    ColorPicker linePicker;
    @FXML
    Label cursorLabel;

    private Cutter cutter = new Cutter();
    private Edge currentLine = new Edge();
    private LinkedList<Edge> edges = new LinkedList<>();
    private LinkedList<Cutter> cutters = new LinkedList<>();

    @FXML
    public void initialize() {
        setupColors();
        setupCanvasListeners();


        clearAllButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            clearCanvas();
            clearData();
        });

        addLineButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                Point p1 = new Point(Integer.parseInt(inputX1Field.getText()),
                        Integer.parseInt(inputY1Field.getText()));
                Point p2 = new Point(Integer.parseInt(inputX2Field.getText()),
                        Integer.parseInt(inputY2Field.getText()));
                edges.add(new Edge(p1, p2));
                doUpdate();
            } catch (NumberFormatException e) {
                setAlert("Введены неверные данные для нового отрезка");
            }
        });

        addCutterButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            try {
                if (cutters.size() == 0) {
                    Point p1 = new Point(Integer.parseInt(inputX1Field.getText()),
                            Integer.parseInt(inputY1Field.getText()));
                    Point p2 = new Point(Integer.parseInt(inputX2Field.getText()),
                            Integer.parseInt(inputY2Field.getText()));
                    cutters.add(new Cutter(p1, p2));
                    doUpdate();
                } else {
                    setAlert("Уже есть отсекатель!");
                }
            } catch (NumberFormatException e) {
                setAlert("Введены неверные данные для нового отсекателя");
            }
        });
    }

    private void setupCanvasListeners() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            MouseButton b = e.getButton();
            boolean hasShift = e.isShiftDown();
            boolean hasControl = e.isControlDown();


            if (b == MouseButton.PRIMARY && hasShift && hasControl) {
                //Прямая
                addPoint((int) e.getX(), (int) e.getY());
            } else if (b == MouseButton.PRIMARY && hasShift) {
                // горизонтальная
                addPointHorizontal((int) e.getX(), (int) e.getY());
            } else if (b == MouseButton.PRIMARY && hasControl) {
                // вертикальная
                addPointVertical((int) e.getX(), (int) e.getY());
            } else if (b == MouseButton.PRIMARY) {
                addPoint((int) e.getX(), (int) e.getY());
                // Прямая
            } else if (b == MouseButton.SECONDARY) {
                // замкнуть многоугольник
                addCutterPoint((int) e.getX(), (int) e.getY());
            }
            doUpdate();

        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            cursorLabel.setText("Координата курсора: " + mouseEvent.getX() + " ; " + mouseEvent.getY());
        });
    }

    private void addCutterPoint(int x, int y) {
        if (cutters.size() == 0) {
            if (!cutter.isBeginInit()) {
                cutter.setTopLeft(new Point(x, y));
                cutter.setBeginInit(true);
            } else if (!cutter.isEndInit()) {
                cutter.setBottomRight(new Point(x, y));
                cutter.setEndInit(true);
                Cutter copy = new Cutter(cutter.getTopLeft(), cutter.getBottomRight());
                cutters.add(copy);
                cutter.clear();
                doUpdate();
            }
        } else {
            setAlert("Уже есть отсекатель!");
        }
    }

    private void setupColors() {
        cutterPicker.setValue(Color.BLACK);
        visiblePicker.setValue(Color.LIME);
        linePicker.setValue(Color.BLUE);
        clearCanvas();
    }

    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }


    private void clearData() {
        currentLine.clear();
        edges.clear();
        cutters.clear();
    }

    private void addPointHorizontal(int x, int y) {
        if (currentLine.isEndInit()) {
            addPoint(x, currentLine.getEnd().getY());
        } else if (currentLine.isBeginInit()) {
            addPoint(x, currentLine.getBegin().getY());
        } else {
            addPoint(x, y);
        }
    }

    private void addPointVertical(int x, int y) {
        if (currentLine.isEndInit()) {
            addPoint(currentLine.getEnd().getX(), y);
        } else if (currentLine.isBeginInit()) {
            addPoint(currentLine.getBegin().getX(), y);
        } else {
            addPoint(x, y);
        }
    }

    private void addPoint(double x, double y) {
        if (!currentLine.isBeginInit()) {
            currentLine.setBegin(new Point(x, y));
            currentLine.setBeginInit(true);
        } else if (!currentLine.isEndInit()) {
            edges.add(new Edge(currentLine.getBegin(), new Point(x, y)));
            doUpdate();
            currentLine.clear();
        }
    }

    private void doUpdate() {
        clearCanvas();
        cut();
    }

    private void drawLine(Edge edge) {
        Point beg = edge.getBegin();
        Point end = edge.getEnd();
        drawLine(beg.getX(), beg.getY(), end.getX(), end.getY());
    }

    private void drawLine(double xBegin, double yBegin, double xEnd, double yEnd) {
        LineDrawer.DigitalDiffAnalyzeDraw(canvas, xBegin, yBegin, xEnd, yEnd, linePicker.getValue());
    }

    private void cut() {
        for (Edge e : edges) {
            drawLine(e);
        }
        for (Cutter c : cutters) {
            for (Edge e : edges) {
                cutMiddlePoint(e, c);
            }
        }
        for (Cutter c : cutters) {
            drawCutter(c);
        }
    }

    private int[] getCode(Point point, Cutter cutter) {
        int[] result = new int[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
        }
        if (point.getX() < cutter.getTopLeft().getX()) {
            result[0] = 1;
        }
        if (point.getX() > cutter.getBottomRight().getX()) {
            result[1] = 1;
        }
        if (point.getY() < cutter.getTopLeft().getY()) {
            result[2] = 1;
        }
        if (point.getY() > cutter.getBottomRight().getY()) {
            result[3] = 1;
        }
        return result;
    }

    private int sum(int[] array) {
        int result = 0;
        for (int i : array) {
            result += i;
        }
        return result;
    }

    private void cutMiddlePoint(Edge line, Cutter cutter) {
        double eps = 0.1;
        int i = 1;
        Point p1 = line.getBegin();
        Point p2 = line.getEnd();
        Color visiblePart = visiblePicker.getValue();
        while (true) {
            int[] T1 = getCode(p1, cutter);
            int[] T2 = getCode(p2, cutter);

            int S1 = sum(T1);
            int S2 = sum(T2);

            if (S1 == 0 && S2 == 0) {
                LineDrawer.DigitalDiffAnalyzeDraw(canvas, p1.getX(), p1.getY(), p2.getX(), p2.getY(),
                        visiblePart);
                return;
            }

            int[] P = new int[4];
            for (int j = 0; j < P.length; j++) {
                P[j] = T1[j] * T2[j];
            }


            if (sum(P) != 0) {

                return;
            }

            if (i > 2) {
                LineDrawer.DigitalDiffAnalyzeDraw(canvas, p1.getX(), p1.getY(), p2.getX(), p2.getY(),
                        visiblePart);
                return;
            }
            Point tmp = new Point(p1.getX(), p1.getY());
            if (S2 == 0) {
                p1 = new Point(p2.getX(), p2.getY());
                p2 = new Point(tmp.getX(), tmp.getY());
                i++;
                continue;
            }

            int n = 1;
            while (true) {

                double x1 = p1.getX();
                double x2 = p2.getX();
                double y1 = p1.getY();
                double y2 = p2.getY();
                double len = Math.abs(x1 - x2 + y1 - y2) / Math.pow(2, n);
                if (len < eps) {
                    p1 = new Point(p2.getX(), p2.getY());
                    p2 = new Point(tmp.getX(), tmp.getY());
                    i++;
                    break;
                } else {
                    Point pointMiddle = new Point((x1 + x2) / 2, (y1 + y2) / 2);
                    Point Pm = new Point(x1, y1);
                    p1 = new Point(pointMiddle.getX(), pointMiddle.getY());
                    T1 = getCode(p1, cutter);
                    T2 = getCode(p2, cutter);
                    P = new int[4];
                    for (int j = 0; j < P.length; j++) {
                        P[j] = T1[j] * T2[j];
                    }

                    if (sum(P) != 0) {
                        p1 = new Point(Pm.getX(), Pm.getY());
                        p2 = new Point(pointMiddle.getX(), pointMiddle.getY());
                    }
                }
                n++;
            }

        }
    }

    private void drawCutter(Cutter cutter) {
        Point beg = cutter.getTopLeft();
        Point end = cutter.getBottomRight();
        drawCutter(beg.getX(), beg.getY(), end.getX(), end.getY());
    }

    private void drawCutter(double x1, double y1, double x2, double y2) {
        Color color = cutterPicker.getValue();
        if ((x1 == x2 || y1 == y2)) {
            LineDrawer.DigitalDiffAnalyzeDraw(canvas, x1, y1, x2, y2, color);
            return;
        }

        LineDrawer.DigitalDiffAnalyzeDraw(canvas, x1, y1, x1, y2, color);
        LineDrawer.DigitalDiffAnalyzeDraw(canvas, x1, y2, x2, y2, color);
        LineDrawer.DigitalDiffAnalyzeDraw(canvas, x2, y2, x2, y1, color);
        LineDrawer.DigitalDiffAnalyzeDraw(canvas, x2, y1, x1, y1, color);
    }

    private void setAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setTitle("Произошла ошибка :(");
        alert.setHeaderText("ОШИБКА");
        alert.show();
    }
}
