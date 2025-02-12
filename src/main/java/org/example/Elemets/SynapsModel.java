package org.example.Elemets;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.ModelInterfeses.Neuron;
import org.example.ModelInterfeses.VarType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Is = sum{(aE * Cm_syn)/ tau_syn}, где aE = Vinput_neuron - Vcurrent_in_target_neuron, принимаем что параметры Cm_syn и tau_syn могут быть отличны от параметров целевого нейрона.
 * !!! Реализовать динамический рассчет tau_syn как tau_syn = tau_target_neuron = f(RNa, RK, RM, RAHP) в MorrisLecar.
 * tau_syn = tau_target_neuron = gNa + gK + gM + gAHP, где g = f(n), n - управляющая функция каала.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SynapsModel {
    List<Neuron> inputNeurons;
    MorrisLecar outputNeuron;
    List<Double> ISyns;

    public SynapsModel() {
        ISyns = new ArrayList<>();
    }

    public void correctOutputNeuron(MorrisLecar outputNeuron){
        this.outputNeuron = outputNeuron;
    }

    /**
     * Запустить модель
     * @param inputsNeuronsWaves
     * @return
     */
    public Map<VarType, List<Double>> start(List<Double[]> inputsNeuronsWaves){
        Map<VarType, List<Double>> res = new HashMap<>();
        res.put(VarType.TIME, new ArrayList<>());
        res.put(VarType.V, new ArrayList<>());
        res.put(VarType.W, new ArrayList<>());
        res.put(VarType.H, new ArrayList<>());
        res.put(VarType.ZM, new ArrayList<>());
        res.put(VarType.ZAHP, new ArrayList<>());
        res.put(VarType.TAU, new ArrayList<>());

        final int serialLen = inputsNeuronsWaves.size();
        int len = (int) (outputNeuron.getTotalTime() / outputNeuron.getDt());
        for (int i = 0; i < len; i++) {
            double[] VCurrentInput = new double[serialLen];
            for (int j = 0; j < serialLen; j++) {
                VCurrentInput[j] = inputsNeuronsWaves.get(j)[i];
            }
            double tau = outputNeuron.tauGlobal();
            double k = outputNeuron.getCm() / tau;
            double ISyn = 0;
            for (int l = 0; l < VCurrentInput.length; l++) {
                double aE = VCurrentInput[l] - outputNeuron.getVm();
                ISyn += k * aE;
            }
            ISyns.add(ISyn);
            outputNeuron.setIStim(ISyn);
            outputNeuron.step();
            res.get(VarType.TIME).add(i*outputNeuron.getDt());
            res.get(VarType.V).add(outputNeuron.getVm());
            res.get(VarType.TAU).add(tau);
            res.get(VarType.W).add(outputNeuron.getWfunc().getLatGateVal());
            res.get(VarType.H).add(outputNeuron.getHfunc().getLatGateVal());
            res.get(VarType.ZM).add(outputNeuron.getZMfunc().getLatGateVal());
            res.get(VarType.ZAHP).add(outputNeuron.getZAHPfunc().getLatGateVal());
        }

        return res;
    }

    public MorrisLecar getOutputNeuron() {
        return outputNeuron;
    }

    public List<Double> getISyns() {
        return ISyns;
    }

}

