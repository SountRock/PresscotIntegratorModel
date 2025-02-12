package org.example.GateFuncs;

import org.example.ModelInterfeses.IonGateFunc;
import org.example.ModelInterfeses.VarType;

public class ZM implements IonGateFunc {
    double tauzM;
    double betazM;
    double gammazM;
    double lastZm;

    public ZM(double tauzM, double betazM, double gammazM) {
        this.tauzM = tauzM;
        this.betazM = betazM;
        this.gammazM = gammazM;
    }

    @Override
    public VarType getType() {
        return VarType.ZM;
    }

    @Override
    public double gateInf(double Vm) {
        //Функция активации M-тока (в установившемся состоянии)
        return 1 / (1 + Math.exp((betazM - Vm) / gammazM));
    }

    @Override
    public double gateTau(double Vm) {
        return tauzM;
    }

    @Override
    public double dgate_dt(double Vm) {
        return (gateInf(Vm) - lastZm) / tauzM;
    }

    @Override
    public void setLatGateVal(double gateVal) {
        lastZm = gateVal;
    }

    @Override
    public double getLatGateVal() {
        return lastZm;
    }

    @Override
    public double dgate_dtUsingTemp(double V_temp) {
        return (gateInf(V_temp) - lastZm) / tauzM;
    }
}
