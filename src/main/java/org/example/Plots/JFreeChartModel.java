package org.example.Plots;

import org.example.Elemets.MorrisLecar;
import org.example.Generator.StimGenerator;
import org.example.ModelInterfeses.VarType;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class JFreeChartModel {
    /**
     * Вывести обычный график Vm(t)
     * @param res
     * @param model
     */
    public static void viewVTChart(Map<VarType, List<Double>> res, MorrisLecar model) {
        //Создаем набор данных
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<Double> times = res.get(VarType.TIME);
        List<Double> Vs = res.get(VarType.V);
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
        double minY = Vs.get(0);
        for(double V : Vs){
            maxY = V > maxY ? V : maxY;
            minY = V < minY ? V : minY;
        }
        rangeAxis.setRange(minY - 5, maxY + 5);

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

    /**
     * Вывести график Vm(t) с графиком стимуляцией
     * @param res
     * @param model
     */
    public static void viewVTChartWithStim(Map<VarType, List<Double>> res, MorrisLecar model, Double[] stimWave) {
        //Создаем набор данных
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<Double> times = res.get(VarType.TIME);
        //Stim//////////////////////////////////////////////////////
        XYSeries StimSeries = new XYSeries("Stim Data");
        for (int j = 0; j < stimWave.length; j++){
            StimSeries.add(times.get(j), stimWave[j]);
        }
        dataset.addSeries(StimSeries);

        //Создаем диаграмму
        JFreeChart chartStim = ChartFactory.createXYLineChart(
                "IStim(t) мкА/см^2",
                "time",
                "IStim",
                dataset
        );

        //Получаем доступ к оси Y и включаем автомасштабирование
        NumberAxis rangeAxis = (NumberAxis) chartStim.getXYPlot().getRangeAxis();
        double maxY = stimWave[0];
        double minY = stimWave[0];
        for(double I : stimWave){
            maxY = I > maxY ? I : maxY;
            minY = I < minY ? I : minY;
        }
        rangeAxis.setRange(minY - 5, maxY + 5);
        //Создаем панель диаграммы
        ChartPanel chartPanelStim = new ChartPanel(chartStim);
        //Stim//////////////////////////////////////////////////////
        dataset = new XYSeriesCollection();
        //V(t)//////////////////////////////////////////////////////
        List<Double> Vs = res.get(VarType.V);
        XYSeries VSeries = new XYSeries("V Data");
        for (int j = 0; j < times.size(); j++){
            VSeries.add(times.get(j), Vs.get(j));
        }
        dataset.addSeries(VSeries);

        //Создаем диаграмму
        JFreeChart chartV = ChartFactory.createXYLineChart(
                "V(t) with GK=" + model.getGK() +
                        "; GNa=" + model.getGNa() +
                        "; GM=" + model.getGM() +
                        "; GAHP=" + model.getGAHP() + "(мСм/см^2)",
                "time",
                "V",
                dataset
        );

        //Получаем доступ к оси Y и включаем автомасштабирование
        rangeAxis = (NumberAxis) chartV.getXYPlot().getRangeAxis();
        maxY = Vs.get(0);
        minY = Vs.get(0);
        for(double V : Vs){
            maxY = V > maxY ? V : maxY;
            minY = V < minY ? V : minY;
        }
        rangeAxis.setRange(minY - 5, maxY + 5);
        //Создаем панель диаграммы
        ChartPanel chartPanelV = new ChartPanel(chartV);
        //V(t)//////////////////////////////////////////////////////

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));

        mainPanel.add(chartPanelStim);
        mainPanel.add(chartPanelV);

        JFrame frame = new JFrame("Model Plot Controls");
        frame.setContentPane(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Вывести график управляющих каналами переменных
     * @param res
     * @param model
     */
    public static void viewVTControlVars(Map<VarType, List<Double>> res, MorrisLecar model) {
        List<Double> times = res.get(VarType.TIME);

        Map<VarType, Double> mins = new HashMap<>();
        Map<VarType, Double> maxs = new HashMap<>();
        for (VarType t : new VarType[]{VarType.W, VarType.ZM, VarType.ZAHP, VarType.H}){
            mins.put(t, res.get(t).get(0));
            maxs.put(t, res.get(t).get(0));
        }

        //Создаем набор данных
        Map<VarType, XYSeriesCollection> datasets = new HashMap<>();
        for (VarType t : new VarType[]{VarType.W, VarType.ZM, VarType.ZAHP, VarType.H}){
            datasets.put(t, new XYSeriesCollection());
            XYSeries series = new XYSeries(t + " Data");
            List<Double> values = res.get(t);
            for (int j = 0; j < times.size(); j++){
                double val = values.get(j);
                series.add(times.get(j), Double.valueOf(val));
            }
            datasets.get(t).addSeries(series);
        }

        Map<VarType, ChartPanel> chartsPanels = new HashMap<>();
        for (VarType t : new VarType[]{VarType.W, VarType.ZM, VarType.ZAHP, VarType.H}){
            JFreeChart chart = ChartFactory.createXYLineChart(
                    t + "(t)",
                    "time", t.name(), datasets.get(t)
            );

            chartsPanels.put(t, new ChartPanel(chart));
        }

        JPanel mainPanel = new JPanel(new GridLayout(4, 1));
        mainPanel.add(chartsPanels.get(VarType.W));
        mainPanel.add(chartsPanels.get(VarType.ZM));
        mainPanel.add(chartsPanels.get(VarType.ZAHP));
        mainPanel.add(chartsPanels.get(VarType.H));
        JFrame frame = new JFrame("Model Plot Controls");
        frame.setContentPane(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Стимуляция постоянным током с изменением G
     * @param GAHP
     */
    public static void goDCWithChangeG(double GAHP, double GNa, double GK, double GM){
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(100);
        model.setDt(0.1);
        model.setIStim(35);

        model.setGAHP(GAHP);
        model.setGNa(GNa);
        model.setGK(GK);
        model.setGM(GM);
        Map<VarType, List<Double>> res = model.start();
        viewVTChart(res, model);
        viewVTControlVars(res, model);
    }

    /**
     * Стимуляция синусоидой с изменением G
     * @param GAHP
     * @param GNa
     * @param GK
     * @param GM
     */
    public static void goSinWaveWithChangeG(double GAHP, double GNa, double GK, double GM){
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(100);
        model.setDt(0.1);

        model.setGAHP(GAHP);
        model.setGNa(GNa);
        model.setGK(GK);
        model.setGM(GM);
        int len = (int) (model.getTotalTime()/model.getDt());
        Double[] wave = StimGenerator.generateSinusoid(len, 40, 1.5, 0.5);
        Map<VarType, List<Double>> res = model.startWithStimWave(wave);
        viewVTChartWithStim(res, model, wave);
    }

    /**
     * Стимуляция спайковой волной другого нейрона с изменением G
     * @param GAHP
     * @param GNa
     * @param GK
     * @param GM
     */
    public static void goSpikeWaveWithChangeG(double Gsyn, double GAHP, double GNa, double GK, double GM){
        //Input Neuron////////////////////////////////////////////
        int len = 1000;
        Double[] spikeWave = StimGenerator.generateSimplySpike(len, 30.0);
        for (int i = 0; i < spikeWave.length; i++) {
            spikeWave[i] = spikeWave[i] * Gsyn;
        }
        //Input Neuron////////////////////////////////////////////

        //Target Neuron///////////////////////////////////////////
        MorrisLecar modelTarget = new MorrisLecar();
        modelTarget.setTotalTime(100);
        modelTarget.setDt(0.1);

        modelTarget.setGAHP(GAHP);
        modelTarget.setGNa(GNa);
        modelTarget.setGK(GK);
        modelTarget.setGM(GM);
        Map<VarType, List<Double>> res = modelTarget.startWithStimWave(spikeWave);
        //Target Neuron///////////////////////////////////////////

        viewVTChartWithStim(res, modelTarget, spikeWave);
    }

    public static void main(String[] args) {
        //G мСм/см^2
        //goSpikeWaveWithChangeG(1,0.5, 24, 30, 1);
        //goSinWaveWithChangeG(1, 24, 30, 2);
        //*
        goDCWithChangeG(1, 24, 30, 2);
        //goDCWithChangeG(3, 24, 30, 2);
        //goDCWithChangeG(5, 24, 30, 2);
        //goDCWithChangeG(9, 24, 30, 2);
        //*/
        //viewVTControlVars(model);
    }
}
