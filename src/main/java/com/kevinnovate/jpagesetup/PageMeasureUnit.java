
package com.kevinnovate.jpagesetup;

/**
 * This enum defines the possible measurement units for the PageSetupDialog. The enum has methods to convert
 * to and from the PageFormat unit (which is 1/72th of an inch).
 * 
 * 
 * @author com.kevinnovate
 */
enum PageMeasureUnit {
    
    IN("inch", 72.0), 
    MM("millimeter", 72.0 / 25.4), 
    PT("point", 1.0);
    
    
    private final String fullName;  //the full unit name
    private final double scale;     //scale factor for converting to/from PageFormat units

    private PageMeasureUnit(String n, double s) {
        fullName = n;
        scale = s;
    }

    @Override
    public String toString() {
        return fullName;
    }

    /**
     * Returns the abbreviated two-character unit
     * @return 
     */
    String abbr() {
        return this.name().toLowerCase();
    }

    /**
     * Convert to PageFormat units.  PageFormat units are in points (1/72ths of an inch)
     * @param val the value to convert
     * @return value converted to PageFormat units
     */
    double toPFUnits(double val) {
        return val * this.scale;
    }

    /**
     * Convert from PageFormat units.  PageFormat units are in points (1/72ths of an inch)
     * @param val the value to convert
     * @return value converted to this PageMeasureUnit units
     */
    double fromPFUnits(double val) {
        
        switch (this) {
            case IN:
                return Math.round(100 * val / scale) / 100.0;  //round to 100ths
            case MM:
                return Math.round(10 * val / scale) / 10.0;  //round to 10ths
            case PT:
                return Math.round(val / scale);  //round to integers
            default:
                throw new RuntimeException("Unknown unit");
        }
        
    }

    Number getIncrementSize() {
        switch (this) {
            case IN:
                return 0.1d;
            case MM:
            case PT:
                return 1d;
            default:
                throw new RuntimeException("Unknown unit");
        }
    }
    
    String getNumberFormat() {
        switch (this) {
            case IN:
                return "0.##";
            case MM:
                return "0.#";
            case PT:
                return "0";
            default:
                throw new RuntimeException("Unknown unit");
        }
    }

   
}
