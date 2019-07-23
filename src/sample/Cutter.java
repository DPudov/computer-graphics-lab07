package sample;

public class Cutter {
    private Point topLeft;
    private Point bottomRight;
    private boolean beginInit;
    private boolean endInit;

    public Cutter() {
        this.beginInit = false;
        this.endInit = false;
    }

    public Cutter(Point topLeft, Point bottomRight) {
        double x1 = topLeft.getX();
        double x2 = bottomRight.getX();

        double y1 = topLeft.getY();
        double y2 = bottomRight.getY();

        double xLeft = Math.min(x1, x2);
        double xRight = Math.max(x1, x2);

        double yTop = Math.min(y1, y2);
        double yBottom = Math.max(y1, y2);

        this.topLeft = new Point(xLeft, yTop);
        this.bottomRight = new Point(xRight, yBottom);
        this.beginInit = false;
        this.endInit = false;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public boolean isBeginInit() {
        return beginInit;
    }

    public void setBeginInit(boolean beginInit) {
        this.beginInit = beginInit;
    }

    public boolean isEndInit() {
        return endInit;
    }

    public void setEndInit(boolean endInit) {
        this.endInit = endInit;
    }

    public void clear() {
        setEndInit(false);
        setBeginInit(false);
    }
}
