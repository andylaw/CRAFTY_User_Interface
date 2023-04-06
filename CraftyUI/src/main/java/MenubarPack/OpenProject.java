package MenubarPack;

import java.io.File;

import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;
import  Main.Main_CraftyFx;
import javafx.stage.DirectoryChooser;

public class OpenProject {
	
	
	static void openProject() {
		DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Project");
        File initialDirectory = new File("C:\\Users\\byari-m\\Documents\\Data");
        chooser.setInitialDirectory(initialDirectory);
        File selectedDirectory = chooser.showDialog(Main_CraftyFx.primaryStage);
        if (selectedDirectory != null) {
        	Path.setProjectPath(selectedDirectory.getAbsolutePath());
        	Path.AllfilePatheInData = Path.pathWithconditions();
        	String[]  tmp= CsvTools.columnFromscsv(0, Path.fileFilter("\\Services.csv").get(0) );
        	for (int i = 1; i < tmp.length; i++) {
        		Path.servicesList.add(tmp[i]);
			}
        
        }
        
        
      
        
        
        
        
	}

}
