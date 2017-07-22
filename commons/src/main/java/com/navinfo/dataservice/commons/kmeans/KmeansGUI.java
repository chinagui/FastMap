/*
 * This file is part of ekmeans.
 *
 * ekmeans is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * ekmeans is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Foobar. If not, see <http://www.gnu.org/licenses/>.
 * 
 * ekmeans  Copyright (C) 2012  Pierre-David Belanger <pierredavidbelanger@gmail.com>
 * 
 * Contributor(s): Pierre-David Belanger <pierredavidbelanger@gmail.com>
 */
package com.navinfo.dataservice.commons.kmeans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Random;

public class KmeansGUI {

    private static final int MIN = 0;
    private static final int MAX = 1;
    private static final int LEN = 2;

    private static final int X = 0;
    private static final int Y = 1;

    private static final int RESOLUTION = 300;
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private JToolBar toolBar;
    private JTextField nTextField;
    private JTextField kTextField;
    private JCheckBox equalCheckBox;
    private JTextField debugTextField;
    private JPanel canvaPanel;
    private JLabel statusBar;
    private KPoint[] centroids = null;
    private KPoint[] points = null;
    private double[][] minmaxlens = null;
    private Kmeans eKmeans = null;

    public KmeansGUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(RESOLUTION + 100, RESOLUTION + 100));
        frame.setPreferredSize(new Dimension(RESOLUTION * 2, RESOLUTION * 2));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        frame.setContentPane(contentPanel);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        contentPanel.add(toolBar, BorderLayout.NORTH);

        JButton csvImportButton = new JButton();
        csvImportButton.setAction(new AbstractAction(" Import CSV ") {
            public void actionPerformed(ActionEvent ae) {
                csvImport();
            }
        });
        toolBar.add(csvImportButton);

        JButton csvExportButton = new JButton();
        csvExportButton.setAction(new AbstractAction(" Export CSV ") {
            public void actionPerformed(ActionEvent ae) {
                csvExport();
            }
        });
        toolBar.add(csvExportButton);

        JLabel nLabel = new JLabel("n:");
        toolBar.add(nLabel);

        nTextField = new JTextField("1000");
        toolBar.add(nTextField);

        JButton randomButton = new JButton();
        randomButton.setAction(new AbstractAction(" Random ") {
            public void actionPerformed(ActionEvent ae) {
                random();
            }
        });
        toolBar.add(randomButton);

        JLabel kLabel = new JLabel("k:");
        toolBar.add(kLabel);

        kTextField = new JTextField("5");
        toolBar.add(kTextField);

        JLabel equalLabel = new JLabel("equal:");
        toolBar.add(equalLabel);

        equalCheckBox = new JCheckBox("");
        toolBar.add(equalCheckBox);

        JLabel debugLabel = new JLabel("debug:");
        toolBar.add(debugLabel);

        debugTextField = new JTextField("0");
        toolBar.add(debugTextField);

        JButton runButton = new JButton();
        runButton.setAction(new AbstractAction(" Start ") {
            public void actionPerformed(ActionEvent ae) {
                start();
            }
        });
        toolBar.add(runButton);

        canvaPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                KmeansGUI.this.paint(g, getWidth(), getHeight());
            }
        };
        contentPanel.add(canvaPanel, BorderLayout.CENTER);

        statusBar = new JLabel(" ");
        contentPanel.add(statusBar, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private void enableToolBar(boolean enabled) {
        for (Component c : toolBar.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    private void csvImport() {
        enableToolBar(false);
        eKmeans = null;
        try {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(toolBar);
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            minmaxlens = new double[][]{
                {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
                {0d, 0d}
            };
            java.util.List points = new ArrayList();
            BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
            String line;
            int count=0;
            Random random = new Random();
            while ((line = reader.readLine()) != null) {
                count++;
//                String[] pointString = line.split(",");
//                KPoint point = new KPoint();
//                point.setX(Double.parseDouble(pointString[X].trim()));
//                point.setY(Double.parseDouble(pointString[Y].trim()));
//                point.setCount(random.nextInt(200));
//                points.add(point);
                int grid = Integer.valueOf(line.trim());
                KPoint point = new KPoint(grid, random.nextInt(500));
                point.setX(point.getX()/500);
                point.setY(point.getY()/500);
                points.add(point);
                if (point.getX() < minmaxlens[MIN][X]) {
                    minmaxlens[MIN][X] = point.getX();
                }
                if (point.getY() < minmaxlens[MIN][Y]) {
                    minmaxlens[MIN][Y] = point.getY();
                }
                if (point.getX() > minmaxlens[MAX][X]) {
                    minmaxlens[MAX][X] = point.getX();
                }
                if (point.getY() > minmaxlens[MAX][Y]) {
                    minmaxlens[MAX][Y] = point.getY();
                }

                if(count>=1000){
                    break;
                }
            }
            minmaxlens[LEN][X] = minmaxlens[MAX][X] - minmaxlens[MIN][X];
            minmaxlens[LEN][Y] = minmaxlens[MAX][Y] - minmaxlens[MIN][Y];
            reader.close();
            this.points = (KPoint[]) points.toArray(new KPoint[points.size()]);
            nTextField.setText(String.valueOf(this.points.length));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            canvaPanel.repaint();
            enableToolBar(true);
        }
    }

    private void csvExport() {
    }

    private void random() {
        enableToolBar(false);
        eKmeans = null;
        int n = Integer.parseInt(nTextField.getText());
        points = new KPoint[n];
        minmaxlens = new double[][]{
            {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
            {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
            {0d, 0d}
        };
        for (int i = 0; i < n; i++) {
            points[i].setX(RANDOM.nextDouble());
            points[i].setY(RANDOM.nextDouble());
            if (points[i].getX() < minmaxlens[MIN][X]) {
                minmaxlens[MIN][X] = points[i].getX();
            }
            if (points[i].getY() < minmaxlens[MIN][Y]) {
                minmaxlens[MIN][Y] = points[i].getY();
            }
            if (points[i].getX() > minmaxlens[MAX][X]) {
                minmaxlens[MAX][X] = points[i].getX();
            }
            if (points[i].getY() > minmaxlens[MAX][Y]) {
                minmaxlens[MAX][Y] = points[i].getY();
            }
        }
        minmaxlens[LEN][X] = minmaxlens[MAX][X] - minmaxlens[MIN][X];
        minmaxlens[LEN][Y] = minmaxlens[MAX][Y] - minmaxlens[MIN][Y];
        canvaPanel.repaint();
        enableToolBar(true);
    }

    private void start() {
        if (points == null) {
            random();
        }
        new Thread(new Runnable() {
            public void run() {
                enableToolBar(false);
                try {
                    KmeansGUI.this.run();
                } finally {
                    enableToolBar(true);
                }
            }
        }).start();
    }

    private void run() {
        try {
            URL url = new URL("http://staticmap.openstreetmap.de/staticmap.php?center=" + (minmaxlens[MIN][X] + (minmaxlens[LEN][X] / 2d)) + "," + (minmaxlens[MIN][Y] + (minmaxlens[LEN][Y] / 2d)) + "&zoom=14&size=" +  canvaPanel.getWidth() + "x" + canvaPanel.getHeight() + "&maptype=mapnik");
            System.out.println("url:" + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        int k = Integer.parseInt(kTextField.getText());
        boolean equal = equalCheckBox.isSelected();
        int debugTmp = 0;
        try {
            debugTmp = Integer.parseInt(debugTextField.getText());
        } catch (NumberFormatException ignore) {
        }
        final int debug = debugTmp;

        Kmeans.Listener listener = null;
        if (debug > 0) {
            listener = new Kmeans.Listener() {
                public void iteration(int iteration, int move) {
                    statusBar.setText(MessageFormat.format("iteration {0} move {1}", iteration, move));
                    canvaPanel.repaint();
                    try {
                        Thread.sleep(debug);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        eKmeans = new Kmeans(points, k, equal, listener);
        long time = System.currentTimeMillis();
        eKmeans.run();
        time = System.currentTimeMillis() - time;
        statusBar.setText(MessageFormat.format("EKmeans run in {0}ms", time));
        canvaPanel.repaint();
    }

    private void paint(Graphics g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        if (minmaxlens == null) {
            return;
        }
        double widthRatio = (width - 6d) / minmaxlens[LEN][X];
        double heightRatio = (height - 6d) / minmaxlens[LEN][Y];
        if (points == null) {
            return;
        }
        g.setColor(Color.BLACK);
        for (int i = 0; i < points.length; i++) {
            int px = 3 + (int) (widthRatio * (points[i].getX() - minmaxlens[MIN][X]));
            int py = 3 + (int) (heightRatio * (points[i].getY() - minmaxlens[MIN][Y]));
            g.drawRect(px - 2, py - 2, 4, 4);
        }
        if (eKmeans == null) {
            return;
        }
        int[] assignments = eKmeans.getAssignments();
        int[] counts = eKmeans.getCounts();
        centroids = eKmeans.getCentroids();
        int s = 225 / centroids.length;
        for (int i = 0; i < points.length; i++) {
            int assignment = assignments[i];
            if (assignment == -1) {
                continue;
            }
            int cx = 3 + (int) (widthRatio * (centroids[assignment].getX() - minmaxlens[MIN][X]));
            int cy = 3 + (int) (heightRatio * (centroids[assignment].getY() - minmaxlens[MIN][Y]));
            int px = 3 + (int) (widthRatio * (points[i].getX() - minmaxlens[MIN][X]));
            int py = 3 + (int) (heightRatio * (points[i].getY() - minmaxlens[MIN][Y]));
            int c = assignment * s;
            g.setColor(new Color(c, c, c));
            g.drawLine(cx, cy, px, py);
        }
        g.setColor(Color.GREEN);
        for (int i = 0; i < centroids.length; i++) {
            int cx = 3 + (int) (widthRatio * (centroids[i].getX() - minmaxlens[MIN][X]));
            int cy = 3 + (int) (heightRatio * (centroids[i].getY() - minmaxlens[MIN][Y]));
            g.drawLine(cx, cy - 2, cx, cy + 2);
            g.drawLine(cx - 2, cy, cx + 2, cy);
            int count = counts[i];
            g.drawString(String.valueOf(count), cx, cy);
        }
    }

    public static void main(String[] args) {
        new KmeansGUI();
    }
}
