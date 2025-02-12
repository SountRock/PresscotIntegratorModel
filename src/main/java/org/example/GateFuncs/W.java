package org.example.GateFuncs;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.ModelInterfeses.IonGateFunc;
import org.example.ModelInterfeses.VarType;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class W implements IonGateFunc {
    double beta_w;
    double gamma_w;
    double phi_w;

    double lastW;

    public W(double beta_w, double gamma_w, double phi_w) {
        this.beta_w = beta_w;
        this.gamma_w = gamma_w;
        this.phi_w = phi_w;
    }

    @Override
    public VarType getType() {
        return VarType.W;
    }

    @Override
    public double gateInf(double Vm) {
        return 0.5 * (1 + Math.tanh((Vm - beta_w) / gamma_w));
    }

    @Override
    public double gateTau(double Vm) {
        return 1 / Math.cosh((Vm - beta_w) / (2 * gamma_w));
    }

    @Override
    public double dgate_dt(double Vm) {
        return phi_w * (gateInf(Vm) - lastW) / gateTau(Vm);
    }

    @Override
    public void setLatGateVal(double gateVal) {
        lastW = gateVal;
    }

    @Override
    public double getLatGateVal() {
        return lastW;
    }

    @Override
    public double dgate_dtUsingTemp(double V_temp) {
        return phi_w * (gateInf(V_temp) - lastW) / gateTau(V_temp);
    }
}
