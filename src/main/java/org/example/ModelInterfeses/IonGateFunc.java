package org.example.ModelInterfeses;

/**
 * Управляющая переменная хранит историю всех изменений.
 */
public interface IonGateFunc {
    /**
     * Получить имя переменной
     * @return
     */
    VarType getType();

    double gateInf(double Vm);

    double gateTau(double Vm);

    double dgate_dt(double Vm);

    void setLatGateVal(double gateVal);

    double getLatGateVal();

    double dgate_dtUsingTemp(double V_temp);
}
