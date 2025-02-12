package org.example.Elemets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.GateFuncs.H;
import org.example.GateFuncs.W;
import org.example.GateFuncs.ZAHP;
import org.example.GateFuncs.ZM;
import org.example.ModelInterfeses.IonGateFunc;
import org.example.ModelInterfeses.Neuron;
import org.example.ModelInterfeses.VarType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * dV/dt = (IStim+I_noise-GNa*minf(V)*h*(V-VNa)-GK*w*(V-VK)-Gshunt* (V-Vshunt)-GM*zM*(V-VK)-GAHP*zAHP*(V-VK))/Cm
 * !!! Почему minf(V) не меняется через Метод Рунге-Кутты 4-го порядка, а напрямую через minf
 * Шум задается рандомом.
 */
public class MorrisLecar implements Neuron {
    // ========================================================================
    // Параметры модели
    // ========================================================================
    double Cm = 2;          //Емкость мембраны (pF)
    double IStim = 0;        //Постоянный ток смещения (DC bias) (мкА/см^2)
    double sigma = 0;       //Амплитуда шума (мкА/см^2)
    double tau_inoise = 5;  //Временная константа шума (мс)
    double I_avg = 0;       //Среднее значение шума (мкА/см^2)
    double beta_m = -1.2;   //Параметр активации натрия (mV)
    double gamma_m = 18;    //Параметр активации натрия (mV)
    double GNa = 24;        //Максимальная проводимость натрия (мСм/см^2)
    double VNa = 50;        //Потенциал обращения натрия (mV)
    double beta_w = -9;     //Параметр активации калия (mV)
    double gamma_w = 8;     //Параметр активации калия (mV)
    double GK = 30;         //Максимальная проводимость калия (мСм/см^2)
    double VK = -100;       //Потенциал обращения калия (mV)
    double phi_w = 0.25;    //Температурный коэффициент для калия
    double Gshunt = 2;      //Проводимость шунтирования (мСм/см^2)
    double Vshunt = -70;    //Потенциал шунтирования (mV)
    double tauzM = 400;     //Временная константа M-тока (мс)
    double betazM = -29;    //Параметр активации M-тока (mV)
    double gammazM = 2;     //Параметр активации M-тока (mV)
    double GM = 2;          //Максимальная проводимость M-тока (мСм/см^2)
    double tauzAHP = 200;   //Временная константа AHP-тока (мс)
    double betazAHP = 0;    //Параметр активации AHP-тока (mV)
    double gammazAHP = 5;   //Параметр активации AHP-тока (mV)
    double GAHP = 1;        //Максимальная проводимость AHP-тока (мСм/см^2)

    double tau_h=1000;      //Временная константа инактивации натрия (мс)
    double alpha_h=0.67;    //Параметр инактивации натрия
    double beta_h=-40;      //Параметр инактивации натрия (mV)
    double gamma_h=8;       //Параметр инактивации натрия (mV)

    // ========================================================================
    // Функции ионных каналов
    // ========================================================================
    IonGateFunc wfunc = new W(beta_w, gamma_w, phi_w);
    IonGateFunc hfunc = new H(alpha_h, beta_h, gamma_h, tau_h);
    IonGateFunc zMfunc = new ZM(tauzM, betazM, gammazM);
    IonGateFunc zAHPfunc = new ZAHP(tauzAHP, betazAHP, gammazAHP);

    // ========================================================================
    // Переменные состояния
    // ========================================================================
    double Vrest = -70;
    double Vm = Vrest;        //Мембранный потенциал (mV)
    //double w = 0.000025;   //Переменная восстановления калия (безразмерная)
    //double zAHP = 0;       //Переменная активации AHP-тока (безразмерная)
    //double zM = 0;         //Переменная активации M-тока (безразмерная)
    double I_noise = 0;    //Ток шума (мкА/см^2)
    //double h = 1;          //Переменная инактивации натрия (безразмерная)

