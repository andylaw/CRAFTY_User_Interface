package TabsPane;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SelectSenario {

	public TitledPane select(  ) throws IOException {
		
		Text message = new Text();
		message.setFill(Color.BLUE);
		ArrayList<String> list=ReadFile.findFolder( new File(Path.projectPath),".xml",false);
		ChoiceBox<String> choiceSenaio = new ChoiceBox<>();//
		choiceSenaio.getItems().addAll(list);
		choiceSenaio.setValue("Select a Senaio File");
		choiceSenaio.setOnAction(e -> {
			if (null != choiceSenaio.getValue())
				try {
					ReadFile.CopyPast(Path.projectPath + choiceSenaio.getValue() + ".xml");
					message.setText(choiceSenaio.getValue() + " is selected");
				//	creatSenario.setExpanded(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		});

		return Tools.T("Select a Senario : ", true, Tools.vBox( choiceSenaio, message));
	}
}
