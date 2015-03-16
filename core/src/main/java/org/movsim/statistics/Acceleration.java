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

public class Acceleration {

    private static Acceleration instance;
    private static boolean enabled = true;

    public static Acceleration getInstance() {
        if (instance == null) {
            instance = new Acceleration();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private HashMap<Vehicle, AccelerationRecord> accMap = new HashMap<>();

    
    public void log(Vehicle vh, double acc) {
        if (enabled) {
            if (!accMap.containsKey(vh)) {
                accMap.put(vh, new AccelerationRecord(acc));
            } else {
                accMap.get(vh).addAcc(acc);
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
                csv.writeNext(new String[] { "id", "cumulatedMinus", "cumulatedPlus", "avg", "std", "max", "min" });
                for (Entry<Vehicle, AccelerationRecord> entry : accMap.entrySet()) {
                    csv.writeNext(new String[] { "" + entry.getKey().getId(), "" + entry.getValue().minusAcc,
                            "" + entry.getValue().plusAcc, });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class AccelerationRecord {

        double plusAcc = 0;
        double minusAcc = 0;

        int n = 0;
        double m = 0;
        double s = 0;
        double min = 0;
        double max = 0;

        AccelerationRecord(double acc) {
            min = acc;
            max = acc;
            addAcc(acc);
        }

        void addAcc(double acc) {
            min = Math.min(acc, min);
            max = Math.max(acc, max);
            double tmpM = m;
            m += (acc - tmpM) / n;
            s += (acc - tmpM) * (acc - m);
            n++;
            if (acc > 0) {
                plusAcc += acc;
            } else if (acc < 0) {
                minusAcc += acc;
            }
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