    public MorrisLecar() {
        wfunc.setLatGateVal(0.000025);
        hfunc.setLatGateVal(1);
        zMfunc.setLatGateVal(0);
        zAHPfunc.setLatGateVal(0);
    }

    // ========================================================================
    // Параметры симуляции
    // ========================================================================
    double totalTime = 1000; //Общая продолжительность симуляции (мс)
    double dt = 0.1;         //Шаг интегрирования (мс)

    // ========================================================================
    // tau нейрона глобально
    // ========================================================================

    /**
     * Рассчет tau глобального (берется текушее V)
     * tau = Сm/(gNa + gK + Gshunt + gM + gAHP), где g = f(n), n - управляющая функция каала.
     * dV/dt = (IStim+I_noise-GNa*minf(V)*h*(V-VNa)-GK*w*(V-VK)-Gshunt* (V-Vshunt)-GM*zM*(V-VK)-GAHP*zAHP*(V-VK))/Cm
     * @return
     */
    public double tauGlobal(){
        return Cm / ((GNa * minf(Vm)*hfunc.getLatGateVal()) + (GK * wfunc.getLatGateVal()) + Gshunt + (GM * zMfunc.getLatGateVal()) + (GAHP*zAHPfunc.getLatGateVal()));
    }

    /**
     * Рассчет tau глобального (V передается как arg)
     * tau = Сm/(gNa + gK + Gshunt + gM + gAHP), где g = f(n), n - управляющая функция каала.
     * dV/dt = (IStim+I_noise-GNa*minf(V)*h*(V-VNa)-GK*w*(V-VK)-Gshunt* (V-Vshunt)-GM*zM*(V-VK)-GAHP*zAHP*(V-VK))
     * @param V
     * @return
     */
    double tauGlobal(double V){
        return Cm / ((GNa * minf(V)*hfunc.getLatGateVal()) + (GK * wfunc.getLatGateVal()) + Gshunt + (GM * zMfunc.getLatGateVal()) + (GAHP*zAHPfunc.getLatGateVal()));
    }

    // ========================================================================
    // Вспомогательные функции (зависящие от напряжения)
    // ========================================================================
    public double minf(double V) {
        //Функция активации натрия (мгновенная)
        return 0.5 * (1 + Math.tanh((V - beta_m) / gamma_m));
    }

    // ========================================================================
    // Производные (описывают изменение переменных состояния во времени)
    // ========================================================================
    public double dVdt() {
        //Уравнение для изменения мембранного потенциала
        return (IStim + I_noise - GNa * hfunc.getLatGateVal() * minf(Vm) * (Vm - VNa) - GK * wfunc.getLatGateVal() * (Vm - VK) - Gshunt * (Vm - Vshunt) - GM * zMfunc.getLatGateVal() * (Vm - VK) - GAHP * zAHPfunc.getLatGateVal() * (Vm - VK)) / Cm;
    }

    public double di_noisedt() {
        //Уравнение для изменения тока шума (процесс Орнштейна-Уленбека)
        // Simple Euler for Ornstein-Uhlenbeck.  More accurate methods exist.
        double nz = Math.random() * 2 - 1;  // Replace with a better random number generator if needed.
        return -1/tau_inoise*(I_noise - I_avg) + sigma*nz;
    }

    public Map<VarType, List<Double>> start(){
        Map<VarType, List<Double>> res = new HashMap<>();
        res.put(VarType.TIME, new ArrayList<>());
        res.put(VarType.V, new ArrayList<>());
        res.put(VarType.W, new ArrayList<>());
        res.put(VarType.H, new ArrayList<>());
        res.put(VarType.ZM, new ArrayList<>());
        res.put(VarType.ZAHP, new ArrayList<>());

        for (double t = 0; t < totalTime; t += dt) {
            step();
            res.get(VarType.TIME).add(t);
            res.get(VarType.V).add(Vm);
            res.get(VarType.W).add(wfunc.getLatGateVal());
            res.get(VarType.H).add(hfunc.getLatGateVal());
            res.get(VarType.ZM).add(zMfunc.getLatGateVal());
            res.get(VarType.ZAHP).add(zAHPfunc.getLatGateVal());
        }

        return res;
    }

