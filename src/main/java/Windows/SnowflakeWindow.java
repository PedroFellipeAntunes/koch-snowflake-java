/*
TODO: Implement raycasting based on angle using spatial grouping
*/

/*
Koch Snowflake
Based on The Coding Train:
    Coding Challenge #129: Koch Fractal Snowflake - https://www.youtube.com/watch?v=X8bXDKqMsXE
*/

package Windows;

import Data.Line;
import Data.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class SnowflakeWindow {
    private final int windowH = 600;
    private final int windowW = 600;
    
    private JFrame frame;
    private JPanel panel;
    private final JLabel genLabel;
    private final JLabel controlLabel;
    private JButton polyButton;
    private JTextField vertTextField;
    
    //Light control
    private final int reflectionLimit = 10;
    private double castAngle = 0.0;
    
    private ArrayList<Line> raycast = new ArrayList<>();
    
    //Fractal control
    private final int polygonRadius = 250;
    private int vertices = 6;
    private boolean polygon = false;
    private int generation = 0;
    
    private ArrayList<Line> snowflake = new ArrayList<>();
    private ArrayList<ArrayList<Line>> recursivePolygons = new ArrayList<>();
    
    private double zoom = 1.0;
    private double rotationAngle = 0.0;
    
    public SnowflakeWindow() {
        frame = new JFrame("Koch Snowflake Fractal");
        frame.setSize(windowW, windowH);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.BLACK);
        
        //Create first line/polygon
        if (!polygon) {
            createFirstLine();
        } else {
            snowflake = createPolygon(windowW / 2, windowH / 2, polygonRadius, vertices);
//            createRecursivePolygons(windowW / 2, windowH / 2, polygonRadius, vertices);
//            raycasting();
        }
        
        //Generation counting label
        genLabel = new JLabel("" + generation);
        genLabel.setBackground(Color.BLACK);
        genLabel.setForeground(Color.WHITE);
        genLabel.setSize(10, 10);
        
        //Draw lines
        panel = new DrawPanel();
        panel.setBackground(Color.BLACK);
        panel.setPreferredSize(new Dimension(windowW, windowH));
        
        //Label for controls
        controlLabel = new JLabel("<html>ENTER == Next generation<br>"
                + "BACKSPACE || DELETE == reset<br>"
                + "RIGHT || LEFT == rotate<br>"
                + "+ || - == ZOOM</html>");
        controlLabel.setBackground(Color.BLACK);
        controlLabel.setForeground(Color.WHITE);
        controlLabel.setSize(225, 70);
        
        //Button that sets either polygon or line
        polyButton = new JButton("Polygon");
        setUpButton(polyButton);
        
        polyButton.addActionListener(l -> {
            polygon = !polygon;
            
            if (polygon == true) {
                polyButton.setBackground(Color.WHITE);
                polyButton.setForeground(Color.BLACK);
            } else {
                polyButton.setBackground(Color.BLACK);
                polyButton.setForeground(Color.WHITE);
            }
            
            frame.requestFocus();
            vertTextField.setVisible(polygon);
            reset();
        });
        
        //Textfield for vertice count
        vertTextField = new JTextField("" + vertices);
        vertTextField.setBackground(Color.WHITE);
        vertTextField.setForeground(Color.BLACK);
        vertTextField.setSize(100, 40);
        vertTextField.setVisible(polygon);
        
        vertTextField.addActionListener(e -> {
            String text = vertTextField.getText();
            
            if (!text.isEmpty()) {
                text = text.substring(0, Math.min(text.length(), 3));
                
                int value = Integer.parseInt(text);
                
                value = Math.max(2, value);
                
                vertices = value;
                vertTextField.setText("" + vertices);
                
                reset();
            } else {
                vertTextField.setText("" + vertices);
            }
            
            frame.requestFocus();
        });
        
        //Panel for layers
        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(windowW, windowH));
        panel.setBounds(0, 0, windowW, windowH);
        genLabel.setBounds(windowW / 2 - genLabel.getWidth(), windowH / 2 - genLabel.getHeight(), genLabel.getWidth(), genLabel.getHeight());
        controlLabel.setBounds(windowW - controlLabel.getWidth(), 0, controlLabel.getWidth(), controlLabel.getHeight());
        polyButton.setBounds(0, 0, polyButton.getWidth(), polyButton.getHeight());
        vertTextField.setBounds(0, polyButton.getHeight() + 5, vertTextField.getWidth(), vertTextField.getHeight());
        
        layered.add(panel, Integer.valueOf(1));
        layered.add(genLabel, Integer.valueOf(2));
        layered.add(controlLabel, Integer.valueOf(3));
        layered.add(polyButton, Integer.valueOf(4));
        layered.add(vertTextField, Integer.valueOf(5));
        
        frame.add(layered, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        frame.setVisible(true);
        
        //Next generation
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS -> zoomIn();
                    case KeyEvent.VK_MINUS -> zoomOut();
                    case KeyEvent.VK_LEFT -> rotateLeft();
                    case KeyEvent.VK_RIGHT -> rotateRight();
                    case KeyEvent.VK_ENTER -> nextGeneration();
                    case KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE -> reset();
                }
                
                panel.repaint();
            }
        });
    }
    
    private void nextGeneration() {
        ArrayList<Line> tempSnowflake = new ArrayList<>();
        
        for (Line l : snowflake) {
            tempSnowflake.addAll(breakLine(l));
        }
        
        snowflake = tempSnowflake;
//        recursivePolygons.set(recursivePolygons.size() - 1, snowflake);
        generation++;
        genLabel.setText("" + generation);
    }
    
    private void reset() {
        recursivePolygons.clear();
        snowflake.clear();
        raycast.clear();
        
        if (!polygon) {
            createFirstLine();
        } else {
            snowflake = createPolygon(windowW / 2, windowH / 2, polygonRadius, vertices);
//            createRecursivePolygons(windowW / 2, windowH / 2, polygonRadius, vertices);
//            raycasting();
        }
        
        generation = 0;
        genLabel.setText("" + generation);
        
        panel.repaint();
    }
    
    private void setUpButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setSize(100, 40);
        button.setFocusable(false);
    }
    
    private void createFirstLine() {
        Point p1 = new Point(0 + 10, windowH / 2);
        Point p2 = new Point(windowW - 10, windowH / 2);
        
        Line newLine = new Line(p1, p2);
        
        snowflake.addFirst(newLine);
    }
    
    private ArrayList<Line> createPolygon(int centralX, int centralY, int radius, int verticeCount) {
        ArrayList<Line> newPolygon = new ArrayList<>();
        
        double angle = 2 * Math.PI / verticeCount;
        
        Point[] pointsArray = new Point[verticeCount];
        
        for (int i = 0; i < verticeCount; i++) {
            double x = centralX + radius * Math.cos(i * angle + Math.PI / 2);
            double y = centralY + radius * Math.sin(i * angle + Math.PI / 2);
            
            pointsArray[i] = new Point(x, y);
            
            if (i > 0) {
                Line newLine = new Line(pointsArray[i - 1], pointsArray[i]);
                newPolygon.add(newLine);
            }
        }
        
        Line lastLine = new Line(pointsArray[verticeCount - 1], pointsArray[0]);
        newPolygon.add(lastLine);
        
        return newPolygon;
    }
    
    private void createRecursivePolygons(int centralX, int centralY, int radius, int verticeCount) {
        //Create snowflake first
        snowflake = createPolygon(centralX, centralY, radius, verticeCount);
        
        if (verticeCount <= 3) {
            return;
        }
        
        int currentVerticeCount = lowestDivisible(verticeCount - 1);
        System.out.println("");
        //Divide by 3 until reaches triangle
        while (currentVerticeCount >= 3) {
            System.out.print(currentVerticeCount+", ");
            ArrayList<Line> newPolygon = createPolygon(centralX, centralY, radius, currentVerticeCount);
            recursivePolygons.add(newPolygon);
            
            currentVerticeCount /= 3;
        }
        
        //Add to end of list
        recursivePolygons.add(snowflake);
    }
    
    private int lowestDivisible(int verticeCount) {
        while (verticeCount % 3 != 0) {
            verticeCount--;
        }
        
        return verticeCount;
    }
    
    private void raycasting() {
        // Define hard start point for first line
        int centerX = windowW / 2;
        int centerY = windowH / 2;
        
        Line startingLine = new Line();
        
        startingLine.setP1(new Point(centerX, centerY));
        
        double angleInRadians = Math.toRadians(castAngle);
        
        startingLine.setP2(genericEndPoint(centerX, centerY, angleInRadians));
        System.out.println("CHECKING FOR FIRST LINE");
        Point collisionPoint = recursiveCollisionSearch(startingLine);
        
        // In the unlikely case where there is no collision
        if (collisionPoint == null) {
            raycast.add(startingLine);
        } else {
            startingLine.setP2(collisionPoint);
            
            raycast.add(startingLine);
            
            processReflections(raycast.get(0), 1);
        }
    }
    
    private Point genericEndPoint(double x, double y, double angle) {
        double length = windowW * windowH;
        double endX = x + length * Math.cos(angle);
        double endY = y + length * Math.sin(angle);
        
        return new Point(endX, endY);
    }
    
    private void processReflections(Line parentLine, int currentCount) {
        System.out.println("Current COUNT: " + currentCount);
        if (currentCount >= reflectionLimit) {
            return;
        }

        // Calcular o ângulo da linha pai (linha de incidência)
        double angleToLine = Math.atan2(parentLine.getP2().getY() - parentLine.getP1().getY(),
                parentLine.getP2().getX() - parentLine.getP1().getX());

        // AQUI É O PONTO ONDE A LINHA ATINGE A SUPERFÍCIE (COLISÃO)
        Point collisionPoint = recursiveCollisionSearch(parentLine);
        if (collisionPoint == null) {
            return; // Não houve colisão, não faz reflexão
        }

        // Calcular a normal (supondo que o objeto é vertical, a normal é horizontal)
        // Aqui você deve ajustar a normal com base na linha da colisão real
        double normalAngle = angleToLine + Math.PI / 2; // Normal é perpendicular à linha
        double angleOfIncidence = angleToLine - normalAngle; // Ângulo de incidência

        // Calcular o ângulo de reflexão
        double reflectedAngle = normalAngle - angleOfIncidence; // Reflexão é igual ao ângulo de incidência

        // Criar nova linha refletida
        Point newP1 = parentLine.getP2(); // O ponto de partida é onde a linha pai terminou

        Line newLine = new Line();
        newLine.setP1(newP1);
        newLine.setP2(genericEndPoint(newP1.getX(), newP1.getY(), reflectedAngle));

        // Verificar colisão com a nova linha refletida
        System.out.println("CHECKING FOR REFLECTIONS");
        Point collisionPointReflected = recursiveCollisionSearch(newLine);

        // Caso não haja colisão
        if (collisionPointReflected == null) {
            raycast.add(newLine);
        } else {
            newLine.setP2(collisionPointReflected);
            raycast.add(newLine);

            // Chamada recursiva para processar a próxima reflexão
            processReflections(newLine, currentCount + 1);
        }
    }
    
    private Point recursiveCollisionSearch(Line rayLine) {
        int currentIndex = 0;
        
        ArrayList<Line> currentPolygon;
        
        Point collisionPoint = null;
        
        for (int i = 0; i < recursivePolygons.size(); i++) {
            currentPolygon = recursivePolygons.get(i);
            
            int numLines = currentPolygon.size();
            int step = numLines / 3;
            
            for (int j = currentIndex; j < currentPolygon.size(); j++) {
                Line currentLine = currentPolygon.get(j);
                
                collisionPoint = checkCollision(rayLine, currentLine);
                
                if (collisionPoint != null) {
                    System.out.println("FOUND COLLISION ON P: "+i+", LINE: "+j);
                    currentIndex = j;
                    break;
                } else {
                    System.out.println("DOES NOT COLLIDE WITH P: "+i+", LINE: "+j);
                }
            }
        }
        
        return collisionPoint;
    }
    
    public static Point checkCollision(Line lineA, Line lineB) {
        double x1 = lineA.getP1().getX();
        double y1 = lineA.getP1().getY();
        double x2 = lineA.getP2().getX();
        double y2 = lineA.getP2().getY();

        double x3 = lineB.getP1().getX();
        double y3 = lineB.getP1().getY();
        double x4 = lineB.getP2().getX();
        double y4 = lineB.getP2().getY();

        double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denominator == 0) {
            return null;
        }

        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denominator;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denominator;

        //return new Point(intersectX, intersectY);
        
        // Verifica se o ponto de interseção está dentro dos limites dos segmentos de linha
        if (isBetween(x1, x2, intersectX) && isBetween(y1, y2, intersectY) &&
            isBetween(x3, x4, intersectX) && isBetween(y3, y4, intersectY)) {
            return new Point(intersectX, intersectY);
        } else {
            return null; // O ponto de interseção não está dentro dos segmentos de linha
        }
    }
    
    private static boolean isBetween(double a, double b, double c) {
        return c >= Math.min(a, b) && c <= Math.max(a, b);
    }
    
    private ArrayList<Line> breakLine(Line line) {
        ArrayList<Line> newSnowflake = new ArrayList<>();
        
        Point p1 = line.getP1();
        Point p2 = line.getP2();
        
        //Get distance P1 P2
        double distance = getDistance(p1, p2);
        
        //Linear interpolation
        Point p3 = interpolate(p1, p2, distance, 1, 3);
        Point p4 = interpolate(p1, p2, distance, 2, 3);
        
        //For middle point
        Point p5 = getPointBasedOnAngle(p3, p4);
        
        //Create lines and add to snowflake
        line.setP2(p3);
        Line line2 = new Line(p3, p5);
        Line line3 = new Line(p5, p4);
        Line line4 = new Line(p4, p2);
        
        newSnowflake.add(line);
        newSnowflake.add(line2);
        newSnowflake.add(line3);
        newSnowflake.add(line4);
        
        return newSnowflake;
    }
    
    private Point getPointBasedOnAngle(Point p1, Point p2) {
        double dX = p2.getX() - p1.getX();
        double dY = p2.getY() - p1.getY();
        
        double distance = Math.sqrt((dX * dX) + (dY * dY));
        double angle = Math.atan2(dY, dX);
        
        angle += (Math.PI / 3) * -1;
        
        double x = p1.getX() + distance * Math.cos(angle);
        double y = p1.getY() + distance * Math.sin(angle);
        
        return new Point(x, y);
    }
    
    private Point interpolate(Point p1, Point p2, double distance, int multiplier, int division) {
        if (distance == 0) {
            distance = 1;
        }
        
        double x = p1.getX() + (multiplier * distance / division) * (p2.getX() - p1.getX()) / distance;
        double y = p1.getY() + (multiplier * distance / division) * (p2.getY() - p1.getY()) / distance;
        
        return new Point(x, y);
    }
    
    private double getDistance(Point p1, Point p2) {
        double x = p2.getX() - p1.getX();
        double y = p2.getY() - p1.getY();
        
        x = Math.pow(x, 2);
        y = Math.pow(y, 2);
        
        return (Math.sqrt(x + y)) / 4;
    }
    
    public void zoomIn() {
        zoom *= 1.5;
    }
    
    public void zoomOut() {
        zoom /= 1.5;
    }
    
    public void rotateLeft() {
        rotationAngle -= Math.PI / 180.0;
    }
    
    public void rotateRight() {
        rotationAngle += Math.PI / 180.0;
    }
    
    private class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.scale(zoom, zoom);
            g2d.rotate(rotationAngle, panel.getWidth() / 2, panel.getHeight() / 2);
            
            //Draw snowflake
            g.setColor(Color.WHITE);
            
