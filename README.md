# Home Battery Simulator

This application simulates a hypothetical home battery using power input / output data exported from Jemena's portal.

## Using the battery simulator

The main class is `BatterySimulator.java`. 

### Configuration

Currently all configuration is done using static variables:

`DATA_FILE`
  : The path, absolute or relative to the working directory, of the data export CSV file.

`OFF_PEAK_ENABLE`
  : Set to `true` to enable off peak calculations.

`OFF_PEAK_DAYS`
  : Days that are entirely off peak. List of integers referencing the `Calendar` constants.

`TIME_COLUMN_OFF_PEAK_START`
  : Columns after and including this are off-peak

`TIME_COLUMN_OFF_PEAK_END`
  : Columns up to and including this are off-peak

`RESERVE_FOR_PEAK`
  : The minimum level (in percentage 0.0 to 1.0) that the battery will drain to during an off-peak period

`RATE_FEED_IN`
  : The feed in price in dollars

`RATE_PEAK`
  : The peak grid draw price in dollars. If off-peak is disabled then this price is used for all calculations.

`RATE_OFF_PEAK`
  : The off-peak grid draw price in dollars.

Hypothetical batteries can then be added in the `main` method. For example:

~~~java
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
~~~

Provide:

  * The name of the battery
  * The price
  * The storage limit (in KWh)
  * The round trip efficiency (as a percentage, 0.0 to 1.0)
  * The percentage to reserve for peak times (copy the example above)

If not known, the storage limit and round trip efficiency can be found for most batteries
[here](https://www.solarquotes.com.au/battery-storage/comparison-table/).

### Output

The program outputs:

  * A human readable summary of the data with
    * "Gen" - Power generated, fed into grid
    * "Con" - Power consumed from the grid
  * For each battery:
    * A summary of the battery settings
    * The total amount that would have been stored over the data period
      * The money loss from not feeding into the grid
    * The total amount used from the battery rather than the grid
      * The money saved
  * A summary of the financial stats
    * Total money saved
    * Money saved per day
    * Estimated time to pay off the battery (Compare this to the battery warranty)
  * Additional stats

Example output:

~~~
Data Summary
============
Time period: 730 days
Power from grid (peak): 11,760.5 KWh ($2646.11) - av. 16.1 kWh / day
Power from grid (off peak): 0.0 KWh ($0.00) - av. 0.0 kWh / day
Power to grid: 12,686.9 KWh ($850.02)
Y: 2020, M: 6, Gen: 32.6, Con: 207.9
Y: 2020, M: 7, Gen: 138.7, Con: 641.6
Y: 2020, M: 8, Gen: 307.2, Con: 565.3
Y: 2020, M: 9, Gen: 639.3, Con: 308.6
Y: 2020, M: 10, Gen: 631.0, Con: 311.0
Y: 2020, M: 11, Gen: 881.4, Con: 339.3
Y: 2020, M: 12, Gen: 1096.5, Con: 273.8
Y: 2021, M: 1, Gen: 814.9, Con: 454.5
Y: 2021, M: 2, Gen: 656.4, Con: 435.8
Y: 2021, M: 3, Gen: 641.5, Con: 352.8
Y: 2021, M: 4, Gen: 465.5, Con: 329.8
Y: 2021, M: 5, Gen: 205.2, Con: 531.2
Y: 2021, M: 6, Gen: 80.5, Con: 726.7
Y: 2021, M: 7, Gen: 120.6, Con: 808.9
Y: 2021, M: 8, Gen: 321.6, Con: 600.8
Y: 2021, M: 9, Gen: 600.5, Con: 387.0
Y: 2021, M: 10, Gen: 735.5, Con: 407.1
Y: 2021, M: 11, Gen: 755.7, Con: 380.9
Y: 2021, M: 12, Gen: 1093.9, Con: 319.1
Y: 2022, M: 1, Gen: 696.8, Con: 603.8
Y: 2022, M: 2, Gen: 651.1, Con: 476.2
Y: 2022, M: 3, Gen: 476.1, Con: 607.8
Y: 2022, M: 4, Gen: 395.0, Con: 377.2
Y: 2022, M: 5, Gen: 183.8, Con: 742.8
Y: 2022, M: 6, Gen: 65.6, Con: 570.6

============

Origin: LG Chem 6.5

Idealised 5.9 KWh system, using 730 days of data
Reserving 0.0 KWh for peak usage

Total Stored: 4,011.6 KWh (-$268.78) (31.6%) - av. 5.5 / day
Saved (off-peak): 0.0 KWh ($0.00)
Saved (peak): 4,011.6 KWh ($902.61)

Total difference: $619.69
Difference / day: $0.85
Time to pay off $4,595.00: 5412 days (~ 14.8 years)

Lost in storage: 211.1 KWh (1.7%)
Sent to grid: 8,675.3 KWh (68.4%)
From grid (off-peak): 0.0 KWh
From grid (peak): 7,748.9 KWh

Average stored: 1.7
Maximum stored: 5.9

Average max daily stored: 4.9

============

Origin: LG Chem 10

Idealised 8.8 KWh system, using 730 days of data
Reserving 0.0 KWh for peak usage

Total Stored: 5,126.3 KWh (-$343.46) (40.4%) - av. 7.0 / day
Saved (off-peak): 0.0 KWh ($0.00)
Saved (peak): 5,126.3 KWh ($1,153.41)

Total difference: $791.88
Difference / day: $1.08
Time to pay off $5,195.00: 4789 days (~ 13.1 years)

Lost in storage: 269.8 KWh (2.1%)
Sent to grid: 7,560.6 KWh (59.6%)
From grid (off-peak): 0.0 KWh
From grid (peak): 6,634.2 KWh

Average stored: 2.8
Maximum stored: 8.8

Average max daily stored: 6.8
~~~
