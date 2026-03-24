package godclassinspector.ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.model.ClassDTO;
import net.sourceforge.plantuml.SourceStringReader;

public class UmlDiagramUI extends ViewPart {

    private Canvas canvas;
    private Image diagramImage;

    @Override
    public void createPartControl(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);

        canvas.addPaintListener(e -> {
            if (diagramImage != null) {
                e.gc.drawImage(diagramImage, 0, 0);
            } else {
                e.gc.drawString("No diagram generated yet. Run analysis first.", 10, 10);
            }
        });
    }
    
    public void generateUML(List<ClassDTO> classList) {
        if (classList == null || classList.isEmpty()) return;

        // 1. Build the PlantUML Source String
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("!pragma layout smetana\n"); // Use internal layout engine
        sb.append("skinparam classAttributeIconSize 0\n"); // Use +/- symbols

        for (ClassDTO dto : classList) {
            String type = dto.isInterface() ? "interface " : (dto.isAbstract() ? "abstract class " : "class ");
            String stereotype = dto.isGodClass() ? " <<GodClass>> #Pink" : "";
            
            sb.append(type).append(dto.getClassName()).append(stereotype).append(" {\n");
            
            // Add Fields
            if (dto.getFields() != null) {
                for (String field : dto.getFields()) sb.append("  ").append(field).append("\n");
            }
            sb.append("  --\n");
            // Add Methods
            if (dto.getMethods() != null) {
                for (String method : dto.getMethods()) sb.append("  ").append(method).append("\n");
            }
            sb.append("}\n");
        }
        sb.append("@enduml");

        // 2. Generate the Image using PlantUML
        SourceStringReader reader = new SourceStringReader(sb.toString());
        try (java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream()) {
            reader.outputImage(os);
            byte[] imageBytes = os.toByteArray();

            // 3. Update the SWT UI thread
            org.eclipse.swt.widgets.Display.getDefault().asyncExec(() -> {
                if (diagramImage != null) diagramImage.dispose();
                
                // Create the SWT Image from the byte array
                diagramImage = new Image(canvas.getDisplay(), new java.io.ByteArrayInputStream(imageBytes));
                
                // Redraw the canvas to show the new diagram
                canvas.redraw();
            });
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setFocus() {
        canvas.setFocus();
    }

    @Override
    public void dispose() {
        if (diagramImage != null) {
			diagramImage.dispose();
		}
        super.dispose();
    }
}