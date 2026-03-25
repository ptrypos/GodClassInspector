package godclassinspector.ui;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import godclassinspector.model.ClassDTO;
import godclassinspector.uml.PlantUmlBuilder;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class UmlDiagramUI extends ViewPart {

    private Browser browser;
    private final PlantUmlBuilder umlBuilder = new PlantUmlBuilder()
            .dpi(96)
            .nodesep(60)
            .ranksep(80)
            .margin(30)
            .padding(10);

    @Override
    public void createPartControl(Composite parent) {
        // Browser renders SVG natively — no raster buffer limit, no clipping.
        browser = new Browser(parent, SWT.NONE);
        browser.setText(idlePage());
    }

    public void generateUML(List<ClassDTO> classList) {
        String source = umlBuilder.build(classList);
        if (source == null || source.isEmpty()) return;

        new Thread(() -> {
            try {
                String svg  = renderSvg(source);
                String html = wrapSvgInHtml(svg);

                browser.getDisplay().asyncExec(() -> {
                    if (!browser.isDisposed()) browser.setText(html);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                browser.getDisplay().asyncExec(() -> {
                    if (!browser.isDisposed()) browser.setText(errorPage(ex.getMessage()));
                });
            }
        }, "plantuml-render").start();
    }

    // ── SVG rendering ─────────────────────────────────────────────────────────

    private static String renderSvg(String plantUmlSource) throws Exception {
        SourceStringReader reader = new SourceStringReader(plantUmlSource);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // SVG is vector — no pixel buffer, no clipping, no DPI limit.
            reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
            return os.toString(StandardCharsets.UTF_8.name());
        }
    }

    // ── HTML wrapper ──────────────────────────────────────────────────────────

    /**
     * Embeds the raw SVG in an HTML page with:
     *  - Fit-to-window on load
     *  - Mouse-wheel zoom (cursor-centred)
     *  - Click-drag panning
     */
    private static String wrapSvgInHtml(String svg) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'/><style>\n"
             + "  * { margin:0; padding:0; box-sizing:border-box; }\n"
             + "  html, body { width:100%; height:100%; overflow:hidden; background:#f5f5f5; }\n"
             + "  #viewport  { width:100%; height:100%; overflow:hidden; cursor:grab; }\n"
             + "  #viewport.dragging { cursor:grabbing; }\n"
             + "  #diagram   { display:inline-block; transform-origin:0 0; user-select:none; }\n"
             + "  #diagram svg { display:block; }\n"
             + "</style></head><body>\n"
             + "<div id='viewport'><div id='diagram'>" + svg + "</div></div>\n"
             + "<script>\n"
             + "  const vp   = document.getElementById('viewport');\n"
             + "  const diag = document.getElementById('diagram');\n"
             + "  let scale = 1, tx = 0, ty = 0;\n"
             + "  let dragging = false, startX = 0, startY = 0, startTx = 0, startTy = 0;\n"
             + "\n"
             + "  function applyTransform() {\n"
             + "    diag.style.transform = 'translate('+tx+'px,'+ty+'px) scale('+scale+')';\n"
             + "  }\n"
             + "\n"
             + "  // Fit diagram to viewport on load\n"
             + "  window.addEventListener('load', () => {\n"
             + "    const dw = diag.offsetWidth,  dh = diag.offsetHeight;\n"
             + "    const vw = vp.offsetWidth,    vh = vp.offsetHeight;\n"
             + "    scale = Math.min(vw / dw, vh / dh, 1);\n"
             + "    tx = (vw - dw * scale) / 2;\n"
             + "    ty = (vh - dh * scale) / 2;\n"
             + "    applyTransform();\n"
             + "  });\n"
             + "\n"
             + "  // Zoom on wheel (cursor-centred)\n"
             + "  vp.addEventListener('wheel', e => {\n"
             + "    e.preventDefault();\n"
             + "    const factor = e.deltaY < 0 ? 1.1 : 1/1.1;\n"
             + "    const rect   = vp.getBoundingClientRect();\n"
             + "    const mx = e.clientX - rect.left;\n"
             + "    const my = e.clientY - rect.top;\n"
             + "    tx = mx - (mx - tx) * factor;\n"
             + "    ty = my - (my - ty) * factor;\n"
             + "    scale = Math.min(Math.max(scale * factor, 0.05), 20);\n"
             + "    applyTransform();\n"
             + "  }, { passive: false });\n"
             + "\n"
             + "  // Drag to pan\n"
             + "  vp.addEventListener('mousedown', e => {\n"
             + "    if (e.button !== 0) return;\n"
             + "    dragging = true; startX = e.clientX; startY = e.clientY;\n"
             + "    startTx = tx; startTy = ty;\n"
             + "    vp.classList.add('dragging');\n"
             + "  });\n"
             + "  window.addEventListener('mousemove', e => {\n"
             + "    if (!dragging) return;\n"
             + "    tx = startTx + (e.clientX - startX);\n"
             + "    ty = startTy + (e.clientY - startY);\n"
             + "    applyTransform();\n"
             + "  });\n"
             + "  window.addEventListener('mouseup', () => {\n"
             + "    dragging = false;\n"
             + "    vp.classList.remove('dragging');\n"
             + "  });\n"
             + "</script></body></html>";
    }

    // ── Placeholder pages ─────────────────────────────────────────────────────

    private static String idlePage() {
        return "<html><body style='font-family:sans-serif;color:#888;"
             + "display:flex;align-items:center;justify-content:center;height:100vh;margin:0'>"
             + "<p>No UML generated. Run analysis to begin.</p></body></html>";
    }

    private static String errorPage(String msg) {
        return "<html><body style='font-family:sans-serif;color:#c00;padding:20px'>"
             + "<b>UML render error:</b><pre>"
             + (msg == null ? "unknown" : msg.replace("<", "&lt;"))
             + "</pre></body></html>";
    }

    // ── ViewPart lifecycle ────────────────────────────────────────────────────

    @Override
    public void setFocus() {
        if (browser != null && !browser.isDisposed()) browser.setFocus();
    }

    @Override
    public void dispose() {
        super.dispose(); // Browser disposes itself
    }
}