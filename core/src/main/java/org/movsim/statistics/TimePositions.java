package org.movsim.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.movsim.simulator.vehicles.Vehicle;

import au.com.bytecode.opencsv.CSVWriter;

public class TimePositions {

    private static TimePositions instance;
    private static boolean enabled = false;

    public static TimePositions getInstance() {
        if (instance == null) {
            instance = new TimePositions();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private HashMap<Double, PositionRecord> timeMap = new HashMap<>();

    
    public void log(Vehicle vh, double time) {
        if (enabled) {
            if (!timeMap.containsKey(time)) {
                timeMap.put(time, new PositionRecord(vh.getCurvAbsc()));
            } else {
                timeMap.get(time).addPos(vh.getCurvAbsc());
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
                csv.writeNext(new String[] { "time", "positions" });
                for (Entry<Double, PositionRecord> entry : timeMap.entrySet()) {
                    csv.writeNext(new String[] { "" + entry.getKey(), "" + entry.getValue() });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class PositionRecord {

        ArrayList<Double> positions = new ArrayList<>();

        PositionRecord(double pos) {
            addPos(pos);
        }

        void addPos(double pos) {
            positions.add(pos);
        }

        @Override
        public String toString() {
            return Arrays.toString(positions.toArray());

        }
    }

}
