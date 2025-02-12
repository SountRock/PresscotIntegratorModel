package org.example;

import org.example.Elemets.MorrisLecar;
import org.example.ModelInterfeses.VarType;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class Main extends JFrame {


    public static void main(String[] args) {
        MorrisLecar model = new MorrisLecar();
        model.setTotalTime(20000);
        model.setDt(0.1);

        Map<VarType, List<Double>> res = model.start();
        System.out.println(res.get(VarType.V));
        System.out.println(res.get(VarType.TIME));
    }
}