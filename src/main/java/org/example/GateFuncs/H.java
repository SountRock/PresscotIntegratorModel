package org.example.GateFuncs;

import org.example.ModelInterfeses.IonGateFunc;
import org.example.ModelInterfeses.VarType;

public class H implements IonGateFunc {
    double alpha_h;
    double beta_h;
    double gamma_h;
    double lastH;
    double tau_h;

    public H(double alpha_h, double beta_h, double gamma_h, double tau_h) {
        this.alpha_h = alpha_h;
        this.beta_h = beta_h;
        this.gamma_h = gamma_h;
        this.tau_h = tau_h;
    }

    @Override
    public VarType getType() {
        return VarType.H;
    }

    @Override
    public double gateInf(double Vm) {
        return 1-alpha_h/(1+Math.exp((beta_h-Vm)/gamma_h));
    }

    @Override
    public double gateTau(double Vm) {
        return tau_h;
    }

    @Override
    public double dgate_dt(double Vm) {
        return (gateInf(Vm)-lastH)/tau_h;
    }

    @Override
    public void setLatGateVal(double gateVal) {
        lastH = gateVal;
    }

    @Override
    public double getLatGateVal() {
        return lastH;
    }

    @Override
    public double dgate_dtUsingTemp(double V_temp) {
        return (gateInf(V_temp)-lastH)/tau_h;
    }
}
