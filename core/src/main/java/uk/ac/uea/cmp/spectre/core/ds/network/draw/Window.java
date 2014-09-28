/*
 * Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 * Copyright (C) 2014  UEA School of Computing Sciences
 *
 * This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.core.ds.network.draw;

import uk.ac.uea.cmp.spectre.core.ds.network.Edge;
import uk.ac.uea.cmp.spectre.core.ds.network.NetworkLabel;
import uk.ac.uea.cmp.spectre.core.ds.network.Vertex;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author balvociute
 */
public class Window extends JPanel {
    private int[][] lines;
    private int[][] points;

    private int[][] lastLines;
    private int[][] lastPoints;

    private Set<double[]> lastLinesSet;
    private Set<double[]> lastPointsSet;

    private List<double[]> pointsToMark = new LinkedList<>();

    Color[] colors = new Color[3];

    double ratio;
    double minX;
    double minY;

    int addX = 200;
    int addY = 100;

    public Window() {
        colors[0] = Color.red;
        colors[1] = Color.green;
        colors[2] = Color.BLUE;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < lines.length; i++) {
            //g.drawLine(lines[i][0], lines[i][1], lines[i][2], lines[i][3]);
            g2.setStroke(new BasicStroke(lines[i][4]));
            g2.drawLine(lines[i][0], getHeight() - lines[i][1], lines[i][2], getHeight() - lines[i][3]);   //thick
        }
        for (int i = 0; i < points.length; i++) {
            g.setColor(Color.black);
            int minus = points[i][2] / 2;
            g.fillOval(points[i][0] - minus, getHeight() - points[i][1] - minus, points[i][2], points[i][2]);
        }

        if (lastLines != null) {
            for (int i = 0; i < lastLines.length; i++) {
                g.setColor(Color.cyan);
                g.drawLine(lastLines[i][0], getHeight() - lastLines[i][1], lastLines[i][2], getHeight() - lastLines[i][3]);
            }
        }
        if (lastPoints != null) {
            for (int i = 0; i < lastPoints.length; i++) {
                g.setColor(Color.cyan);
                g.fillOval(lastPoints[i][0] - 2, getHeight() - lastPoints[i][1] - 2, 5, 5);
            }
        }

