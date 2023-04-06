package UtilitiesFx;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
public class MouseLeftPressed {
	
	public static 	void pressed(Node node) {
		
		node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (e.isSecondaryButtonDown()) {
				eventfilter( node);
							}
		});
	}
	
	
	public static  void eventfilter(Node node) {
		

		ContextMenu cm = new ContextMenu();
		MenuItem creatWindos = new MenuItem("Access  information");
		MenuItem printInfo = new MenuItem("Print Info");
		MenuItem SelectArea = new MenuItem("Select area ");
		cm.getItems().addAll(creatWindos, printInfo,SelectArea);

		node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (cm.isShowing()) {
				cm.hide();
			}
			if (e.isSecondaryButtonDown()) {
				cm.show((Stage) node.getScene().getWindow(), e.getScreenX(), e.getScreenY());
				e.consume();
			}
		});

		creatWindos.setOnAction(e -> {
			
		});

		printInfo.setOnAction(e -> {
			System.out.println("test");
		});
		
	}
	

	
	public static void smartMenu(Node node,HashMap<String, Consumer<String>> hash) {
		ContextMenu cm = new ContextMenu();
		
		MenuItem [] item= new MenuItem[hash.size()];
		AtomicInteger i = new AtomicInteger();
		hash.forEach((k,v)->{
			 item[i.get()] = new MenuItem(k);
			 cm.getItems().add( item[i.get()] );
			 item[i.get()] .setOnAction(e -> {
					v.accept(k);
				});
			 i.getAndIncrement();
		});
		
		node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (cm.isShowing()) {
				cm.hide();
			}
			if (e.isSecondaryButtonDown()) {
				cm.show((Stage) node.getScene().getWindow(), e.getScreenX(), e.getScreenY());
				e.consume();
			}
		});
    }

}