//            for (int j = 0; j < recursivePolygons.size(); j++) {
//                ArrayList<Line> polygon = recursivePolygons.get(j);
//                
//                float hue = (float) j / recursivePolygons.size(); // Normaliza o hue entre 0 e 1
//                g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
//                
//                for (int i = 0; i < polygon.size(); i++) {
//                    Line current = polygon.get(i);
//
//                    g.drawLine((int) current.getP1().getX(),
//                            (int) current.getP1().getY(),
//                            (int) current.getP2().getX(),
//                            (int) current.getP2().getY());
//                }
//            }
            
            for (int i = 0; i < snowflake.size(); i++) {
                Line current = snowflake.get(i);
                
                g.drawLine((int) current.getP1().getX(),
                        (int) current.getP1().getY(),
                        (int) current.getP2().getX(),
                        (int) current.getP2().getY());
            }
            
            //Draw raycast
//            if (polygon) {
//                g.setColor(Color.WHITE);
//
//                for (int i = 0; i < raycast.size(); i++) {
//                    Line current = raycast.get(i);
//                    
//                    g.drawLine((int) current.getP1().getX(),
//                            (int) current.getP1().getY(),
//                            (int) current.getP2().getX(),
//                            (int) current.getP2().getY());
//                    
//                    g.drawOval((int) current.getP2().getX() - 5,
//                            (int) current.getP2().getY() - 5, 10, 10);
//                }
//            }
        }
    }
}