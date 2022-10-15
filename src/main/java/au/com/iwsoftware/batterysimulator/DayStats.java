/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package au.com.iwsoftware.batterysimulator;

import java.util.Calendar;

/**
 *
 */
public class DayStats 
{
    private Calendar date;
    private double consumption;
    private double generation;

    /**
     * @return the date
     */
    public Calendar getDate()
    {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Calendar date)
    {
        this.date = date;
    }

    /**
     * @return the consumption
     */
    public double getConsumption()
    {
        return consumption;
    }

    /**
     * @param consumption the consumption to set
     */
    public void setConsumption(double consumption)
    {
        this.consumption = consumption;
    }

    /**
     * @return the generation
     */
    public double getGeneration()
    {
        return generation;
    }

    /**
     * @param generation the generation to set
     */
    public void setGeneration(double generation)
    {
        this.generation = generation;
    }
    
    public void addConsumption(double consumption)
    {
        this.consumption += consumption;
    }
    
    public void addGeneration(double generation)
    {
        this.generation += generation;
    }
}
