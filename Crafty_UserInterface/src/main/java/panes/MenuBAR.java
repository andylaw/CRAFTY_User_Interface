package panes;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.WarningWindowes;
import dataLoader.Paths;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import main.FxMain;
import main.OpenTabs;

public class MenuBAR extends MenuBar {
	public MenuBAR() {

		Menu fileMenu = new Menu("File");	
		MenuItem openProject = new MenuItem("Open Projects From File Systeme...");
		openProject.setAccelerator(KeyCombination.keyCombination("Ctrl+Alt+O"));
		MenuItem Resrart = new MenuItem("Resrart");
		MenuItem Exit = new MenuItem("Exit");
		Menu recent = new Menu("Recent Projects");
		fileMenu.getItems().addAll(openProject, recent,new SeparatorMenuItem(), Resrart, Exit);
		Menu view = new Menu("View");
		MenuItem cameraView = new MenuItem("Default Map View");
		MenuItem colorPallet = new MenuItem("Map Color Palettes");
		view.getItems().addAll(cameraView, colorPallet);
		Menu Help = new Menu("Help");
		MenuItem welcom = new MenuItem("CRFATY Website");
		Help.getItems().addAll(welcom);
		

		Exit.setOnAction(e -> {
			Platform.exit();
		});

		Resrart.setOnAction(e -> {
			restartApplication();
		});

		openProject.setOnAction(e -> {
			openProject();
			if (!Paths.getProjectPath().equals("")) {
				new OpenTabs();
			}
		});

		welcom.setOnAction(e -> {
			openWebInBrowser();
		});
		cameraView.setOnAction(e -> {
			FxMain.camera.cameraFocusBox();
		});
		colorPallet.setOnAction(e -> {
			NewWindow winColor = new NewWindow();
			ColorsTools.windowzpalette(winColor);
		});
		updateRecentFilesMenu( recent);

		getMenus().addAll(fileMenu, view, Help);
	}

	@SuppressWarnings("deprecation")
	private void restartApplication() {
		StringBuilder cmd = new StringBuilder();
		cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
		for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			cmd.append(jvmArg + " ");
		}
		cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
		cmd.append(FxMain.class.getName()).append(" ");

		try {
			Runtime.getRuntime().exec(cmd.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	void openWebInBrowser() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

			try {
				Desktop.getDesktop().browse(new URI("https://landchange.imk-ifu.kit.edu/CRAFTY"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}

		}
	}


	private void updateRecentFilesMenu(Menu recentMenu) {
		recentMenu.getItems().clear();
		String[] paths = PathTools.read("RecentProject.txt").split("\n");
		for (int i =paths.length-1;i>=0;i--) {
			if(!paths[i].equals("")) {
			MenuItem item = new MenuItem(paths[i]);
			int j=i;
			item.setOnAction(event -> {
				Paths.initialisation(paths[j]);
				new OpenTabs();
			});
			recentMenu.getItems().add(item);}
		}
	}
	
	static void openProject() {
		File selectedDirectory = PathTools.selecFolder("C:\\Users\\byari-m\\Documents\\Data");

		if (selectedDirectory != null) {
			if (selectedDirectory != null) {
				List<String> folderMissig = Paths.checkfolders(selectedDirectory.getAbsolutePath());
				boolean ispathcorrect = true;
				if (folderMissig.size() > 0) {
					ispathcorrect = false;
					WarningWindowes.showWarningMessage("Folders Missing", "Try Again", x -> {
						openProject();
					}, "Exit", x -> {
						Platform.exit();
					}, folderMissig);
				}
				if (ispathcorrect) {
					Paths.initialisation(selectedDirectory.getAbsolutePath());
				}
			}
		}
	}

}
