package org.example.Plots;

import org.example.Elemets.MorrisLecar;
import org.example.Elemets.SynapsModel;
import org.example.Generator.StimGenerator;
import org.example.ModelInterfeses.VarType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JFreeChartSynapsModel {
    /**
     * Вывести график выхода с целевого нейрона, с графиком изменения ISyn
     * @param res
     * @param model
     */
    public static void viewVoltageNISynapse(Map<VarType, List<Double>> res, SynapsModel model) {
        //Создаем набор данных
        XYSeriesCollection dataset = new XYSeriesCollection();
        List<Double> times = res.get(VarType.TIME);
        //ISyn//////////////////////////////////////////////////////
        List<Double> ISyns = model.getISyns();
        XYSeries ISynsSeries = new XYSeries("ISyn Data");
        for (int j = 0; j < ISyns.size(); j++){
            ISynsSeries.add(times.get(j), ISyns.get(j));
        }
        dataset.addSeries(ISynsSeries);

        //Создаем диаграмму
        JFreeChart chartISyns = ChartFactory.createXYLineChart(
                "ISyn(t) мкА/см^2",
                "time",
                "ISyn",
                dataset
        );
        ISyns = ISyns.stream()
                .map(i -> {
                    if (Double.isInfinite(i) || Double.isNaN(i)) {
                        return 0.0;
                    } else {
                        return i;
                    }
                })
                .collect(Collectors.toList());

        //Получаем доступ к оси Y и включаем автомасштабирование
        NumberAxis rangeAxis = (NumberAxis) chartISyns.getXYPlot().getRangeAxis();
        double maxY = ISyns.get(0);
        double minY = ISyns.get(0);
        for(double I : ISyns){
            maxY = I > maxY ? I : maxY;
            minY = I < minY ? I : minY;
        }
        rangeAxis.setRange(minY - maxY * 0.2, maxY + maxY * 0.2);
        //Создаем панель диаграммы
        ChartPanel chartPanelISyns = new ChartPanel(chartISyns);
        //ISyn//////////////////////////////////////////////////////

        dataset = new XYSeriesCollection();
        //V(t)//////////////////////////////////////////////////////
        List<Double> Vs = res.get(VarType.V);
        Vs = Vs.stream()
                .map(v -> {
                    if (Double.isInfinite(v) || Double.isNaN(v)) {
                        return 0.0;
                    } else {
                        return v;
                    }
                })
                .collect(Collectors.toList());

        XYSeries VSeries = new XYSeries("V Data");
        for (int j = 0; j < times.size(); j++){
            VSeries.add(times.get(j), Vs.get(j));
        }
        dataset.addSeries(VSeries);

        //Создаем диаграмму
        JFreeChart chartV = ChartFactory.createXYLineChart(
                "V(t) мкА/см^2",
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

        mainPanel.add(chartPanelISyns);
        mainPanel.add(chartPanelV);

        JFrame frame = new JFrame("Model Plot Synaps");
        frame.setContentPane(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Вывести график управляющих каналами переменных
     */
    public static void viewInputs(List<Double[]> inputsNeuronsWaves, double dt) {
        Map<Integer, Double> mins = new HashMap<>();
        Map<Integer, Double> maxs = new HashMap<>();
        for (int i = 0; i < inputsNeuronsWaves.size(); i++) {
            double max = Arrays.stream(inputsNeuronsWaves.get(i))
                    .mapToDouble(Double::doubleValue)
                    .max().getAsDouble();
            double min = Arrays.stream(inputsNeuronsWaves.get(i))
                    .mapToDouble(Double::doubleValue)
                    .min().getAsDouble();


            mins.put(i, min);
            maxs.put(i, max);
        }

        Map<Integer, XYSeriesCollection> datasets = new HashMap<>();
        for (int i = 0; i < inputsNeuronsWaves.size(); i++) {
            datasets.put(i, new XYSeriesCollection());
            XYSeries series = new XYSeries(i + " Data");
            Double[] values = inputsNeuronsWaves.get(i);
            for (int j = 0; j < values.length; j++) {
                series.add(j*dt, values[j]);
            }
            datasets.get(i).addSeries(series);
        }

        Map<Integer, ChartPanel> chartsPanels = new HashMap<>();
        for (int i = 0; i < inputsNeuronsWaves.size(); i++) {
            datasets.get(i);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "V" + i + "(t)",
                    "time", "V" + i, datasets.get(i)
            );
            NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
            rangeAxis.setRange(
                    mins.get(i) - 5,
                    maxs.get(i) + 5
            );

            chartsPanels.put(i, new ChartPanel(chart));
        }

        JPanel mainPanel = new JPanel(new GridLayout(inputsNeuronsWaves.size(), 1));
        for (int i = 0; i < inputsNeuronsWaves.size(); i++) {
            mainPanel.add(chartsPanels.get(i));
        }
        JFrame frame = new JFrame("Model Plot Inputs");
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
            NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
            rangeAxis.setRange(
                    mins.get(t) - 0.05 * mins.get(t),
                    maxs.get(t) + 0.05 * maxs.get(t)
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
     * Стимуляция с изменением G целевого нейрона и Cm_syn, tau_syn
     * @param phasesInputNeurons
     * @param GAHP
     * @param GNa
     * @param GK
     * @param GM
     */
    public static void goWithChangeTargetNeuronNSynParams(
            double[] phasesInputNeurons,
            double GAHP, double GNa, double GK, double GM
    ){
        double totalTime = 100;
        double dt = 0.1;
        int len = (int)(totalTime/dt);
        //Input Neuron////////////////////////////////////////////
        List<Double[]> inputsNeuronsWaves = new ArrayList<>();
        for (int i = 0; i < phasesInputNeurons.length; i++) {
            inputsNeuronsWaves.add(StimGenerator.generateSimplySpike(len, phasesInputNeurons[i]));
        }
        viewInputs(inputsNeuronsWaves, dt);
        //Input Neuron////////////////////////////////////////////

        //Target Neuron///////////////////////////////////////////
        SynapsModel synapsModel = new SynapsModel();

        MorrisLecar modelTarget = new MorrisLecar();
        modelTarget.setTotalTime(totalTime);
        modelTarget.setDt(dt);
        modelTarget.setGNa(GNa/phasesInputNeurons.length);
        modelTarget.setGK(GK/phasesInputNeurons.length);
        modelTarget.setGshunt(modelTarget.getGshunt()/phasesInputNeurons.length);

        modelTarget.setGAHP(GAHP);
        modelTarget.setGM(GM);

        synapsModel.correctOutputNeuron(modelTarget);
        //Target Neuron///////////////////////////////////////////
        Map<VarType, List<Double>> res = synapsModel.start(inputsNeuronsWaves);
        System.out.println(res.get(VarType.V));

        viewVoltageNISynapse(res, synapsModel);
        viewVTControlVars(res, synapsModel.getOutputNeuron());
    }

    public static void main(String[] args) {
        //*
        //Спайки идут синхронно
        //Здоровый спайк
        double[] phasesInputNeurons = new double[]{10, 10, 10, 10};
        //G мСм/см^2
        goWithChangeTargetNeuronNSynParams(phasesInputNeurons,
               1, 24, 30, 200);
        //*/

        /*
        //Спайки идут друг за другом
        //Временное окно 2.1 с, шаг 0.7 c.
        double[] phasesInputNeurons = new double[]{10, 10.7, 11.4, 12.1};
        //G мСм/см^2
        goWithChangeTargetNeuronNSynParams(phasesInputNeurons,
               1, 24, 30, 1);
        //*/

        /*
        //Спайки идут хаотично 1
        //Временное окно 2.1 с, шаг 0.7 c.
        double[] phasesInputNeurons = new double[]{10, 11, 11.5, 12.1};
        //G мСм/см^2
        goWithChangeTargetNeuronNSynParams(phasesInputNeurons,
                1, 24, 30, 1);
        //*/
    }
}
