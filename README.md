# JPageSetup - A Better Printing PageSetup Dialog for Java Swing. 

The typical Java printing workflow provides two dialogs, one to set up the page format (returned in a PageFormat object) 
and another to select the Printer settings. The former uses PrinterJob.pageDialog(), which is limited in its use. Specifically the
Windows version doesn't provide custom sizes for any printer, the Mac version has no margin specifications for standard sizes, and
both show limited number of paper types.

This library is an improved, fully native Java version that addresses the limitations and is also extensible.

![Demo Screenshot](https://github.com/kkieffer/jPageSetup/blob/master/PageSetupScreenshot.jpg "Demo Screenshot")


## Features

* The user can select from a variety of Paper Sizes. There are 100 built in paper sizes plus custom sizes can be added easily.
* Unit measurements can be displayed in inches, millimeters, or points. The default unit is chosen based on the user's locale or paper selection.
* Portrait or landscape modes can be selected as well as margins for the printable area.
* When instantiated with an existing PageFormat object, the panel displays those settings.
* The settings can be validated against a selected printer.
* You can set your own application icons in the dialog


## How to Use

It's quite simple to request a new PageFormat from the user. Call the dialog from the Swing thread, and the result will be returned 
in a PageFormat object. If the user cancels, the object is null.

```Java
            
PageSetupDialog diag = new PageSetupDialog(null, true, null, null, null);
diag.setVisible(true);
            
PageFormat r = diag.getDialogResult();
```

You can also add new Page types easily, prior to calling the dialog:
       
```Java
AutoPageType.addType("Test Category", "My Test Type", 100, 150, PageMeasureUnit.PT);  //add a Paper format of 100x150 points
```
      
![Demo Screenshot](https://github.com/kkieffer/jPageSetup/blob/master/PageSetupScreenshot2.jpg "Demo Screenshot 2")


## Building

Build and Run using Maven:  "mvn package"
Navigate to the "target" directory

* Run: java -cp classes:test-classes com.kevinnovate.jpagesetup.PageSetupExample

Only the Java JRE 1.8 is required.  No other dependencies are needed.


## License

This project is licensed under the Apache License - see the [LICENSE.md](LICENSE.md) file for details
The icon is a modified version of the Creative Commons licensed icon [here](https://commons.wikimedia.org/wiki/File:Module_doc_page_icon.svg).
