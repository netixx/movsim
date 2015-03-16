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

public class Speed {

    private static Speed instance;
    private static boolean enabled = true;

    public static Speed getInstance() {
        if (instance == null) {
            instance = new Speed();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private HashMap<Vehicle, SpeedRecord> speedMap = new HashMap<>();

    
    public void log(Vehicle vh, double speed) {
        if (enabled) {
            if (!speedMap.containsKey(vh)) {
                speedMap.put(vh, new SpeedRecord(speed));
            } else {
                speedMap.get(vh).addSpeed(speed);
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
                csv.writeNext(new String[] { "id", "avg", "std" });
                for (Entry<Vehicle, SpeedRecord> entry : speedMap.entrySet()) {
                    csv.writeNext(new String[] { "" + entry.getKey().getId(), "" + entry.getValue().getAvg(),
                            "" + entry.getValue().getStd(), "" + entry.getValue().getMax(),
                            "" + entry.getValue().getMin() });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SpeedRecord {
        int n = 0;
        double m = 0;
        double s = 0;
        double min = 0;
        double max = 0;

        SpeedRecord(double speed) {
            n = 1;
            max = speed;
            min = speed;
            addSpeed(speed);
        }

        void addSpeed(double speed) {
            min = Math.min(speed, min);
            max = Math.max(speed, max);
            double tmpM = m;
            m += (speed - tmpM) / n;
            s += (speed - tmpM) * (speed - m);
            n++;
        }
        
        double getAvg() {
            return m;
        }

        double getVariance() {
            return s / (n - 1);
        }

        double getStd() {
            return Math.sqrt(getVariance());
        }

        double getMin() {
            return min;
        }

        double getMax() {
            return max;
        }

    }

}
