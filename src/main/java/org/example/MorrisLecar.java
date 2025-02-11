package org.example;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * dV/dt = (I_DC+I_noise-GNa*minf(V)*(V-VNa)-GK*w*(V-VK)-Gshunt* (V-Vshunt)-GM*zM*(V-VK)-GAHP*zAHP*(V-VK))/Cm
 * Шум задается рандомом.
 */
public class MorrisLecar {
    // ========================================================================
    // Параметры модели
    // ========================================================================
    double Cm = 2;          //Емкость мембраны (pF)
    double I_DC = 0;        //Постоянный ток смещения (DC bias) (мкА/см^2)
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
    // Переменные состояния
    // ========================================================================
    double Vrest = -70;
    double V = Vrest;        //Мембранный потенциал (mV)
    double w = 0.000025;   //Переменная восстановления калия (безразмерная)
    double zAHP = 0;       //Переменная активации AHP-тока (безразмерная)
    double zM = 0;         //Переменная активации M-тока (безразмерная)
    double I_noise = 0;    //Ток шума (мкА/см^2)
    double h = 1;          //Переменная инактивации натрия (безразмерная)

    // ========================================================================
    // Параметры симуляции
    // ========================================================================
    double totalTime = 1000; //Общая продолжительность симуляции (мс)
    double dt = 0.1;         //Шаг интегрирования (мс)

    // ========================================================================
    // Вспомогательные функции (зависящие от напряжения)
    // ========================================================================
    double minf(double V) {
        //Функция активации натрия (мгновенная)
        return 0.5 * (1 + Math.tanh((V - beta_m) / gamma_m));
    }

    double winf(double V) {
        //Функция активации калия (в установившемся состоянии)
        return 0.5 * (1 + Math.tanh((V - beta_w) / gamma_w));
    }

    double tauw(double V) {
        //Временная константа активации калия
        return 1 / Math.cosh((V - beta_w) / (2 * gamma_w));
    }

    double zinfM(double V) {
        //Функция активации M-тока (в установившемся состоянии)
        return 1 / (1 + Math.exp((betazM - V) / gammazM));
    }

    double zinfAHP(double V) {
        //Функция активации AHP-тока (в установившемся состоянии)
        return 1 / (1 + Math.exp((betazAHP - V) / gammazAHP));
    }

    double hinf(double v){
        return 1-alpha_h/(1+Math.exp((beta_h-v)/gamma_h));
    }

    // ========================================================================
    // Производные (описывают изменение переменных состояния во времени)
    // ========================================================================
    double dVdt() {
        //Уравнение для изменения мембранного потенциала
        return (I_DC + I_noise - GNa * h * minf(V) * (V - VNa) - GK * w * (V - VK) - Gshunt * (V - Vshunt) - GM * zM * (V - VK) - GAHP * zAHP * (V - VK)) / Cm;
    }

    double dwdt() {
        //Уравнение для изменения переменной восстановления калия
        return phi_w * (winf(V) - w) / tauw(V);
    }

    double dzAHPdt() {
        //Уравнение для изменения переменной активации AHP-тока
        return (zinfAHP(V) - zAHP) / tauzAHP;
    }

    double dzMdt() {
        //Уравнение для изменения переменной активации M-тока
        return (zinfM(V) - zM) / tauzM;
    }

    double di_noisedt() {
        //Уравнение для изменения тока шума (процесс Орнштейна-Уленбека)
        // Simple Euler for Ornstein-Uhlenbeck.  More accurate methods exist.
        double nz = Math.random() * 2 - 1;  // Replace with a better random number generator if needed.
        return -1/tau_inoise*(I_noise - I_avg) + sigma*nz;
    }

    double dhdt(){
        return (hinf(V)-h)/tau_h;
    }

    public Map<OutputType, List<Double>> start(){
        Map<OutputType, List<Double>> res = new HashMap<>();
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.V, new ArrayList<>());
        res.put(OutputType.W, new ArrayList<>());
        res.put(OutputType.H, new ArrayList<>());
        res.put(OutputType.ZM, new ArrayList<>());
        res.put(OutputType.ZAHP, new ArrayList<>());

        for (double t = 0; t < totalTime; t += dt) {
            rk4Step();
            res.get(OutputType.TIME).add(t);
            res.get(OutputType.V).add(V);
            res.get(OutputType.W).add(w);
            res.get(OutputType.H).add(h);
            res.get(OutputType.ZM).add(zM);
            res.get(OutputType.ZAHP).add(zAHP);
        }

