
package com.kevinnovate.jpagesetup;

import java.util.LinkedList;

/**
 * Encapsulates a predefined Page type for selection by PageSetupDialog. Many different US and international types are available.
 * 
 * A user can add custom types as well.
 * 
 * @author com.kevinnovate
 */
final class AutoPageType {

    private static final LinkedList<AutoPageType> types = new LinkedList<>();

    
    /**
     * Get all possible AutoPageType objects. This method is synchronized with addType().
     * @return an array of AutoPageType objects
     */
    public static synchronized AutoPageType[] getAll() {
          
        AutoPageType[] array = new AutoPageType[types.size()];
        types.toArray(array);
        return array;
    }
    
    //Constants for ISO size calculations. Formulas from: https://en.wikipedia.org/wiki/Paper_size#Overview:_ISO_paper_sizes
    private static final double THETA_A = 1000 * Math.pow(2, .25);  //4th root of 2
    private static final double THETA_B = 1000 * Math.pow(2, .5);   //square root of 2
    private static final double THETA_C = 1000 * Math.pow(8, .125);  //8th root of 8
    
    
    private static double getISOWidth(double i, double theta) {
        return Math.round(theta * Math.pow(2.0, -(i+1)/2.0));
    }
    
    private static double getISOHeight(double i, double theta) {
        return Math.round(theta * Math.pow(2.0, -i/2.0));
    }
    
