package org.example;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JFreeChartModel {
    public static void viewVTChart(MorrisLecar model) {
        Map<OutputType, List<Double>> res = model.start();
        double Vrest =  Double.valueOf(model.getV());

        //Создаем набор данных
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<Double> times = res.get(OutputType.TIME);
        List<Double> Vs = res.get(OutputType.V);
        XYSeries VSeries = new XYSeries("V Data");
        for (int j = 0; j < times.size(); j++){
            VSeries.add(times.get(j), Vs.get(j));
        }
        dataset.addSeries(VSeries);

        //Создаем диаграмму
        JFreeChart chart = ChartFactory.createXYLineChart(
                "V(t) with GK=" + model.getGK() +
                        "; GNa=" + model.getGNa() +
                        "; GM=" + model.getGM() +
                        "; GAHP=" + model.getGAHP() + "(мСм/см^2)",
                "time",
                "V",
                dataset
        );

        //Получаем доступ к оси Y и включаем автомасштабирование
        NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        double maxY = Vs.get(0);
        for(double V : Vs){
            maxY = V > maxY ? V : maxY;
        }
        rangeAxis.setRange(model.getVrest() - 5, maxY + 5);

        //Создаем панель диаграммы
        ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        JFrame frame = new JFrame("Model Plot");
        frame.setContentPane(chartPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void viewVTControlVars(MorrisLecar model) {
        Map<OutputType, List<Double>> res = model.start();
        List<Double> times = res.get(OutputType.TIME);

        Map<OutputType, Double> mins = new HashMap<>();
        Map<OutputType, Double> maxs = new HashMap<>();
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.Z, OutputType.H}){
            mins.put(t, res.get(t).get(0));
            maxs.put(t, res.get(t).get(0));
        }

        //Создаем набор данных
        Map<OutputType, XYSeriesCollection> datasets = new HashMap<>();
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.Z, OutputType.H}){
            datasets.put(t, new XYSeriesCollection());
            XYSeries series = new XYSeries(t + " Data");
            List<Double> values = res.get(t);
            for (int j = 0; j < times.size(); j++){
                double val = values.get(j);
                series.add(times.get(j), Double.valueOf(val));
                if (val > maxs.get(t)){
                    maxs.put(t, val);
                } else if (val < mins.get(t)){
                    mins.put(t, val);
                }
            }
            datasets.get(t).addSeries(series);
        }

        Map<OutputType, ChartPanel> chartsPanels = new HashMap<>();
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.Z, OutputType.H}){
            JFreeChart chart = ChartFactory.createXYLineChart(
                    t + "(t) with GK=" + model.getGK() +
                            "; GNa=" + model.getGNa() +
                            "; GM=" + model.getGM() +
                            "; GAHP=" + model.getGAHP() + "(мСм/см^2)",
                    "time", t.name(), datasets.get(t)
            );
            NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
            rangeAxis.setRange(
                    mins.get(t) - 0.05 * mins.get(t),
                    maxs.get(t) + 0.05 * maxs.get(t)
            );

            chartsPanels.put(t, new ChartPanel(chart));
        }

        JPanel mainPanel = new JPanel(new GridLayout(3, 1));
        mainPanel.add(chartsPanels.get(OutputType.W));
        mainPanel.add(chartsPanels.get(OutputType.Z));
        mainPanel.add(chartsPanels.get(OutputType.H));
        JFrame frame = new JFrame("Model Plot Controls");
        frame.setContentPane(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void goWithChange(double GAHP){
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(100);
        model.setDt(0.1);
        model.setI_DC(35);

        model.setGAHP(GAHP);
        viewVTChart(model);
    }

    public static void main(String[] args) {
        goWithChange(1);
        goWithChange(3);
        goWithChange(5);
        goWithChange(9);
        //viewVTControlVars(model);
    }
}
