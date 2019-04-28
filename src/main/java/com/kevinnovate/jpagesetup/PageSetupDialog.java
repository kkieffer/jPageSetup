
package com.kevinnovate.jpagesetup;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.HashMap;
import java.util.Locale;
import javax.print.PrintService;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;

/**
 * This is the primary class for jPageSetup.  This class extends the JDialog class and provides a native Java replacement for the 
 * PrinterJob.pageDialog() method.
 * 
 * The user can select from pre-defined paper sizes or set the size manually, as well as the margin. The user can select the 
 * default page for a printer, or validate the set size against a printer's capabilities.
 * 
 * This dialog provides the user selection results in a standard java.awt.print.PageFormat object.
 * 
 * The default measurement is based on the user's locale.  US and Canada default to inches, otherwise millimeters
 * 
 * Pressing the Escape key cancels the dialog.
 * 
 * Calling getDialogResult() returns the result of this Dialog
 * 
 * The user-selected PageFormat is validated against the selected printer, unless the printer is set to "Any Printer"
 * 
 * @author com.kevinnovate
 */
public class PageSetupDialog extends javax.swing.JDialog {

  
    /**
     * This class used to hold PrintService entries in the printer combo box
     */
    private static class PrintServiceEntry {
        private final PrintService ps;
        
        private PrintServiceEntry(PrintService p) {
            ps = p;
        }
        
        private PrintService getPrintService() {
            return ps;
        }
        
        @Override
        public String toString() {
            if (ps == null)
                return "Any Printer";
            else
                return ps.getName();
        }
        
    }
 
    /**
     * Compare dimensions and consider them equal if their difference is less than .1. This is used to 
     * check validated PageFormat dimensions, which may be slightly different due to rounding.
     * @param a the first dimension
     * @param b the second dimension
     * @return true if "equal" enough, false otherwise
     */
    private static boolean compareDimensions(double a, double b) {
        return Math.abs(a - b) < 0.1;
    }
    
    
    private static ImageIcon landscapeIcon = new ImageIcon(PageSetupDialog.class.getResource("/icons/landscape_orientation.png"));
    private static ImageIcon portraitIcon = new ImageIcon(PageSetupDialog.class.getResource("/icons/portrait_orientation.png"));
    private static ImageIcon revLandscapeIcon = new ImageIcon(PageSetupDialog.class.getResource("/icons/rev_landscape_orientation.png"));
    
    private PageMeasureUnit unit = null;
    private PageFormat returnFormat = null;
    private final Icon errorIcon;
    private final JPopupMenu autoPaperMenu;
    
    /**
     * From the provided PageFormat, validate against the limitations of the supplied printer. 
     * @param s the printer service to validate against.  Use null for the default printer.
     * @param f the page format to validate. Use null for the default PageFormat for the supplied printer
     * @return a validated, possibly changed PageFormat object that meets the limitations of the printer
     */
    private static PageFormat validateForPrinter(PrintService s, PageFormat f)  {
            
        PrinterJob job = PrinterJob.getPrinterJob(); 
        if (s != null) {
            try {
                job.setPrintService(s);  //try and set the requested printer
            } catch (PrinterException ex) {  //if that doesn't work, use default print service
            }  
        }

        if (f == null)
            f = job.defaultPage();
         
        //Get the default page format for printer, but remove margins. Seems to be a bug with the imageable area.
        //instead, validate the format against the printer to get the minimum margins
        Paper p = new Paper();  
        p.setImageableArea(0, 0, f.getWidth(), f.getHeight());    
        f.setPaper(p);
        return job.validatePage(f);
    }
    
    private int currentOrientation;
            
