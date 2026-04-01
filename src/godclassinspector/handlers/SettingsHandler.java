package godclassinspector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import godclassinspector.ui.SettingsDialog;

public class SettingsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    Shell shell = HandlerUtil.getActiveShell(event);
	    SettingsDialog dialog = new SettingsDialog(shell);
	    dialog.open();
	    return null;
	}
}
