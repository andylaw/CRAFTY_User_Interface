package MenubarPack;

import java.io.File;
import java.util.List;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.WarningWindowes;
import javafx.application.Platform;

public class OpenProject {
	


	static void openProject() {
		File selectedDirectory =ReadFile.selecFolder("C:\\Users\\byari-m\\Documents\\Data")  ;
		if (selectedDirectory != null) {
			List<String> folderMissig = Path.checkfolders(selectedDirectory.getAbsolutePath());
			boolean ispathcorrect = true;
			if (folderMissig.size() > 0) {
				ispathcorrect = false;
				WarningWindowes.showWarningMessage("Folders Missing",
						"Try Again",x -> {
							openProject();
						},
						"Exit",x->{
							// Present only data visualisation and agents configuration 
							// Close interaction with Cobra
							Platform.exit();
						},
						 folderMissig);
			}
			if (ispathcorrect) {
				Path.initialisation(selectedDirectory.getAbsolutePath());
			}

		}

	}

}
