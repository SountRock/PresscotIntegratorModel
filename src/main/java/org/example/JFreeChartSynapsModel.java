package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JFreeChartSynapsModel {
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
        //System.out.println(Arrays.toString(wave));

        return wave;
    }

    /**
     * Стимуляция с изменением G целевого нейрона и Cm_syn, tau_syn
     * @param phasesInputNeurons
     * @param Cm_syn (pF)
     * @param tau_syn pF/мСм
     * @param GAHP
     * @param GNa
     * @param GK
     * @param GM
     */
    public static void goWithChangeTargetNeuronNSynParams(
            double[] phasesInputNeurons,
            double Cm_syn, double tau_syn,
            double GAHP, double GNa, double GK, double GM
    ){
        double totalTime = 100;
        double dt = 0.1;
        int len = (int)(totalTime/dt);
        //Input Neuron////////////////////////////////////////////
        List<Double[]> inputsNeuronsWaves = new ArrayList<>();
        for (int i = 0; i < phasesInputNeurons.length; i++) {
            inputsNeuronsWaves.add(generateSimplySpike(len, phasesInputNeurons[i]));
        }
        //Input Neuron////////////////////////////////////////////

        //Target Neuron///////////////////////////////////////////
        SynapsModel synapsModel = new SynapsModel(Cm_syn, tau_syn);

        MorrisLecar modelTarget = new MorrisLecar();
        modelTarget.setTotalTime(100);
        modelTarget.setDt(0.1);
        modelTarget.setGAHP(GAHP);
        modelTarget.setGNa(GNa);
        modelTarget.setGK(GK);
        modelTarget.setGM(GM);
        synapsModel.correctTargetNeuron(modelTarget);
        //Target Neuron///////////////////////////////////////////
        Map<OutputType, List<Double>> res = synapsModel.start(inputsNeuronsWaves);
        System.out.println(res.get(OutputType.V));
    }

    public static void main(String[] args) {
        double[] phasesInputNeurons = new double[]{10, 10, 10, 10};
        goWithChangeTargetNeuronNSynParams(phasesInputNeurons,
                2, 10,
                1, 24, 30, 2);
    }
}
