
package com.kevinnovate.jpagesetup;

import java.awt.print.PageFormat;
import javax.swing.SwingUtilities;

/**
 *
 * @author kevin
 */
public class PageSetupExample {
    
    static { //Add a test type before dialog is called
        AutoPageType.addType("Test Category", "My Test Type", 100, 150, PageMeasureUnit.PT);
    }
    
    
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            PageSetupDialog diag = new PageSetupDialog(null, true, null, null, null);
            diag.setVisible(true);
            
            PageFormat r = diag.getDialogResult();
            if (r == null)
                System.out.println("User cancelled");
            else {
             
                
                System.out.println("User selected paper: " + r.getImageableWidth() + " x " + r.getImageableHeight());
            }
            
        });
        
    }
    
    
}
