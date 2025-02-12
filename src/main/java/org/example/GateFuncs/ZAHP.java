package org.example.GateFuncs;

import org.example.ModelInterfeses.IonGateFunc;
import org.example.ModelInterfeses.VarType;

public class ZAHP implements IonGateFunc {
    double tauzAHP;
    double betazAHP;
    double gammazAHP;
    double lastZAHP;

    public ZAHP(double tauzAHP, double betazAHP, double gammazAHP) {
        this.tauzAHP = tauzAHP;
        this.betazAHP = betazAHP;
        this.gammazAHP = gammazAHP;
    }

    @Override
    public VarType getType() {
        return VarType.ZAHP;
    }

    @Override
    public double gateInf(double Vm) {
        return 1 / (1 + Math.exp((betazAHP - Vm) / gammazAHP));
    }

    @Override
    public double gateTau(double Vm) {
        return tauzAHP;
    }

    @Override
    public double dgate_dt(double Vm) {
        return (gateInf(Vm) - lastZAHP) / tauzAHP;
    }

    @Override
    public void setLatGateVal(double gateVal) {
        lastZAHP = gateVal;
    }

    @Override
    public double getLatGateVal() {
        return lastZAHP;
    }

    @Override
    public double dgate_dtUsingTemp(double V_temp) {
        return (gateInf(V_temp) - lastZAHP) / tauzAHP;
    }
}
