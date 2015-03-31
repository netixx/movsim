package org.movsim.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class TimeNumber {

    private static TimeNumber instance;
    private static boolean enabled = true;

    public static TimeNumber getInstance() {
        if (instance == null) {
            instance = new TimeNumber();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private List<Record> records = new LinkedList<>();


    
    public void log(double time, int number) {
        if (enabled) {
            records.add(new Record(time, number));
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
                csv.writeNext(new String[] { "time", "number" });
                for (Record rec : records) {
                    csv.writeNext(new String[] { "" + rec.time, "" + rec.number });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class Record {
        final double time;
        final int number;

        Record(double time, int number) {
            this.number = number;
            this.time = time;
        }
    }

}