        return res;
    }

    public Map<OutputType, List<Double>> startWithStimWave(Double[] stimWave){
        if (stimWave.length > totalTime/dt){
            throw new RuntimeException("stimWave not correct length");
        }

        Map<OutputType, List<Double>> res = new HashMap<>();
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.TIME, new ArrayList<>());
        res.put(OutputType.V, new ArrayList<>());
        res.put(OutputType.W, new ArrayList<>());
        res.put(OutputType.H, new ArrayList<>());
        res.put(OutputType.ZM, new ArrayList<>());
        res.put(OutputType.ZAHP, new ArrayList<>());

        for (int i = 0; i<stimWave.length; i++) {
            setI_DC(stimWave[i]);
            rk4Step();
            res.get(OutputType.TIME).add(i*dt);
            res.get(OutputType.V).add(V);
            res.get(OutputType.W).add(w);
            res.get(OutputType.H).add(h);
            res.get(OutputType.ZM).add(zM);
            res.get(OutputType.ZAHP).add(zAHP);
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
    public void rk4Step() {
        //Вычисление k1 (производные в текущей точке)
        double k1_V = dVdt();
        double k1_w = dwdt();
        double k1_zAHP = dzAHPdt();
        double k1_zM = dzMdt();
        double k1_inoise = di_noisedt();
        double k1_h = dhdt();

        //Вычисление k2 (производные в середине интервала, используя k1)
        double V_temp = V + dt * k1_V / 2;
        double w_temp = w + dt * k1_w / 2;
        double zAHP_temp = zAHP + dt * k1_zAHP / 2;
        double zM_temp = zM + dt * k1_zM / 2;
        double ino_temp = I_noise + dt * k1_inoise / 2;
        double h_temp = h + dt * k1_h /2;

        double k2_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k2_w = dwdtUsingTemp(V_temp);
        double k2_zAHP = dzAHPdtUsingTemp(V_temp);
        double k2_zM = dzMdtUsingTemp(V_temp);
        double k2_inoise = di_noisedtUsingTemp(ino_temp);
        double k2_h = dhdtUsingTemp(V_temp);

        // Вычисление k3 (производные в середине интервала, используя k2)
        V_temp = V + dt * k2_V / 2;
        w_temp = w + dt * k2_w / 2;
        zAHP_temp = zAHP + dt * k2_zAHP / 2;
        zM_temp = zM + dt * k2_zM / 2;
        ino_temp = I_noise + dt * k2_inoise / 2;
        h_temp = h + dt * k2_h / 2;

        double k3_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k3_w = dwdtUsingTemp(V_temp);
        double k3_zAHP = dzAHPdtUsingTemp(V_temp);
        double k3_zM = dzMdtUsingTemp(V_temp);
        double k3_inoise = di_noisedtUsingTemp(ino_temp);
        double k3_h = dhdtUsingTemp(V_temp);

        //Вычисление k4 (производные в конце интервала, используя k3)
        V_temp = V + dt * k3_V;
        w_temp = w + dt * k3_w;
        zAHP_temp = zAHP + dt * k3_zAHP;
        zM_temp = zM + dt * k3_zM;
        ino_temp = I_noise + dt * k3_inoise;
        h_temp = h + dt * k3_h;

        double k4_V = dVdtUsingTemp(V_temp, w_temp, zAHP_temp, zM_temp, ino_temp, h_temp);
        double k4_w = dwdtUsingTemp(V_temp);
        double k4_zAHP = dzAHPdtUsingTemp(V_temp);
        double k4_zM = dzMdtUsingTemp(V_temp);
        double k4_inoise = di_noisedtUsingTemp(ino_temp);
        double k4_h = dhdtUsingTemp(V_temp);

        //Обновление переменных состояния с использованием взвешенной суммы k1, k2, k3, k4
        V += dt * (k1_V + 2 * k2_V + 2 * k3_V + k4_V) / 6;
        w += dt * (k1_w + 2 * k2_w + 2 * k3_w + k4_w) / 6;
        zAHP += dt * (k1_zAHP + 2 * k2_zAHP + 2 * k3_zAHP + k4_zAHP) / 6;
        zM += dt * (k1_zM + 2 * k2_zM + 2 * k3_zM + k4_zM) / 6;
        I_noise += dt * (k1_inoise + 2 * k2_inoise + 2 * k3_inoise + k4_inoise) / 6;
        h += dt * (k1_h + 2 * k2_h + 2 * k3_h + k4_h) / 6;
    }

    // ========================================================================
    // Методы dVdt, dwdt, dzAHPdt, dzMdt с использованием временных значений для RK4
    // (необходимы для правильного вычисления производных на промежуточных шагах)
    // ========================================================================
    private double dVdtUsingTemp(double V_temp, double w_temp, double zAHP_temp, double zM_temp, double i_noise_temp, double h_temp) {
        return (I_DC + i_noise_temp - GNa * h_temp * minf(V_temp) * (V_temp - VNa) - GK * w_temp * (V_temp - VK) - Gshunt * (V_temp - Vshunt) - GM * zM_temp * (V_temp - VK) - GAHP * zAHP_temp * (V_temp - VK)) / Cm;
    }

    private double dwdtUsingTemp(double V_temp) {
        return phi_w * (winf(V_temp) - w) / tauw(V_temp);
    }

    private double dzAHPdtUsingTemp(double V_temp) {
        return (zinfAHP(V_temp) - zAHP) / tauzAHP;
    }

    private double dzMdtUsingTemp(double V_temp) {
        return (zinfM(V_temp) - zM) / tauzM;
    }

    private double di_noisedtUsingTemp(double i_noise_temp) {
        double nz = Math.random() * 2 - 1;
        return -1/tau_inoise*(i_noise_temp - I_avg) + sigma * nz;
    }

    private double dhdtUsingTemp(double V_temp){
        return (hinf(V_temp)-h)/tau_h;
    }
}