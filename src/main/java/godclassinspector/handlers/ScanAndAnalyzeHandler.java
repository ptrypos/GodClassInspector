package godclassinspector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ScanAndAnalyzeHandler extends AbstractHandler {

	private final DiscoverHandler discoverHandler = new DiscoverHandler();
	private final AnalyzeHandler analyzeHandler = new AnalyzeHandler();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		discoverHandler.execute(event);
		analyzeHandler.execute(event);
		return null;
	}
}