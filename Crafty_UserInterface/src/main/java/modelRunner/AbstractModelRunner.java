package modelRunner;

import java.util.ArrayList;
import java.util.List;

import model.ModelState;

public abstract class AbstractModelRunner {

	List<ModelState> stateManager = new ArrayList<>();

	public void start() {
		loadStateManager();
		setup(this);
		toSchedule();
	}

	public abstract void setup(AbstractModelRunner abstractModelRunner);

	public abstract void toSchedule();

	public abstract void loadStateManager();

	public List<ModelState> getStateManager() {
		return stateManager;
	}
}
