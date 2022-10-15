/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 *  Copyright (c) 2017 IWSoftware Pty Ltd.
 *  All rights reserved.
 */

package au.com.iwsoftware.batterysimulator;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 */
public class Battery
{
    private final String name;
    
    private final double price;
    
    private final double storageLimit;
    private final double roundtripEfficiency;
    private final double reserveForPeak;
    
    // Stats with battery
    private double lossesTotal;
    private double storedTotal;
    private double storedCurrent;
    
    private double offPeakDrained;
    private double peakDrained;
    
    private double sentToGrid;
    
    private double fromGridOffPeak;
    private double fromGridPeak;
    
    private SummaryStatistics storageStats;
    
    private double maxStoredToday;
    private SummaryStatistics maxDailyStorageStats = new SummaryStatistics();
    
    public Battery(String name, double price, double storageLimit, double roundtripEfficiency, double reserveForPeakPc)
    {
        this.name = name;
        this.price = price;
        this.storageLimit = storageLimit;
        this.roundtripEfficiency = roundtripEfficiency;
        this.reserveForPeak = storageLimit * reserveForPeakPc;
        storageStats = new SummaryStatistics();
    }
    
    public void endDay()
    {
        maxDailyStorageStats.addValue(maxStoredToday);
        maxStoredToday = 0.0;
    }
    
    public void runData(double consumed, double generated, boolean offPeak)
    {
        double available = (offPeak)?(storedCurrent > reserveForPeak ? storedCurrent - reserveForPeak : 0):storedCurrent;
        double amountDrained = Math.min(consumed, available);
        double amountStored = Math.min(generated, storageLimit - storedCurrent);
        
        // TODO: Better way to simulate effiency losses
        lossesTotal += amountStored * (1.0 - roundtripEfficiency);
        amountStored *= roundtripEfficiency;
        
        storedCurrent -= amountDrained;
        if (offPeak)
        {
            offPeakDrained += amountDrained;
            fromGridOffPeak += consumed - amountDrained;
        }
        else
        {
            peakDrained += amountDrained;
            fromGridPeak += consumed - amountDrained;
        }
        
        storedTotal += amountStored;
        storedCurrent += amountStored;
        
        if (storedCurrent > maxStoredToday)
        {
            maxStoredToday = storedCurrent;
        }
        
        sentToGrid += generated - amountStored;
        
        storageStats.addValue(storedCurrent);
    }

    /**
     * @return the stored
     */
    public double getStoredTotal()
    {
        return storedTotal;
    }

    /**
     * @param storedTotal the stored to set
     */
    public void setStoredTotal(double storedTotal)
    {
        this.storedTotal = storedTotal;
    }

    /**
     * @return the offPeakDrained
     */
    public double getOffPeakDrained()
    {
        return offPeakDrained;
    }

    /**
     * @param offPeakDrained the offPeakDrained to set
     */
    public void setOffPeakDrained(double offPeakDrained)
    {
        this.offPeakDrained = offPeakDrained;
    }

    /**
     * @return the peakDrained
     */
    public double getPeakDrained()
    {
        return peakDrained;
    }

    /**
     * @param peakDrained the peakDrained to set
     */
    public void setPeakDrained(double peakDrained)
    {
        this.peakDrained = peakDrained;
    }

    /**
     * @return the storedCurrent
     */
    public double getStoredCurrent()
    {
        return storedCurrent;
    }

    /**
     * @param storedCurrent the storedCurrent to set
     */
    public void setStoredCurrent(double storedCurrent)
    {
        this.storedCurrent = storedCurrent;
    }

    /**
     * @return the storageLimit
     */
    public double getStorageLimit()
    {
        return storageLimit;
    }

    /**
     * @return the sentToGrid
     */
    public double getSentToGrid()
    {
        return sentToGrid;
    }

    /**
     * @return the fromGridOffPeak
     */
    public double getFromGridOffPeak()
    {
        return fromGridOffPeak;
    }

    /**
     * @return the fromGridPeak
     */
    public double getFromGridPeak()
    {
        return fromGridPeak;
    }

    /**
     * @return the storageStats
     */
    public SummaryStatistics getStorageStats()
    {
        return storageStats;
    }

    public SummaryStatistics getMaxDailyStorageStats()
    {
        return maxDailyStorageStats;
    }

    /**
     * @return the lossesTotal
     */
    public double getLossesTotal()
    {
        return lossesTotal;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the roundtripEfficiency
     */
    public double getRoundtripEfficiency()
    {
        return roundtripEfficiency;
    }

    /**
     * @return the price
     */
    public double getPrice()
    {
        return price;
    }
}