    public Map<VarType, List<Double>> startWithStimWave(Double[] stimWave){
        if (stimWave.length > totalTime/dt){
            throw new RuntimeException("stimWave not correct length");
        }

        Map<VarType, List<Double>> res = new HashMap<>();
        res.put(VarType.TIME, new ArrayList<>());
        res.put(VarType.TIME, new ArrayList<>());
        res.put(VarType.V, new ArrayList<>());
        res.put(VarType.W, new ArrayList<>());
        res.put(VarType.H, new ArrayList<>());
        res.put(VarType.ZM, new ArrayList<>());
        res.put(VarType.ZAHP, new ArrayList<>());

        for (int i = 0; i<stimWave.length; i++) {
            setIStim(stimWave[i]);
            step();
            res.get(VarType.TIME).add(i*dt);
            res.get(VarType.V).add(Vm);
            res.get(VarType.W).add(wfunc.getLatGateVal());
            res.get(VarType.H).add(hfunc.getLatGateVal());
            res.get(VarType.ZM).add(zMfunc.getLatGateVal());
            res.get(VarType.ZAHP).add(zAHPfunc.getLatGateVal());
        }

        return res;
    }

    // ========================================================================
    // Метод Рунге-Кутты 4-го порядка (RK4) для численного интегрирования (своего рода рассчет фрейма размером в 4 точки от t внутри dt, т.е. в диапазоне [t0, t0 + dt)
    /*
    Сводится к вычислению наклонов интервалов:
        Основное выражение: dy/dt = f(t, y)

        Вычисление k1 (наклон в начале интервала):
        k1 = f(t0, y0)

        Вычисление k2 (наклон в середине интервала, используя k1):
        k2 = f(t0 + dt/2, y0 + dt*k1/2)

        Вычисление k3 (наклон в середине интервала, используя k2):
        k3 = f(t0 + dt/2, y0 + dt*k2/2)

        Вычисление k4 (наклон в конце интервала, используя k3):
        k4 = f(t0 + dt, y0 + dt*k3)

        Обновление значения y:
        y1 = y0 + dt * (k1 + 2*k2 + 2*k3 + k4) / 6 Это – взвешенное среднее значение четырех оценок наклона, используемое для обновления значения y. Обратите внимание на веса: 1/6, 1/3, 1/3, 1/6.

        Для модели:
        Основные выражения:
            dV/dt = f1(V, w, zAHP, zM, i_noise, h)
            dw/dt = f2(V, w)
            dzAHP/dt = f3(V, zAHP)
            dzM/dt = f4(V, zM)
            di_noise/dt = f5(i_noise)
            dh/dt = f6(V, h)
        Для каждого вычисляються k1-k4
     */
    // ========================================================================
    public void step() {
        //Вычисление k1 (производные в текущей точке)
        double k1_V = dVdt();
        double k1_w = wfunc.dgate_dt(Vm);
        double k1_zAHP = zAHPfunc.dgate_dt(Vm);
        double k1_zM = zMfunc.dgate_dt(Vm);
        double k1_h = hfunc.dgate_dt(Vm);
        double k1_inoise = di_noisedt();

        //Вычисление k2 (производные в середине интервала, используя k1)
        double V_temp = Vm + dt * k1_V / 2;
        double w_temp = wfunc.getLatGateVal() + dt * k1_w / 2;
        double zAHP_temp = zAHPfunc.getLatGateVal() + dt * k1_zAHP / 2;
        double zM_temp = zMfunc.getLatGateVal() + dt * k1_zM / 2;
        double h_temp = hfunc.getLatGateVal() + dt * k1_h /2;
        double ino_temp = I_noise + dt * k1_inoise / 2;

        double k2_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k2_w = wfunc.dgate_dtUsingTemp(V_temp);
        double k2_zAHP = zAHPfunc.dgate_dtUsingTemp(V_temp);
        double k2_zM = zMfunc.dgate_dtUsingTemp(V_temp);
        double k2_h = hfunc.dgate_dtUsingTemp(V_temp);
        double k2_inoise = di_noisedtUsingTemp(ino_temp);

        // Вычисление k3 (производные в середине интервала, используя k2)
        V_temp = Vm + dt * k2_V / 2;
        w_temp = wfunc.getLatGateVal() + dt * k2_w / 2;
        zAHP_temp = zAHPfunc.getLatGateVal() + dt * k2_zAHP / 2;
        zM_temp = zMfunc.getLatGateVal() + dt * k2_zM / 2;
        ino_temp = I_noise + dt * k2_inoise / 2;
        h_temp = hfunc.getLatGateVal() + dt * k2_h / 2;

        double k3_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k3_w = wfunc.dgate_dtUsingTemp(V_temp);
        double k3_zAHP = zAHPfunc.dgate_dtUsingTemp(V_temp);
        double k3_zM = zMfunc.dgate_dtUsingTemp(V_temp);
        double k3_inoise = di_noisedtUsingTemp(ino_temp);
        double k3_h = hfunc.dgate_dtUsingTemp(V_temp);

        //Вычисление k4 (производные в конце интервала, используя k3)
        V_temp = Vm + dt * k3_V;
        w_temp = wfunc.getLatGateVal() + dt * k3_w;
        zAHP_temp = zAHPfunc.getLatGateVal() + dt * k3_zAHP;
        zM_temp = zMfunc.getLatGateVal() + dt * k3_zM;
        h_temp = hfunc.getLatGateVal() + dt * k3_h;
        ino_temp = I_noise + dt * k3_inoise;

        double k4_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k4_w = wfunc.dgate_dtUsingTemp(V_temp);
        double k4_zAHP = zAHPfunc.dgate_dtUsingTemp(V_temp);
        double k4_zM = zMfunc.dgate_dtUsingTemp(V_temp);
        double k4_h = hfunc.dgate_dtUsingTemp(V_temp);
        double k4_inoise = di_noisedtUsingTemp(ino_temp);

        //Обновление переменных состояния с использованием взвешенной суммы k1, k2, k3, k4
        Vm += dt * (k1_V + 2 * k2_V + 2 * k3_V + k4_V) / 6;
        wfunc.setLatGateVal(wfunc.getLatGateVal() +  dt * (k1_w + 2 * k2_w + 2 * k3_w + k4_w) / 6);
        zAHPfunc.setLatGateVal(zAHPfunc.getLatGateVal() + dt * (k1_zAHP + 2 * k2_zAHP + 2 * k3_zAHP + k4_zAHP) / 6);
        zMfunc.setLatGateVal(zMfunc.getLatGateVal() + dt * (k1_zM + 2 * k2_zM + 2 * k3_zM + k4_zM) / 6);
        hfunc.setLatGateVal(hfunc.getLatGateVal() + dt * (k1_h + 2 * k2_h + 2 * k3_h + k4_h) / 6);
        I_noise += dt * (k1_inoise + 2 * k2_inoise + 2 * k3_inoise + k4_inoise) / 6;
    }

    // ========================================================================
    // Методы dVdt, dwdt, dzAHPdt, dzMdt с использованием временных значений для RK4
    // (необходимы для правильного вычисления производных на промежуточных шагах)
    // ========================================================================
    private double dVdtUsingTemp(double V_temp, double w_temp, double zAHP_temp, double zM_temp, double i_noise_temp, double h_temp) {
        return (IStim + i_noise_temp - GNa * h_temp * minf(V_temp) * (V_temp - VNa) - GK * w_temp * (V_temp - VK) - Gshunt * (V_temp - Vshunt) - GM * zM_temp * (V_temp - VK) - GAHP * zAHP_temp * (V_temp - VK)) / Cm;
    }

    private double di_noisedtUsingTemp(double i_noise_temp) {
        double nz = Math.random() * 2 - 1;
        return -1/tau_inoise*(i_noise_temp - I_avg) + sigma * nz;
    }
}