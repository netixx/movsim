package org.movsim.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import org.movsim.simulator.vehicles.Vehicle;

import au.com.bytecode.opencsv.CSVWriter;

public class TravelTime {

    private static TravelTime instance;
    private static boolean enabled = true;

    public static TravelTime getInstance() {
        if (instance == null) {
            instance = new TravelTime();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private HashMap<Vehicle, EntryExit> timeMap = new HashMap<>();

    
    public void recordEntry(Vehicle vh, double time) {
        if (enabled) {
            if (!timeMap.containsKey(vh)) {
                timeMap.put(vh, new EntryExit(time));
            } else {
                timeMap.get(vh).setEntry(time);
            }
        }
    }

    public void recordExit(Vehicle vh, double time) {
        if (enabled) {
            timeMap.get(vh).setExit(time);
        }
    }

    public void log(Vehicle vh, double increment) {
        if (enabled) {
            if (!timeMap.containsKey(vh)) {
                timeMap.put(vh, new EntryExit(0));
            } else {
                timeMap.get(vh).addIncrement(increment);
            }
        }
    }

    public void toCsv(Path path) {
        if (enabled) {
            Charset cs = Charset.forName("UTF-8");
            OpenOption[] opts = new OpenOption[] {
            // StandardOpenOption.WRITE, StandardOpenOption.CREATE
            };
            try (BufferedWriter writer = Files.newBufferedWriter(path, cs, opts)) {
                CSVWriter csv = new CSVWriter(writer, ';');
                csv.writeNext(new String[] { "id", "entry", "exit", "time" });
                for (Entry<Vehicle, EntryExit> entry : timeMap.entrySet()) {
                    csv.writeNext(new String[] { "" + entry.getKey().getId(), "" + entry.getValue().entry,
                            "" + entry.getValue().exit, "" + entry.getValue().time });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    private class EntryExit {

        double entry = 0;
        double exit = 0;
        double time = 0;

        EntryExit(double entry) {
            setEntry(entry);
        }
        
        void addIncrement(double increment) {
            time += increment;
        }
        
        void setEntry(double time) {
            entry = time;
        }

        void setExit(double time) {
            exit = time;
        }
    }

}
