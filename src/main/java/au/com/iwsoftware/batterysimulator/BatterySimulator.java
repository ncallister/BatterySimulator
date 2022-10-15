/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 *  Copyright (c) 2017 IWSoftware Pty Ltd.
 *  All rights reserved.
 */

package au.com.iwsoftware.batterysimulator;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class BatterySimulator 
{
    public static final String DATA_FILE = "./electricity-data.csv";
    
    // Start on the second row.
    public static final int ROW_START = 1;
    
    public static final int COLUMN_CON_GEN = 2;
    public static final int COLUMN_DATE = 3;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    
    public static final int COLUMN_DATA_START = 5;
    
    public static final List<Integer> OFF_PEAK_DAYS = Arrays.asList(new Integer[]
    {
        Calendar.SATURDAY,
        Calendar.SUNDAY
    });
    
    public static final boolean OFF_PEAK_ENABLE = false;
    /**
     * Columns up to and including this are off-peak
     */
    public static final int TIME_COLUMN_OFF_PEAK_END = 13;
    
    /**
     * Columns after and including this are off-peak
     */
    public static final int TIME_COLUMN_OFF_PEAK_START = 46;
    
    public static final double RESERVE_FOR_PEAK = 0.4; // reserve 40% for peak
    
    // All in dollars
    public static final double RATE_FEED_IN = 0.067;
    public static final double RATE_PEAK = 0.225;
    public static final double RATE_OFF_PEAK = 0.13;
    
    /// Statistics of raw data (no battery consideration)
    // Day stats
    private static List<DayStats> dayStats = new ArrayList<>();
    
    private static double totalConsumedPeak;
    private static double totalConsumedOffPeak;
    private static double totalGenerated;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        CSV csv = CSV.create();
        CSVReader reader = csv.reader(DATA_FILE);
        
        List<String[]> data = reader.readAll();
        int days = (data.size() - 1) / 2;
        
        // Define batteries
        List<Battery> batteries = new ArrayList<>();
        batteries.add(new Battery(
                "Origin: LG Chem 6.5",
                4595.0,
                5.9, 
                0.95,
                OFF_PEAK_ENABLE ? RESERVE_FOR_PEAK : 0.0));
        batteries.add(new Battery(
                "Origin: LG Chem 10",
                5195.0,
                8.8, 
                0.95,
                OFF_PEAK_ENABLE ? RESERVE_FOR_PEAK : 0.0));
        
        for (int dayRow = 0 ; dayRow < days ; dayRow++)
        {
            processRow(data, dayRow, batteries);
        }
        
        System.out.println("Data Summary");
        System.out.println("============");
        System.out.println(String.format("Time period: %d days", days));
        System.out.println(
                String.format(
                        "Power from grid (peak): %,.1f KWh ($%.2f) - av. %.1f kWh / day", 
                        totalConsumedPeak, 
                        totalConsumedPeak * RATE_PEAK, 
                        totalConsumedPeak / days));
        System.out.println(
                String.format(
                        "Power from grid (off peak): %,.1f KWh ($%.2f) - av. %.1f kWh / day", 
                        totalConsumedOffPeak, 
                        totalConsumedOffPeak * RATE_OFF_PEAK, 
                        totalConsumedOffPeak / days));
        System.out.println(
                String.format(
                        "Power to grid: %,.1f KWh ($%.2f)", 
                        totalGenerated, 
                        totalGenerated * RATE_FEED_IN));
        
        int monthSize = 0;
        for (int i = 0 ; i < dayStats.size() ; i += monthSize)
        {
            monthSize = printMonth(dayStats.get(i).getDate().get(Calendar.YEAR), dayStats.get(i).getDate().get(Calendar.MONTH));
        }
        
        for (Battery battery : batteries)
        {
            printBatteryStats(battery, days);
        }
    }
    
    private static void printBatteryStats(Battery battery, int days)
    {
        System.out.println();
       
        System.out.println("============");
        System.out.println();
        System.out.println(battery.getName());
        System.out.println();
        
        System.out.println(String.format("Idealised %.1f KWh system, using %d days of data", battery.getStorageLimit(), days));
        System.out.println(String.format("Reserving %.1f KWh for peak usage", OFF_PEAK_ENABLE ? RESERVE_FOR_PEAK : 0.0));
        System.out.println();
        
        System.out.println(String.format("Total Stored: %,.1f KWh (-$%.2f) (%.1f%%) - av. %.1f / day", 
                                         battery.getStoredTotal(), 
                                         battery.getStoredTotal() * RATE_FEED_IN,
                                         battery.getStoredTotal() * 100.0 / totalGenerated,
                                         battery.getStoredTotal() / days));
        
        System.out.println(String.format("Saved (off-peak): %,.1f KWh ($%,.2f)", 
                                         battery.getOffPeakDrained(),
                                         battery.getOffPeakDrained() * RATE_OFF_PEAK));
        
        System.out.println(String.format("Saved (peak): %,.1f KWh ($%,.2f)", 
                                         battery.getPeakDrained(),
                                         battery.getPeakDrained() * RATE_PEAK));
        System.out.println();
        
        double saved = (battery.getOffPeakDrained() * RATE_OFF_PEAK) + (battery.getPeakDrained() * RATE_PEAK) - (battery.getStoredTotal() * RATE_FEED_IN) - (battery.getLossesTotal() * RATE_FEED_IN);
        System.out.println(String.format("Total difference: $%,.2f",
                                         saved));
        System.out.println(String.format("Difference / day: $%,.2f",
                                         saved / days));
        System.out.println(String.format("Time to pay off $%,.2f: %d days (~ %.1f years)",
                                         battery.getPrice(),
                                         (int)(battery.getPrice() / (saved / days)),
                                         (battery.getPrice() / (saved / days)) / 365));
        
        System.out.println();
        System.out.println(
                String.format(
                        "Lost in storage: %,.1f KWh (%.1f%%)", 
                        battery.getLossesTotal(), 
                        battery.getLossesTotal() * 100.0 / totalGenerated));
        System.out.println(
                String.format(
                        "Sent to grid: %,.1f KWh (%.1f%%)", 
                        battery.getSentToGrid(), 
                        battery.getSentToGrid() * 100.0 / totalGenerated));
        System.out.println(String.format("From grid (off-peak): %,.1f KWh", battery.getFromGridOffPeak()));
        System.out.println(String.format("From grid (peak): %,.1f KWh", battery.getFromGridPeak()));
        System.out.println();
        System.out.println(String.format("Average stored: %.1f", battery.getStorageStats().getMean()));
        System.out.println(String.format("Maximum stored: %.1f", battery.getStorageStats().getMax()));
        System.out.println();
        System.out.println(String.format("Average max daily stored: %.1f", battery.getMaxDailyStorageStats().getMean()));
    }
    
    private static int printMonth(int year, int month)
    {
        List<DayStats> monthDayStats = dayStats.stream()
                .filter(ds -> ds.getDate().get(Calendar.YEAR) == year && ds.getDate().get(Calendar.MONTH) == month)
                .collect(Collectors.toList());
        
        double generated = monthDayStats.stream().collect(Collectors.summingDouble(ds -> ds.getGeneration()));
        double consumed = monthDayStats.stream().collect(Collectors.summingDouble(ds -> ds.getConsumption()));
        
        System.out.println(String.format("Y: %d, M: %d, Gen: %.1f, Con: %.1f", year, month + 1, generated, consumed));
        
        return monthDayStats.size();
    }
    
    private static void processRow(List<String[]> data, int index, List<Battery> batteries) throws ParseException
    {
        int consumptionRow = getConsumptionRow(index);
        int generationRow = getGenerationRow(index);
        
        Calendar day = Calendar.getInstance();
        day.setTime(DATE_FORMAT.parse(data.get(consumptionRow)[COLUMN_DATE]));
        
        DayStats dayStat = new DayStats();
        dayStat.setDate(day);
        dayStats.add(dayStat);
        
        // sanity check
        if (!"Consumption".equals(data.get(consumptionRow)[COLUMN_CON_GEN]) ||
            !"Generation".equals(data.get(generationRow)[COLUMN_CON_GEN]) ||
            !data.get(consumptionRow)[COLUMN_DATE].equals(data.get(generationRow)[COLUMN_DATE]))
        {
            throw new RuntimeException("Row mismatch");
        }
        
        for (int i = 0 ; i < 48 ; ++i)
        {
            double consumed = Double.valueOf(data.get(consumptionRow)[i + COLUMN_DATA_START]);
            double generated = Double.valueOf(data.get(generationRow)[i + COLUMN_DATA_START]);
            boolean offPeak = isOffPeak(day, i);
            
            for (Battery battery : batteries)
            {
                battery.runData(consumed, generated, offPeak);
            }
            dayStat.addGeneration(generated);
            dayStat.addConsumption(consumed);
            
            totalGenerated += generated;
            if (offPeak)
            {
                totalConsumedOffPeak += consumed;
            }
            else
            {
                totalConsumedPeak += consumed;
            }
        }
        
        for (Battery battery : batteries)
        {
            battery.endDay();
        }
    }
    
    private static int getConsumptionRow(int index)
    {
        return (index * 2) + ROW_START;
    }
    
    private static int getGenerationRow(int index)
    {
        return (index * 2) + ROW_START + 1;
    }
    
    private static boolean isOffPeak(Calendar day, int timeColumn)
    {
        if (!OFF_PEAK_ENABLE)
        {
            return false;
        }
        
        boolean offPeak = false;
        
        if (OFF_PEAK_DAYS.contains(day.get(Calendar.DAY_OF_WEEK)))
        {
            offPeak = true;
        }
        
        if (timeColumn <= TIME_COLUMN_OFF_PEAK_END || timeColumn >= TIME_COLUMN_OFF_PEAK_START)
        {
            offPeak = true;
        }
        
        return offPeak;
    }
}
