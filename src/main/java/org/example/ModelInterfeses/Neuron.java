package org.example.ModelInterfeses;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для общения с нейроном
 */
public interface Neuron {

    /**
     * Передает стимул в виде I
     * @param IStim
     */
    void setIStim(double IStim);

    /**
     * Получить текущий мембранный потенциал
     */
    double getVm();

    /**
     * Получить глобальный tau нейрона
     */
    double tauGlobal();

    /**
     * Получить Cm нейрона
     */

    double getCm();

    /**
     * Провести симуляцию с постоянным током (его можно задать через setIStim)
     * @retur таблица с переменными модели, что менялись в ходе симуляции
     */
    Map<VarType, List<Double>> start();


    /**
     * Провести симуляцию с постоянным током (его можно задать через setIStim)
     * @retur таблица с переменными модели, что менялись в ходе симуляции
     */
    Map<VarType, List<Double>> startWithStimWave(Double[] stimWave);

    /**
     * Получить время моделирования
     */
    double getTotalTime();


    /**
     * Получить времяной шаг моделирования
     * @return
     */
    double getDt();

    /**
     * Совершить шаг моделирования
     */
    void step();
}
