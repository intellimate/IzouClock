package org.intellimate.izou.addon.izouclock;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

        ClockController clockController = ClockController.getInstance();
        List<String> events = new ArrayList<>();
        events.add("blub");
        events.add("paff");
        clockController.scheduleAlarm("monday", false, 12, 4, 1, events);
    }
}
