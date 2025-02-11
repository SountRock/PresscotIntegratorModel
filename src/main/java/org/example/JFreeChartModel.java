package org.example;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class JFreeChartModel {
    /**
     * Вывести обычный график Vm(t)
     * @param res
     * @param model
     */
    public static void viewVTChart(Map<OutputType, List<Double>> res, MorrisLecar model) {
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
    public static void viewVTChartWithStim(Map<OutputType, List<Double>> res, MorrisLecar model, Double[] stimWave) {
        //Создаем набор данных
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<Double> times = res.get(OutputType.TIME);
        //Stim//////////////////////////////////////////////////////
        XYSeries StimSeries = new XYSeries("Stim Data");
        for (int j = 0; j < stimWave.length; j++){
            StimSeries.add(times.get(j), stimWave[j]);
        }
        dataset.addSeries(StimSeries);

        //Создаем диаграмму
        JFreeChart chartStim = ChartFactory.createXYLineChart(
                "IStim(t) (мСм/см^2)",
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
        List<Double> Vs = res.get(OutputType.V);
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
    public static void viewVTControlVars(Map<OutputType, List<Double>> res, MorrisLecar model) {
        List<Double> times = res.get(OutputType.TIME);

        Map<OutputType, Double> mins = new HashMap<>();
        Map<OutputType, Double> maxs = new HashMap<>();
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.ZM, OutputType.ZAHP, OutputType.H}){
            mins.put(t, res.get(t).get(0));
            maxs.put(t, res.get(t).get(0));
        }

        //Создаем набор данных
        Map<OutputType, XYSeriesCollection> datasets = new HashMap<>();
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.ZM, OutputType.ZAHP, OutputType.H}){
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
        for (OutputType t : new OutputType[]{OutputType.W, OutputType.ZM, OutputType.ZAHP, OutputType.H}){
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
        mainPanel.add(chartsPanels.get(OutputType.ZM));
        mainPanel.add(chartsPanels.get(OutputType.ZAHP));
        mainPanel.add(chartsPanels.get(OutputType.H));
        JFrame frame = new JFrame("Model Plot Controls");
        frame.setContentPane(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Сгенерировать синусоиду
     * @param len
     * @param amplitude
     * @param frequency
     * @param phase
     * @return
     */
    public static Double[] generateSinusoid(int len, double amplitude, double frequency, double phase) {
        Double[] sinusoid = new Double[len];
        for (int i = 0; i < len; i++) {
            double angle = 2 * Math.PI * frequency * i / len + phase;
            sinusoid[i] = amplitude * Math.sin(angle);
        }
        return sinusoid;
    }

    /**
     * Генерация одиночного простого спайка
     * @param len
     * @param phase
     * @return
     */
    public static Double[] generateSimplySpike(int len, double phase) {
        MorrisLecar modelStim = new MorrisLecar();
        modelStim.setTotalTime(100);
        modelStim.setDt(0.1);
        modelStim.setI_DC(34);
        Map<OutputType, List<Double>> res = modelStim.start();

        List<Double> values = res.get(OutputType.V);
        List<Double> times = res.get(OutputType.TIME);

        //Получение семпла одиночного спайка//////////////////////////////////
        double startTime = 32;
        double endTime = 83.1;
        int startIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == startTime ) {
                startIndex = i;
                break;
            }
        }
        int endIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == endTime ) {
                endIndex = i;
                break;
            }
        }
        //System.out.println(times.get(startIndex));
        //System.out.println(times.get(endIndex));
        Double[] spikeSample = values.subList(startIndex, endIndex).toArray(new Double[0]);
        //Получение семпла одиночного спайка//////////////////////////////////
        
        //Рассчет индекса фазы начала спайка//////////////////////////////////
        int phaseIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == phase ) {
                phaseIndex = i;
                break;
            }
        }
        //Рассчет индекса фазы начала спайка//////////////////////////////////

        Double[] wave = new Double[len];
        Arrays.fill(wave, spikeSample[spikeSample.length - 1]);
        System.arraycopy(spikeSample, 0, wave, phaseIndex, spikeSample.length);
        System.out.println(Arrays.toString(wave));

        return wave;
    }

    /**
     * Стимуляция постоянным током с изменением G
     * @param GAHP
     */
    public static void goDCWithChangeG(double GAHP, double GNa, double GK, double GM){
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(100);
        model.setDt(0.1);
        model.setI_DC(35);

        model.setGAHP(GAHP);
        model.setGNa(GNa);
        model.setGK(GK);
        model.setGM(GM);
        Map<OutputType, List<Double>> res = model.start();
        viewVTChart(res, model);
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
        Double[] wave = generateSinusoid(len, 40, 1.5, 0.5);
        Map<OutputType, List<Double>> res = model.startWithStimWave(wave);
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
        Double[] spikeWave = generateSimplySpike(len, 30.0);
        for (int i = 0; i < spikeWave.length; i++) {
            spikeWave[i] = spikeWave[i] * Gsyn;
        }
        //Input Neuron////////////////////////////////////////////

        //Target Neuron///////////////////////////////////////////
        MorrisLecar modelTarget = new MorrisLecar();
        modelTarget.setTotalTime(100);
        modelTarget.setDt(0.1);

        //modelTarget.setGAHP(GAHP);
        //modelTarget.setGNa(GNa);
        //modelTarget.setGK(GK);
        //modelTarget.setGM(GM);
        Map<OutputType, List<Double>> res = modelTarget.startWithStimWave(spikeWave);
        //Target Neuron///////////////////////////////////////////

        viewVTChartWithStim(res, modelTarget, spikeWave);
    }

    public static void main(String[] args) {
        goSpikeWaveWithChangeG(1,0.5, 24, 30, 2);
        //goSinWaveWithChangeG(1, 24, 30, 2);
        //*
        //goDCWithChangeG(1, 24, 30, 2);
        //goDCWithChangeG(3, 24, 30, 2);
        //goDCWithChangeG(5, 24, 30, 2);
        //goDCWithChangeG(9, 24, 30, 2);
        //*/
        //viewVTControlVars(model);
    }
}
