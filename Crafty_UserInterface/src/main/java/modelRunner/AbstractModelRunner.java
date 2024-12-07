package modelRunner;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModelRunner {

	List<ModelState> stateManager = new ArrayList<>();

	public AbstractModelRunner() {
		loadStateManager();
		setup();
		toSchedule();
	}

	public abstract void setup();

	public abstract void toSchedule();

	public abstract void loadStateManager();

	public List<ModelState> getStateManager() {
		return stateManager;
	}
}
