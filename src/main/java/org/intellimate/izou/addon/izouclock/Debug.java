package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import java.util.LinkedList;

/**
 * Use this class to debug
 */
public class Debug {

    /**
     * Main method - still use to debug
     *
     * @param args the classic argument of the main method
     */
    public static void main(String[] args) {
        LinkedList<AddOnModel> addOns = new LinkedList<>();
        ClockAddOn clockAddOn = new ClockAddOn();
        addOns.add(clockAddOn);
        Main main = new Main(addOns);
        //Main.main(null);
    }
}