        for (int i = 0; i < pointsToMark.size(); i++) {
            double[] p = pointsToMark.get(i);
            int x = (int) ((p[0] - minX) * ratio) + addX;
            int y = (int) ((p[1] - minY) * ratio) + addY;
            g.setColor(colors[(int) p[2]]);
            g.fillOval(x - 3, getHeight() - y - 3, 7, 7);
        }

    }

    public static double[] getCorners(List<Vertex> vertices) {
        //0 - minX
        //1 - maxX
        //2 - minY
        //3 - maxY
        double[] corners = new double[4];

        Iterator<Vertex> vertexIt = vertices.iterator();
        int i = 0;
        while (vertexIt.hasNext()) {
            Vertex v = vertexIt.next();

            NetworkLabel l = v.getLabel();


            corners[0] = (i == 0 || corners[0] > v.getX()) ? v.getX() : corners[0];
            corners[1] = (i == 0 || corners[1] < v.getX()) ? v.getX() : corners[1];
            corners[2] = (i == 0 || corners[2] > v.getY()) ? v.getY() : corners[2];
            corners[3] = (i == 0 || corners[3] < v.getY()) ? v.getY() : corners[3];

//            if(v.getLabel() != null)
//            {
//                if(l.getOffsetX() < 0)
//                {
//                    corners[0] = (corners[0] > v.getX() + l.getOffsetX()) ? v.getX() + l.getOffsetX() : corners[0];
//                }
//                else if(l.getOffsetX() > 0)
//                {
//                    corners[1] = (corners[1] < v.getX() + l.getOffsetX()) ? v.getX() + l.getOffsetX() : corners[1];
//                }
//
//                if(l.getOffsetY() < 0)
//                {
//                    corners[2] = (corners[2] > v.getY() + l.getOffsetY()) ? v.getY() + l.getOffsetY() : corners[2];
//                }
//                else if(l.getOffsetY() > 0)
//                {
//                    corners[3] = (corners[3] < v.getY() + l.getOffsetY()) ? v.getY() + l.getOffsetY() : corners[3];
//                }
//            }

            i++;
        }
        return corners;
    }

    public void setGraph(Vertex net, double[] corners, int w, int h) {
        LinkedList<Edge> edges = net.getFirstEdge().collectEdges();

        lines = new int[edges.size()][5];
        Set<Integer[]> p = new HashSet<>();

        corners = getCorners(net.collectVertices());

        minX = corners[0];
        double maxX = corners[1];
        minY = corners[2];
        double maxY = corners[3];

        if (ratio == 0) {
            ratio = Math.min((double) (w) / Math.abs(maxX - minX), (double) (h) / Math.abs(maxY - minY)) * 0.85;
        }

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            lines[i][0] = (int) ((e.getBot().getX() - minX) * ratio) + addX;
            lines[i][1] = (int) ((e.getBot().getY() - minY) * ratio) + addY;
            lines[i][2] = (int) ((e.getTop().getX() - minX) * ratio) + addX;
            lines[i][3] = (int) ((e.getTop().getY() - minY) * ratio) + addY;
            lines[i][4] = e.getWidth();
        }

        LinkedList<Vertex> vertices = net.collectVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            if (v.getTaxa().size() > 0) {
                Integer[] xy = new Integer[3];
                xy[0] = (int) ((v.getX() - minX) * ratio) + addX;
                xy[1] = (int) ((v.getY() - minY) * ratio) + addY;
                xy[2] = 7;
                p.add(xy);
            }
            if (v.getElist().size() == 1) {
                Edge e = v.getElist().getFirst();
                Vertex vi = (e.getTop() == v) ? e.getBot() : e.getTop();
                Integer[] xy = new Integer[3];
                xy[0] = (int) ((vi.getX() - minX) * ratio) + addX;
                xy[1] = (int) ((vi.getY() - minY) * ratio) + addY;
                xy[2] = 5;
                p.add(xy);
            }
        }
        points = new int[p.size()][3];
        Iterator<Integer[]> pIt = p.iterator();
        int u = 0;
        while (pIt.hasNext()) {
            Integer[] xy = pIt.next();
            points[u][0] = xy[0];
            points[u][1] = xy[1];
            points[u][2] = xy[2];
            u++;
        }

        updateLast();
    }

    public void setLast(Set<double[]> lines, Set<double[]> points) {
        lastLinesSet = lines;
        lastPointsSet = points;
    }

    public void updateLast() {
        if (lastLinesSet != null && lastPointsSet != null) {
            lastLines = new int[lastLinesSet.size()][4];
            lastPoints = new int[lastPointsSet.size()][3];

            Iterator<double[]> lIt = lastLinesSet.iterator();
            int i = 0;
            while (lIt.hasNext()) {
                double[] ll = lIt.next();
                lastLines[i][0] = (int) ((ll[0] - minX) * ratio) + addX;
                lastLines[i][1] = (int) ((ll[1] - minY) * ratio) + addY;
                lastLines[i][2] = (int) ((ll[2] - minX) * ratio) + addX;
                lastLines[i][3] = (int) ((ll[3] - minY) * ratio) + addY;
                i++;
            }

            Iterator<double[]> pIt = lastPointsSet.iterator();
            i = 0;
            while (pIt.hasNext()) {
                double[] pp = pIt.next();
                lastPoints[i][0] = (int) ((pp[0] - minX) * ratio) + addX;
                lastPoints[i][1] = (int) ((pp[1] - minY) * ratio) + addY;
                lastPoints[i][2] = (int) pp[2];
                i++;
            }
        }
    }

    void markPoint(Vertex c0, int color) {
        double[] p = new double[3];
        p[0] = c0.getX();
        p[1] = c0.getY();
        p[2] = color;
        pointsToMark.add(p);
        this.repaint();
    }
}