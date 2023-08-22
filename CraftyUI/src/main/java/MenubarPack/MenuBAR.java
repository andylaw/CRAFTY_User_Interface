package MenubarPack;

import java.io.IOException;

//import org.volante.abm.serialization.MultiRunsByari;

import TabsPane.NewWindow;
import TabsPane.OpenTabs;
import TabsPane.OutPutter;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MenuBAR extends MenuBar{
	public MenuBAR ( ImageView imageView) {
		
		Menu fileMenu = new Menu("File");
		
	    // Create menu items for the File menu
		MenuItem NewProject =  new MenuItem("New Project ");
		NewProject.setAccelerator(KeyCombination.keyCombination("Ctrl+Alt+N"));
	    MenuItem openProject =  new MenuItem("Open Projects From File Systeme...");
	    openProject.setAccelerator(KeyCombination.keyCombination("Ctrl+Alt+O"));
	    MenuItem saveProject = new MenuItem("Save Project");
	    saveProject.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
	    MenuItem Refresh = new MenuItem("Refresh");
	    MenuItem Resrart = new MenuItem("Resrart");
	    MenuItem Exit = new MenuItem("Exit");
	    fileMenu.getItems().addAll(openProject, saveProject, new SeparatorMenuItem(), Refresh,Resrart, Exit);
	    Menu Help = new Menu("Help");
	    MenuItem welcom = new MenuItem("Welcom");
	    MenuItem Uml = new MenuItem("CRAFTY UML diagram");
	    Help.getItems().addAll(welcom, Uml);
	    // MenuItem NewProject =  new MenuItem("New Project ");
	    
	   
	    Menu OutPUT = new Menu("OutPut");
	    MenuItem OutPutConfiguration = new MenuItem("OutPut Configuration");
	    OutPUT.getItems().addAll(OutPutConfiguration);
	    Exit.setOnAction(e->{
	    	 Platform.exit();
	    });
	    
	    openProject.setOnAction(e->{
	    	
	    	 OpenProject.openProject();
	    	 try {
				new OpenTabs();
				imageView.setVisible(false);
				
			} catch (IOException e1) {}
	    });
	    OutPutConfiguration.setOnAction(e->{
	    	OutPutter outPut = new OutPutter();
	    	 try {
				new NewWindow().creatwindows("OutPut Configuration",outPut.paneOutPut());
			} catch (IOException e1) {}
	    	   }); 
	    
	    welcom.setOnAction(e->{
	    	 welcomInfo();
	   // 	System.out.println(new ModelRunner().tstNbr);
	    	 
	    	   }); 
	    getMenus().addAll(fileMenu,OutPUT,Help);
	}

	
	
	
	

	void welcomInfo() {
		WebView webView = new WebView();
        // get the WebEngine object from the WebView
        WebEngine webEngine = webView.getEngine();

        // specify the URL of the website you want to open
        String url = "https://landchange.imk-ifu.kit.edu/CRAFTY";

        // load the specified URL in the WebEngine
        webEngine.load(url);
       
        new NewWindow().creatwindows("Welcome",0.4,0.8,webView);
        
	
	}
	
}
