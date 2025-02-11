package org.example;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Is = sum{(aE * Cm_syn)/ tau_syn}, где aE = Vinput_neuron - Vcurrent_in_target_neuron, принимаем что параметры Cm_syn и tau_syn могут быть отличны от параметров целевого нейрона.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SynapsModel {
    double Cm_syn;
    double tau_syn;
    double[] aEs;

    MorrisLecar targetNeuron;
    Map<OutputType, List<Double>> res;
    List<Double> ISyns;

    public SynapsModel(double Cm_syn, double tau_syn) {
        this.Cm_syn = Cm_syn;
        this.tau_syn = tau_syn;
        res = new HashMap<>();
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.V, new ArrayList<>());
        res.put(OutputType.W, new ArrayList<>());
        res.put(OutputType.H, new ArrayList<>());
        res.put(OutputType.ZM, new ArrayList<>());
        res.put(OutputType.ZAHP, new ArrayList<>());
        ISyns = new ArrayList<>();
    }

    public void correctTargetNeuron(MorrisLecar targetNeuron){
        this.targetNeuron = targetNeuron;
    }


    public Map<OutputType, List<Double>> start(List<Double[]> inputsNeuronsWaves){
        Map<OutputType, List<Double>> res = new HashMap<>();
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.V, new ArrayList<>());
        res.put(OutputType.W, new ArrayList<>());
        res.put(OutputType.H, new ArrayList<>());
        res.put(OutputType.ZM, new ArrayList<>());
        res.put(OutputType.ZAHP, new ArrayList<>());

        final int serialLen = inputsNeuronsWaves.size();
        int len = (int) (targetNeuron.getTotalTime()/targetNeuron.getDt());
        for (int i = 0; i < len; i++) {
            double[] VCurrentInput = new double[serialLen];
            for (int j = 0; j < serialLen; j++) {
                VCurrentInput[j] = inputsNeuronsWaves.get(j)[i];
            }
            double k = Cm_syn / tau_syn;
            double ISyn = 0;
            for (int l = 0; l < VCurrentInput.length; l++) {
                double aEs = VCurrentInput[l] - targetNeuron.getV();
                ISyn += k * aEs;
            }
            ISyns.add(ISyn);
            targetNeuron.setI_DC(ISyn);
            targetNeuron.rk4Step();
            res.get(OutputType.TIME).add(i*targetNeuron.getDt());
            res.get(OutputType.V).add(targetNeuron.getV());
            res.get(OutputType.W).add(targetNeuron.getW());
            res.get(OutputType.H).add(targetNeuron.getH());
            res.get(OutputType.ZM).add(targetNeuron.getZM());
            res.get(OutputType.ZAHP).add(targetNeuron.getZAHP());
        }

        return res;
    }

    public Map<OutputType, List<Double>> getRes() {
        return res;
    }

    public List<Double> getISyns() {
        return ISyns;
    }

    public double[] getLastAEs() {
        return aEs;
    }
}

