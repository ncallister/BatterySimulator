/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package au.com.iwsoftware.batterysimulator;

import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 *
 */
public class UsageSummary 
{
    private double totalDraw;
    private double totalFeed;
    
    private DoubleSummaryStatistics dailyDrawStats;
    private DoubleSummaryStatistics dailyFeedStats;
    
    public static final int CON_GEN_COLUMN_DEFAULT = 2;
    
    public static final String CON_GEN_HEADING = "CON/GEN";
    public static final String CON_GEN_DRAW = "Consumption";
    public static final String CON_GEN_FEED = "Generation";
    
    public void processData(List<String[]> data)
    {
        
    }
}
