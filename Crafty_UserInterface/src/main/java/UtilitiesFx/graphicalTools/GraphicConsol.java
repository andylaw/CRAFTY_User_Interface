package UtilitiesFx.graphicalTools;

import java.io.OutputStream;
import java.io.PrintStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class GraphicConsol {


	public static TextArea start() {
		TextArea console = new TextArea();
		console.setEditable(false);
		// Set an uncaught exception handler for the JavaFX Application Thread
		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
			appendText(console, "Exception on " + thread.getName() + ": " + throwable + "\n");
		});


		// Redirect output streams to the console
		ConsoleOutputCapturer.captureOutput(console);
		return console;


	}

	private static void appendText(TextArea textArea, String text) {
		Platform.runLater(() -> textArea.appendText(text));
	}

}

class ConsoleOutputCapturer {
	public static void captureOutput(TextArea console) {
		OutputStream consoleOut = new Console(console);
		System.setOut(new PrintStream(consoleOut, true));
		System.setErr(new PrintStream(consoleOut, true));
	}
}

class Console extends OutputStream {
	private final TextArea output;

	public Console(TextArea ta) {
		this.output = ta;
	}

	@Override
	public void write(int i) {
		Platform.runLater(() -> output.appendText(String.valueOf((char) i)));
	}

	@Override
	    public void write(byte[] b, int off, int len) {
	        Platform.runLater(() -> output.appendText(new String(b, off, len)));
	    }
}
