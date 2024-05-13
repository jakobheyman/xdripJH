# xdripJH
> Modified version of xDrip+

 xDrip+ source: https://github.com/NightscoutFoundation/xDrip

## Major notes
* Modifications done primarily for Libre-2 OOP2 setup
* Display 1 min data instead of original 5 min
* Default calibration changed including optional historic recalculation of bg values (see below)
* Use OOP raw or calibrated bg as main data (Less common settings > Other misc. options)
* Use raw/calibrated data as blood glucose values if there is no calibration available
* Display raw data (Graph Settings > Display raw data plot)
* Display OOP calibrated data (Graph Settings > Display OOP calibrated data plot)
* Display last OOP calibrated value in home screen (Graph Settings > Display OOP calibrated bg value)
* Optionally modify Libre-2 raw values with multiplication and/or +/- (Advanced settings for Libre 2)
* Display mmol glucose values with two decimal digits
* Extended potential min and max blood glucose values
* 1 second X (time) resolution in graph
* Keep Y range constant in main graph and optionally modify min and max Y (Graph Settings)
* Modify X/Y display when switching between portrait/landscape (Graph Settings)
* Horizontal zoom in main graph and vertical zoom in preview graph
* Optionally set custom vibration pattern for low/high alerts (example: 0,150,70,150,70,500)
* Optionally hide noise line (Graph Settings)
* Calibration Graph uses the same unit (mg/dl or mmol/l) for raw and calibrated values
* Modified basal TBR and micro bolus icons display
* Statistics buttons added (DD) with custom start (D1) and stop (D2) dates
* There may be issues/problems introduced by the modifications
* There is no proper testing of the application

### Calibration notes
* Slope and intercept calculated with weights decreasing linearly with time from calibration point
* The weighting can be controlled with these three parameters (Advanced Calibration)
  - Calibration weight days - time over which the weight decreases from 1 to 0
  - Calibration weight days initial - weight time used at the start of a new sensor
  - Calibration weight days initial transition - transition time from initial to standard weight days
* Use raw data as blood glucose if there is no calibration available (no initial warm up period)
* No specific initial calibration used (slope = 1 after the first calibration)
* Optionally recalculate all affected calibrations and glucose values at calibration (Graph Settings)
