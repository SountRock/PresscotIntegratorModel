package org.example.Generator;

import org.example.Elemets.MorrisLecar;
import org.example.ModelInterfeses.VarType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Вспомогательный класс внешней стимуляции
 */
public class StimGenerator {
    /**
     * Сгенерировать синусоиду
     * @param len
     * @param amplitude
     * @param frequency
     * @param phase
     * @return
     */
    public static Double[] generateSinusoid(int len, double amplitude, double frequency, double phase) {
        Double[] sinusoid = new Double[len];
        for (int i = 0; i < len; i++) {
            double angle = 2 * Math.PI * frequency * i / len + phase;
            sinusoid[i] = amplitude * Math.sin(angle);
        }
        return sinusoid;
    }

    /**
     * Генерация одиночного простого спайка
     * @param len
     * @param phase
     * @return
     */
    public static Double[] generateSimplySpike(int len, double phase) {
        MorrisLecar modelStim = new MorrisLecar();
        modelStim.setTotalTime(100);
        modelStim.setDt(0.1);
        modelStim.setIStim(34);
        Map<VarType, List<Double>> res = modelStim.start();

        List<Double> values = res.get(VarType.V);
        List<Double> times = res.get(VarType.TIME);

        //Получение семпла одиночного спайка//////////////////////////////////
        double startTime = 32;
        double endTime = 83.1;
        int startIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == startTime ) {
                startIndex = i;
                break;
            }
        }
        int endIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == endTime ) {
                endIndex = i;
                break;
            }
        }

        Double[] spikeSample = values.subList(startIndex, endIndex).toArray(new Double[0]);
        //Получение семпла одиночного спайка//////////////////////////////////

        //Рассчет индекса фазы начала спайка//////////////////////////////////
        int phaseIndex = 0;
        for (int i = 0; i < times.size(); i++) {
            if ( (Math.round(times.get(i) * 10.0) / 10.0) == phase ) {
                phaseIndex = i;
                break;
            }
        }
        //Рассчет индекса фазы начала спайка//////////////////////////////////

        Double[] wave = new Double[len];
        Arrays.fill(wave, spikeSample[spikeSample.length - 1]);
        System.arraycopy(spikeSample, 0, wave, phaseIndex, spikeSample.length);
        System.out.println(Arrays.toString(wave));

        return wave;
    }
}
