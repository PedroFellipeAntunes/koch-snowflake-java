package Data;

public class Line {
    private Point p1 = null;
    private Point p2 = null;
    
    private Point collisionPoint = null;
    
    private double nx;
    private double ny;
    
    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        
        calculateNormal();
    }
    
    public Line() {
        //Empty new line
    }
    
    public Line(Line other) {
        this.p1 = new Point(other.getP1().getX(), other.getP1().getY());
        this.p2 = new Point(other.getP2().getX(), other.getP2().getY());
        
        calculateNormal();
    }
    
    private void calculateNormal() {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        
        this.nx = -dy / length;
        this.ny = dx / length;
    }
    
    public double getNx() {
        return nx;
    }
    
    public double getNy() {
        return ny;
    }
    
    public Point getP1() {
        return p1;
    }
    
    public void setP1(Point p1) {
        this.p1 = p1;
        
        if (p2 != null) {
            calculateNormal();
        }
    }
    
    public Point getP2() {
        return p2;
    }
    
    public void setP2(Point p2) {
        this.p2 = p2;
        
        if (p1 != null) {
            calculateNormal();
        }
    }
    
    public Point getCollisionPoint() {
        return collisionPoint;
    }
    
    public void setCollisionPoint(Point collisionPoint) {
        this.collisionPoint = collisionPoint;
    }

    public boolean compareLines(Line compared) {
        return this.p1.getX() == compared.getP1().getX()
                && this.p1.getY() == compared.getP1().getY()
                && this.p2.getX() == compared.getP2().getX()
                && this.p2.getY() == compared.getP2().getY();
    }
}