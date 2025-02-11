package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class CrosshairChart extends JFrame implements ChartMouseListener {

    private ChartPanel chartPanel;
    private double crosshairX = Double.NaN;
    private double crosshairY = Double.NaN;

    public CrosshairChart(String title) {
        super(title);

        XYSeriesCollection dataset = createDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Crosshair Chart Example",
                "X-Axis",
                "Y-Axis",
                dataset
        );

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartPanel.addChartMouseListener(this); // Слушаем события мыши

        setContentPane(chartPanel);
    }

    private XYSeriesCollection createDataset() {
        XYSeries series = new XYSeries("Series 1");
        series.add(1.0, 2.0);
        series.add(2.0, 4.0);
        series.add(3.0, 6.0);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }


    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
        JFreeChart chart = event.getChart();
        XYPlot plot = chart.getXYPlot();
        double domainAxisLocation = plot.getDomainAxis().java2DToValue(event.getTrigger().getX(), dataArea, plot.getDomainAxisEdge());
        double rangeAxisLocation = plot.getRangeAxis().java2DToValue(event.getTrigger().getY(), dataArea, plot.getRangeAxisEdge());

        crosshairX = domainAxisLocation;
        crosshairY = rangeAxisLocation;
        chartPanel.repaint(); // Перерисовываем панель, чтобы отобразить перекрестие
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CrosshairChart example = new CrosshairChart("JFreeChart Crosshairs");
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