    /**
     * Add a new custom type to the list. This method is synchronized with getAll().
     * @param category the category, which can match an existing category to group with
     * @param name the new unique name
     * @param width width of the paper 
     * @param height height of the paper
     * @param unit units of width and height
     * @return true if added, false if the name already exists
     */
    public static synchronized boolean addType(String category, String name, double width, double height, PageMeasureUnit unit) {
        AutoPageType t = new AutoPageType(category, name, width, height, unit);
        if (types.contains(t))
            return false;
        
        types.add(t);
        return true;
    }
    
    
    static {
        
        //US and Canada sizes
        types.add(new AutoPageType("US/ANSI", "Letter (ANSI A)", 8.5, 11, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US/ANSI", "Legal", 8.5, 14, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US/ANSI", "Tabloid/Ledger (ANSI B)", 11, 17, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US/ANSI", "Executive", 7.25, 10.55, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US/ANSI", "Government Letter", 8.5, 10.5, PageMeasureUnit.IN));
        types.add(new AutoPageType("US/ANSI", "Government Legal (Oficio/Folio)", 8.5, 13, PageMeasureUnit.IN));
        types.add(new AutoPageType("US/ANSI", "ANSI C", 17, 22, PageMeasureUnit.IN));
        types.add(new AutoPageType("US/ANSI", "ANSI D", 22, 34, PageMeasureUnit.IN));
        types.add(new AutoPageType("US/ANSI", "ANSI E", 34, 44, PageMeasureUnit.IN));
        types.add(new AutoPageType("US/ANSI", "Half Letter (Statement/Stationery)", 5.5, 8.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US/ANSI", "Junior Legal", 5, 6, PageMeasureUnit.IN));
              
        types.add(new AutoPageType("US Envelope (Commercial)", "6-1/4", 6.0, 3.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "6-3/4", 6.5, 3.625, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "7", 6.75, 3.75, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "7-3/4 (Monarch)", 7.5, 3.875, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "8-5/8", 8.625, 3.625, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "9", 8.875, 3.875, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "10 (Common)", 9.5, 4.125, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "11", 10.375, 4.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "12", 11, 4.75, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "14", 11.5, 5.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Commercial)", "16", 12, 6.0, PageMeasureUnit.IN)); 
          
        types.add(new AutoPageType("US Envelope (Announcement)", "A1", 3.625, 5.125, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A2 (Lady Grey)", 5.75, 4.375, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A4", 6.25, 4.25, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A6 (Thompson's Standard)", 6.5, 4.75, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A7 (Besselheim)", 7.25, 5.25, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A8 (Carr's)", 8.125, 5.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A9 (Diplomat)", 8.75, 5.75, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A10 (Willow)", 9.5, 6.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Announcement)", "A Long", 8.875, 3.875, PageMeasureUnit.IN)); 
     
        types.add(new AutoPageType("US Envelope (Catalog)", "1", 9.0, 6.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "1-3/4", 9.5, 6.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "3", 10.0, 7.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "6", 10.5, 7.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "8", 11.25, 8.25, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "9-3/4", 11.25, 8.75, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "10-1/2", 12.0, 9.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "12-1/2", 12.5, 9.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "13-1/2", 13, 10.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "14-1/2", 14.5, 11.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "15", 15.0, 10.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Envelope (Catalog)", "15-1/2", 15.5, 12.0, PageMeasureUnit.IN)); 

        //US Architectural
        types.add(new AutoPageType("US Architectural", "Arch A", 9, 12.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Architectural", "Arch B", 12, 18.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Architectural", "Arch C", 18, 24.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Architectural", "Arch D", 24, 36.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Architectural", "Arch E", 36, 48.0, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("US Architectural", "Arch E1", 30, 42.0, PageMeasureUnit.IN)); 

        
        //ISO A, B, and C sizes
        for (int i=0; i<=10; i++) {
            
            types.add(new AutoPageType("ISO A", "A" + i, getISOWidth(i, THETA_A), getISOHeight(i, THETA_A), PageMeasureUnit.MM));
            types.add(new AutoPageType("ISO B", "B" + i, getISOWidth(i, THETA_B), getISOHeight(i, THETA_B), PageMeasureUnit.MM));
            types.add(new AutoPageType("ISO C", "C" + i, getISOWidth(i, THETA_C), getISOHeight(i, THETA_C), PageMeasureUnit.MM));
            
        }
        
        //Cards
        types.add(new AutoPageType("Card", "3x5 Index", 3, 5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Card", "4x6 Index", 4, 6, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Card", "5x8 Index", 5, 8, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Card", "International Business", 53.98, 85.6, PageMeasureUnit.MM)); 
        types.add(new AutoPageType("Card", "US Business", 2, 3.5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Card", "Japanese Business", 50, 90, PageMeasureUnit.MM)); 
        
         //Photo
        types.add(new AutoPageType("Photo", "3x5", 3, 5, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "4x6", 4, 6, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "5x7", 5, 7, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "6x8", 6, 8, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "8x10", 8, 10, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "8x12 ", 8, 12, PageMeasureUnit.IN)); 
        types.add(new AutoPageType("Photo", "11x14 ", 11, 14, PageMeasureUnit.IN)); 
              
        //Other
        types.add(new AutoPageType("Other", "ISO DL Envelope", 220.0, 110.0, PageMeasureUnit.MM)); 
        types.add(new AutoPageType("Other", "JIS B4", 257.0, 364.0, PageMeasureUnit.MM)); 
        types.add(new AutoPageType("Other", "JIS B5", 182.0, 257.0, PageMeasureUnit.MM)); 
        types.add(new AutoPageType("Other", "F4", 210.0, 330.0, PageMeasureUnit.MM)); 

        
    }
     
    

    private final double width;
    private final double height;
    private final String category;
    private final String dimensionStr;
    private final String readableName;
    private final PageMeasureUnit unit;

    /**
     * Create a type
     * @param cat the category for the type
     * @param n the name of the type
     * @param w width, in units of u
     * @param h height, in units of u
     * @param u units for the dimension measurements
     */
    private AutoPageType(String cat, String n, double w, double h, PageMeasureUnit u) {
        category = cat;
        readableName = n;
        dimensionStr = w + " x " + h + " " + u.abbr();
        
        //Convert to common PageFormat units
        width = u.toPFUnits(w);
        height = u.toPFUnits(h);
        unit = u;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Gets the width in PageFormat units
     * @return width of the Paper type
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height in PageFormat units
     * @return height of the Paper type
     */
    public double getHeight() {
        return height;
    }

    public String getDimensionString() {
        return dimensionStr;
    }

    @Override
    public String toString() {
        return readableName;
    }

    public PageMeasureUnit getUnit() {
        return unit;
    }
    
}
