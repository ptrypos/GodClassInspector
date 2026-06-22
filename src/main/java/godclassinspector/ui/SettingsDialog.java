package godclassinspector.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import godclassinspector.models.MetricsThresholdDTO;

public class SettingsDialog extends TitleAreaDialog {

    private Text thresholdWMC;
    private Text thresholdATFD;
    private Text thresholdTCC;

    public SettingsDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Detection Settings");
        setMessage("Configure the thresholds for God Class detection.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("WMC Threshold:");
        thresholdWMC = new Text(container, SWT.BORDER);
        thresholdWMC.setText(Integer.toString(MetricsThresholdDTO.getWmcThreshold()));
        thresholdWMC.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("ATFD Threshold:");
        thresholdATFD = new Text(container, SWT.BORDER);
        thresholdATFD.setText(Integer.toString(MetricsThresholdDTO.getAtfdThreshold()));
        thresholdATFD.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("TCC Threshold:");
        thresholdTCC = new Text(container, SWT.BORDER);
        thresholdTCC.setText(Double.toString(MetricsThresholdDTO.getTccThreshold()));
        thresholdTCC.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return area;
    }

    @Override
    protected void okPressed() {
        MetricsThresholdDTO.setWmcThreshold(Integer.parseInt(thresholdWMC.getText()));
        MetricsThresholdDTO.setAtfdThreshold(Integer.parseInt(thresholdATFD.getText()));
        MetricsThresholdDTO.setTccThreshold(Double.parseDouble(thresholdTCC.getText()));
        super.okPressed();
    }
}