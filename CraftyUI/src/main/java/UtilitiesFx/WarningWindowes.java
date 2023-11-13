package UtilitiesFx;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class WarningWindowes {
	
	
	static public void showWarningMessage(String message,String okbuttonName,Consumer<String> Retry,String cancelbuttonName,Consumer<String> continuAnyWay) {
		showWarningMessage(message, okbuttonName, Retry, cancelbuttonName,continuAnyWay,new ArrayList<String>()) ;
	}
	
	
	static public void showWarningMessage(String message,String okbuttonName,Consumer<String> okbuttonConsumer,
			String cancelbuttonName,Consumer<String> continuAnyWay,List<String>listWarning) {
		ButtonType okButtonType = new ButtonType(okbuttonName, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(cancelbuttonName, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType customButtonType = new ButtonType("Close CRAFTY");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText(message);
        String str="";
        for (int i = 0; i < listWarning.size(); i++) {
			str=str+listWarning.get(i)+"\n";
		}
        alert.setContentText(str);

        // Add custom buttons
        alert.getButtonTypes().setAll(okButtonType, cancelButtonType, customButtonType);

        // Handle button actions
        alert.setOnCloseRequest(event -> {
            ButtonType result = alert.getResult();
            if (result == okButtonType) {
            	okbuttonConsumer.accept("");
            } else if (result == cancelButtonType) {
            	continuAnyWay.accept("");
            } else if (result == customButtonType) {
            	 Platform.exit();
            }
        });

        alert.showAndWait();
    }

}
