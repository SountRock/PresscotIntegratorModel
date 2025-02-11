package org.example;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class Main extends JFrame {


    public static void main(String[] args) {
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(20000);
        model.setDt(0.1);

        Map<OutputType, List<Double>> res = model.start();
        System.out.println(res.get(OutputType.V));
        System.out.println(res.get(OutputType.TIME));
    }
}