    /**
     * Create the PageSetupDialog
     * @param parent the parent frame
     * @param modal true for modal
     * @param fmt the PageFormat to initialize with. If null, the format will be initialized with the default printer format
     * @param errorIcon the custom icon to show on error message popups (JOptionPanes), null to use default Java icon
     * @param dialogIconImage the icon to show on the frame title bar, null for default Java icon
     */
    public PageSetupDialog(java.awt.Frame parent, boolean modal, PageFormat fmt, Icon errorIcon, Image dialogIconImage) {
        super(parent, modal);
        initComponents();
        
        this.errorIcon = errorIcon;
        
        //Set the default unit
        String defaultCountry = Locale.getDefault().getCountry();
        if (defaultCountry != null && (defaultCountry.equals(Locale.US.getCountry()) || defaultCountry.equals(Locale.CANADA.getCountry())))
            unit = PageMeasureUnit.IN;
        else
            unit = PageMeasureUnit.MM;
        
        
        //Add all the units to the measurement Combo Box
        for (PageMeasureUnit u : PageMeasureUnit.values())
            measureUnitComboBox.addItem(u);     
        measureUnitComboBox.setSelectedItem(unit);  

        //Create the Popupmenu that is created when pressing the autoPaperSize button with all the AutoPage entries
        autoPaperMenu = new JPopupMenu();
        HashMap<String, JMenu> categories = new HashMap<>();
        for (AutoPageType p : AutoPageType.getAll()) {  //for all Paper types
            
            JMenu m = categories.get(p.getCategory());  
            
            //If the category does not exist yet, create it
            if (m == null) {
                m = new JMenu(p.getCategory());
                categories.put(p.getCategory(), m);
                autoPaperMenu.add(m);
            }
                
            //Create the menu item for this Paper type
            JMenuItem mi = new JMenuItem(p.toString());
            
            //When selected, set the width and height spinners based on the the type dimensions, taking into account the orientation
            mi.addActionListener((ActionEvent e) -> {
                
                double w = unit.fromPFUnits(p.getWidth());
                double h = unit.fromPFUnits(p.getHeight());
                
                if (w > h) 
                    setFromOrientation(PageFormat.LANDSCAPE);       
                else
                    setFromOrientation(PageFormat.PORTRAIT); 

                widthSpinner.setValue(w);
                heightSpinner.setValue(h);
                measureUnitComboBox.setSelectedItem(p.getUnit());  //set the unit based on the type's unit
                    
            });
            mi.setToolTipText(p.getDimensionString());  //set the tooltip text to the type's dimension string
            
            //Add the new menu item to the Category menu
            m.add(mi);
        }
        
        
        //If the user doesn't supply an initial PageFormat, create one for the default printer
        if (fmt == null) 
            fmt = validateForPrinter(null, null);
        
        //Add a "Any Printer" 
        printerComboBox.addItem(new PrintServiceEntry(null));
        
        //Populate the combo box with available printers
        for (PrintService p : PrinterJob.lookupPrintServices())
            printerComboBox.addItem(new PrintServiceEntry(p));
        
        //Setup all fields based on the PageFormat
        initFromPageFormat(fmt);
        
        if (dialogIconImage != null)
            setIconImage(dialogIconImage);
        
        ActionMap am = sizePane.getActionMap();
        
        sizePane.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Escape");
        sizePane.getActionMap().put("Escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               dispose();
            }
        });
        
        setLocationRelativeTo(parent);

    }
    
    private void setFromOrientation(int orientation) {
        currentOrientation = orientation;
        switch (orientation) {
            case PageFormat.PORTRAIT:
                portraitRadioButton.setSelected(true);
                landscapeRadioButton.setSelected(false);
                revLandscapeRadioButton.setSelected(false);
                pageIconLabel.setIcon(portraitIcon);
                break;
            case PageFormat.LANDSCAPE:
                portraitRadioButton.setSelected(false);
                landscapeRadioButton.setSelected(true);
                revLandscapeRadioButton.setSelected(false);
                pageIconLabel.setIcon(landscapeIcon);
                break;
            case PageFormat.REVERSE_LANDSCAPE:
                portraitRadioButton.setSelected(false);
                landscapeRadioButton.setSelected(false);
                revLandscapeRadioButton.setSelected(true);
                pageIconLabel.setIcon(revLandscapeIcon);
                break;
            default:
                throw new RuntimeException("Unhandled format");
        }
    }
    
    /**
     * From the supplied PageFormat, set all the fields on the dialog
     * @param format the format to use
     */
    private void initFromPageFormat(PageFormat format) {
        
        setFromOrientation(format.getOrientation());
               
        widthSpinner.setValue(unit.fromPFUnits(format.getWidth()));
        heightSpinner.setValue(unit.fromPFUnits(format.getHeight()));
        leftMarginSpinner.setValue(unit.fromPFUnits(format.getImageableX()));
        topMarginSpinner.setValue(unit.fromPFUnits(format.getImageableY()));
        rightMarginSpinner.setValue(unit.fromPFUnits(format.getWidth() - (format.getImageableX() + format.getImageableWidth())));
        bottomMarginSpinner.setValue(unit.fromPFUnits(format.getHeight() - (format.getImageableY() + format.getImageableHeight())));
    }
    
    
    /**
     * From the fields on the dialog, create a PageFormat
     * @return the created PageFormat
     */
    private PageFormat getPageFormatFromValues() {
        
        int orientation;
        if (portraitRadioButton.isSelected())
            orientation = PageFormat.PORTRAIT;
        else if (landscapeRadioButton.isSelected())
            orientation = PageFormat.LANDSCAPE;
        else
            orientation = PageFormat.REVERSE_LANDSCAPE;
   
        //Get paper dimensions in PageFormat units
        double width = unit.toPFUnits((double)widthSpinner.getValue());        
        double height = unit.toPFUnits((double)heightSpinner.getValue());
   
        //Get all margin dimensions in PageFormat units
        double left = unit.toPFUnits((double)leftMarginSpinner.getValue());
        double top = unit.toPFUnits((double)topMarginSpinner.getValue());
        double right = unit.toPFUnits((double)rightMarginSpinner.getValue());
        double bottom = unit.toPFUnits((double)bottomMarginSpinner.getValue());


        PageFormat fmt = new PageFormat();
        fmt.setOrientation(orientation);
        
        Paper p = new Paper();
        
        switch (orientation) {
            case PageFormat.PORTRAIT:
                p.setSize(width, height);
                p.setImageableArea(left, top, width - (left + right), height - (top + bottom));
                break;
            case PageFormat.LANDSCAPE:
                p.setSize(height, width);
                p.setImageableArea(top, right, height - (top + bottom), width - (left + right));  //rotate counter-clockwise for Landscape
                break;
            case PageFormat.REVERSE_LANDSCAPE:
                p.setSize(height, width);
                p.setImageableArea(bottom, left, height - (top + bottom), width - (left + right));  //rotate clockwise for Rev Landscape             
                break;
            default:
                throw new RuntimeException("Unhandled format");       
        }
            
        fmt.setPaper(p);
        return fmt;
    }
    
    
    /**
     * Get the resultant PageFormat if the user pressed the OK button. 
     * @return the user-chosen PageFormat if the OK button was pressed, or null if the Cancel button was pressed
     */
    public PageFormat getDialogResult() {
        return returnFormat;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        sizePane = new javax.swing.JPanel();
        heightLabel = new javax.swing.JLabel();
        pageIconLabel = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        heightSpinner = new javax.swing.JSpinner();
        widthSpinner = new javax.swing.JSpinner();
        orientationPane = new javax.swing.JPanel();
        portraitRadioButton = new javax.swing.JRadioButton();
        landscapeRadioButton = new javax.swing.JRadioButton();
        revLandscapeRadioButton = new javax.swing.JRadioButton();
        marginsPane = new javax.swing.JPanel();
        leftMarginSpinner = new javax.swing.JSpinner();
        topMarginSpinner = new javax.swing.JSpinner();
        rightMarginSpinner = new javax.swing.JSpinner();
        bottomMarginSpinner = new javax.swing.JSpinner();
        leftLabel = new javax.swing.JLabel();
        topLabel = new javax.swing.JLabel();
        bottomLabel = new javax.swing.JLabel();
        rightLabel = new javax.swing.JLabel();
        upperPane = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        measureUnitComboBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        autoPaperSizeButton = new javax.swing.JButton();
        southPane = new javax.swing.JPanel();
        lowerPane = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        autoPanel = new javax.swing.JPanel();
        printerComboBox = new javax.swing.JComboBox<>();
        validateForPrinterButton = new javax.swing.JButton();
        defaultForPrinterButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Page Setup");
        setMinimumSize(new java.awt.Dimension(775, 480));
        setPreferredSize(new java.awt.Dimension(775, 480));
        setResizable(false);
        setSize(new java.awt.Dimension(775, 480));

        sizePane.setBorder(javax.swing.BorderFactory.createTitledBorder("Size"));
        java.awt.GridBagLayout entryPaneLayout = new java.awt.GridBagLayout();
        entryPaneLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        entryPaneLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0};
        sizePane.setLayout(entryPaneLayout);

        heightLabel.setText("Height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        sizePane.add(heightLabel, gridBagConstraints);

        pageIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/portrait_orientation.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        sizePane.add(pageIconLabel, gridBagConstraints);

        widthLabel.setText("Width");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        sizePane.add(widthLabel, gridBagConstraints);

        heightSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        heightSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        sizePane.add(heightSpinner, gridBagConstraints);

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        widthSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        sizePane.add(widthSpinner, gridBagConstraints);

        orientationPane.setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(portraitRadioButton);
        portraitRadioButton.setText("Portrait");
        portraitRadioButton.setToolTipText("Orientation with the short side at the top");
        portraitRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                portraitRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        orientationPane.add(portraitRadioButton, gridBagConstraints);

        buttonGroup.add(landscapeRadioButton);
        landscapeRadioButton.setText("Landscape");
        landscapeRadioButton.setToolTipText("Orientation where Portrait is rotated counter-clockwise (standard for Windows)");
        landscapeRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                landscapeRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        orientationPane.add(landscapeRadioButton, gridBagConstraints);

        buttonGroup.add(revLandscapeRadioButton);
        revLandscapeRadioButton.setText("Reverse Landscape");
        revLandscapeRadioButton.setToolTipText("Orientation where Portrait is rotated clockwise (standard for MacOS)");
        revLandscapeRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                revLandscapeRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        orientationPane.add(revLandscapeRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        sizePane.add(orientationPane, gridBagConstraints);

        marginsPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Margins"));
        java.awt.GridBagLayout marginsPaneLayout = new java.awt.GridBagLayout();
        marginsPaneLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        marginsPaneLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        marginsPane.setLayout(marginsPaneLayout);

        leftMarginSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        leftMarginSpinner.setToolTipText("Non-printable area at the left of the page");
        leftMarginSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        marginsPane.add(leftMarginSpinner, gridBagConstraints);

        topMarginSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        topMarginSpinner.setToolTipText("Non-printable area at the top of the page");
        topMarginSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        marginsPane.add(topMarginSpinner, gridBagConstraints);

        rightMarginSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        rightMarginSpinner.setToolTipText("Non-printable area at the right of the page");
        rightMarginSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        marginsPane.add(rightMarginSpinner, gridBagConstraints);

        bottomMarginSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 0.1d));
        bottomMarginSpinner.setToolTipText("Non-printable area at the bottom of the page");
        bottomMarginSpinner.setPreferredSize(new java.awt.Dimension(80, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        marginsPane.add(bottomMarginSpinner, gridBagConstraints);

        leftLabel.setText("Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        marginsPane.add(leftLabel, gridBagConstraints);

        topLabel.setText("Top");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        marginsPane.add(topLabel, gridBagConstraints);

        bottomLabel.setText("Bottom");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        marginsPane.add(bottomLabel, gridBagConstraints);

        rightLabel.setText("Right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        marginsPane.add(rightLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 0);
        sizePane.add(marginsPane, gridBagConstraints);

        java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
        jPanel2Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel2Layout.rowHeights = new int[] {0};
        upperPane.setLayout(jPanel2Layout);

        jLabel2.setText("Measurement Unit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        upperPane.add(jLabel2, gridBagConstraints);

        measureUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                measureUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        upperPane.add(measureUnitComboBox, gridBagConstraints);

        jLabel8.setText("Auto Paper Size");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        upperPane.add(jLabel8, gridBagConstraints);

        autoPaperSizeButton.setText("▼");
        autoPaperSizeButton.setToolTipText("Set the dimensions from a defined paper size");
        autoPaperSizeButton.setPreferredSize(new java.awt.Dimension(45, 29));
        autoPaperSizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                autoPaperSizeButtonMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 40);
        upperPane.add(autoPaperSizeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        sizePane.add(upperPane, gridBagConstraints);

        getContentPane().add(sizePane, java.awt.BorderLayout.CENTER);

        southPane.setLayout(new java.awt.BorderLayout());

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        lowerPane.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        lowerPane.add(cancelButton);

        southPane.add(lowerPane, java.awt.BorderLayout.PAGE_END);

        autoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Format for Printer"));
        java.awt.GridBagLayout autoPanelLayout = new java.awt.GridBagLayout();
        autoPanelLayout.columnWidths = new int[] {0, 5, 0};
        autoPanelLayout.rowHeights = new int[] {0};
        autoPanel.setLayout(autoPanelLayout);

        printerComboBox.setMaximumRowCount(15);
        printerComboBox.setToolTipText("List of available printers");
        printerComboBox.setPreferredSize(new java.awt.Dimension(340, 27));
        printerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printerComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 11, 0);
        autoPanel.add(printerComboBox, gridBagConstraints);

        validateForPrinterButton.setText("Validate");
        validateForPrinterButton.setToolTipText("Update the dimensions and margins to comply with selected printer limitations");
        validateForPrinterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateForPrinterButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        autoPanel.add(validateForPrinterButton, gridBagConstraints);

        defaultForPrinterButton.setText("Default");
        defaultForPrinterButton.setToolTipText("Set the format to the selected printer's default page with minimum margins");
        defaultForPrinterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultForPrinterButtonActionPerformed(evt);
            }
        });
        autoPanel.add(defaultForPrinterButton, new java.awt.GridBagConstraints());

        southPane.add(autoPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(southPane, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    /**
     * Validate the page dimensions and margins against the capabilities of the selected printer, adjusting as necessary
     * @param evt 
     */
    private void validateForPrinterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateForPrinterButtonActionPerformed
        
        PrintServiceEntry e = (PrintServiceEntry)printerComboBox.getSelectedItem();
        if (e.getPrintService() == null)  //Any printer - automatically valid
            return;

        PrinterJob job = PrinterJob.getPrinterJob();  //default printer job
        try {
            
            job.setPrintService(e.getPrintService());              //set job for selected printer         
            PageFormat validated_fmt = job.validatePage(getPageFormatFromValues());  //validate the PageFormat created from the fields
            initFromPageFormat(validated_fmt);  //set the fields on the Dialog from the validated format
            
        } catch (PrinterException ex) {  //Service cannot support
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE, errorIcon);
        }   

    }//GEN-LAST:event_validateForPrinterButtonActionPerformed

    /**
     * Flip from portrait to landscape or vice versa
     */
    private void flipOrientation() {
        double width = (double)widthSpinner.getValue();
        double height = (double)heightSpinner.getValue();
        widthSpinner.setValue(height);
        heightSpinner.setValue(width);
    }
    
            
    /**
     * Recursive function for each component on a JPanel, if component is a JSpinner, first convert to PF units using the old unit, 
     * then convert back to unit values using the new unit.  If component is a sub JPanel, call recursively.
     * @param parent the parent JPanel 
     * @param oldUnit the original unit
     * @param newUnit the new desired unit
     */
    private void updateJSpinnerForUnit(JPanel parent, PageMeasureUnit oldUnit, PageMeasureUnit newUnit) {
        
        for (Component c : parent.getComponents()) {
            if (c instanceof JSpinner) {
                
                JSpinner s = (JSpinner)c;
                double pfVal = oldUnit.toPFUnits((double)s.getValue());             
                
                s.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, newUnit.getIncrementSize()));
                s.setEditor(new JSpinner.NumberEditor(s, newUnit.getNumberFormat()));
                s.setValue(newUnit.fromPFUnits(pfVal));
                                                               
            }
            if (c instanceof JPanel)
                updateJSpinnerForUnit((JPanel)c, oldUnit, newUnit);
            
        }
        
    }
    
    private void measureUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_measureUnitComboBoxItemStateChanged

        PageMeasureUnit newUnit = (PageMeasureUnit)measureUnitComboBox.getSelectedItem(); 
        updateJSpinnerForUnit(sizePane, unit, newUnit);  //update all fields for the new unit
        unit = newUnit;  //set to new unit
    }//GEN-LAST:event_measureUnitComboBoxItemStateChanged

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
                
        PageFormat fmt = getPageFormatFromValues();
                       
        PrintServiceEntry e = (PrintServiceEntry)printerComboBox.getSelectedItem();

        try {

            if (e.getPrintService() != null) {  //check dimensions against selected printer
            
                PrinterJob job = PrinterJob.getPrinterJob();        //default printer job         
                job.setPrintService(e.getPrintService());          //set job for selected printer         
                PageFormat validated_fmt = job.validatePage(fmt);

                //See if validation changed anything          
                if (validated_fmt.getOrientation() != fmt.getOrientation() ||
                    !compareDimensions(validated_fmt.getWidth(), fmt.getWidth()) ||
                    !compareDimensions(validated_fmt.getHeight(), fmt.getHeight())) {

                    throw new PrinterException("Paper dimensions are outside of the range suitable for the printer \"" + e.toString() + "\"");
                }

                if (!compareDimensions(validated_fmt.getImageableX(), fmt.getImageableX()) ||
                    !compareDimensions(validated_fmt.getImageableY(), fmt.getImageableY()) ||
                    !compareDimensions(validated_fmt.getImageableWidth(), fmt.getImageableWidth()) ||
                    !compareDimensions(validated_fmt.getImageableHeight(), fmt.getImageableHeight())) {

                    throw new PrinterException("Margins are outside of the printable area for the printer \"" + e.toString() + "\"");
                }
            }
            
            if (fmt.getImageableWidth() <= 0 || fmt.getImageableHeight() <= 0)
                throw new PrinterException("Margins are too large, no remaining printable area");
            
            
        } catch (PrinterException ex) {  //Service cannot support, close dialog, set exception
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE, errorIcon);
            return;
        }
        
        returnFormat = fmt;
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Set the PageFormat from the default PageFormat for the selected printer
     * @param evt 
     */
    private void defaultForPrinterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultForPrinterButtonActionPerformed
        PrintServiceEntry e = (PrintServiceEntry)printerComboBox.getSelectedItem();
        
        PageFormat fmt = validateForPrinter(e.getPrintService(), null);
        initFromPageFormat(fmt);
  
    }//GEN-LAST:event_defaultForPrinterButtonActionPerformed

    /**
     * Show the popup menu at the mouse when the button is pressed
     * @param evt 
     */
    private void autoPaperSizeButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_autoPaperSizeButtonMousePressed
        autoPaperMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_autoPaperSizeButtonMousePressed

    private void printerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printerComboBoxActionPerformed
        PrintServiceEntry e = (PrintServiceEntry)printerComboBox.getSelectedItem();
        
        validateForPrinterButton.setEnabled(!(e.getPrintService() == null));
        defaultForPrinterButton.setEnabled(!(e.getPrintService() == null));

                
    }//GEN-LAST:event_printerComboBoxActionPerformed

    private void landscapeRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_landscapeRadioButtonItemStateChanged
        if (landscapeRadioButton.isSelected() && currentOrientation != PageFormat.LANDSCAPE) {
            pageIconLabel.setIcon(landscapeIcon);
            if (currentOrientation == PageFormat.PORTRAIT)
                flipOrientation();
            
            currentOrientation = PageFormat.LANDSCAPE;
        }
    }//GEN-LAST:event_landscapeRadioButtonItemStateChanged

    private void portraitRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_portraitRadioButtonItemStateChanged
        if (portraitRadioButton.isSelected() && currentOrientation != PageFormat.PORTRAIT) {
            pageIconLabel.setIcon(portraitIcon);
            if (currentOrientation != PageFormat.PORTRAIT)
                flipOrientation();
            
            currentOrientation = PageFormat.PORTRAIT;
        }
    }//GEN-LAST:event_portraitRadioButtonItemStateChanged

    private void revLandscapeRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_revLandscapeRadioButtonItemStateChanged
        if (revLandscapeRadioButton.isSelected() && currentOrientation != PageFormat.REVERSE_LANDSCAPE) {
            pageIconLabel.setIcon(revLandscapeIcon);
            if (currentOrientation == PageFormat.PORTRAIT)
                flipOrientation();
            
            currentOrientation = PageFormat.REVERSE_LANDSCAPE;
        }
    }//GEN-LAST:event_revLandscapeRadioButtonItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel autoPanel;
    private javax.swing.JButton autoPaperSizeButton;
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JSpinner bottomMarginSpinner;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton defaultForPrinterButton;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JRadioButton landscapeRadioButton;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JSpinner leftMarginSpinner;
    private javax.swing.JPanel lowerPane;
    private javax.swing.JPanel marginsPane;
    private javax.swing.JComboBox<PageMeasureUnit> measureUnitComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel orientationPane;
    private javax.swing.JLabel pageIconLabel;
    private javax.swing.JRadioButton portraitRadioButton;
    private javax.swing.JComboBox<PrintServiceEntry> printerComboBox;
    private javax.swing.JRadioButton revLandscapeRadioButton;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JSpinner rightMarginSpinner;
    private javax.swing.JPanel sizePane;
    private javax.swing.JPanel southPane;
    private javax.swing.JLabel topLabel;
    private javax.swing.JSpinner topMarginSpinner;
    private javax.swing.JPanel upperPane;
    private javax.swing.JButton validateForPrinterButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
}
