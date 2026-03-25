package godclassinspector.uml;

import godclassinspector.model.ClassDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a PlantUML class diagram source string from a list of ClassDTOs.
 *
 * Layout order:
 *   1. Interfaces  (top)
 *   2. Abstract classes
 *   3. Regular classes
 *   4. God classes  (bottom, visually separated by a note/divider)
 *
 * Arrows are always orthogonal (no curves).
 */
public class PlantUmlBuilder {

    // ── Configuration ─────────────────────────────────────────────────────────

    private int dpi         = 96;
    private int nodesep     = 60;
    private int ranksep     = 80;
    private int margin      = 30;
    private int padding     = 10;
    private boolean leftToRight = false;

    // ── Fluent setters ────────────────────────────────────────────────────────

    public PlantUmlBuilder dpi(int dpi)               { this.dpi = dpi;           return this; }
    public PlantUmlBuilder nodesep(int n)             { this.nodesep = n;         return this; }
    public PlantUmlBuilder ranksep(int r)             { this.ranksep = r;         return this; }
    public PlantUmlBuilder margin(int m)              { this.margin = m;          return this; }
    public PlantUmlBuilder padding(int p)             { this.padding = p;         return this; }
    public PlantUmlBuilder leftToRight(boolean b)     { this.leftToRight = b;     return this; }

    // ── Main entry point ──────────────────────────────────────────────────────

    public String build(List<ClassDTO> classList) {
        if (classList == null || classList.isEmpty()) return "";

        // ── Split into ordered buckets ────────────────────────────────────────
        // PlantUML renders classes roughly in declaration order within each
        // rank, so declaring interfaces first pushes them to the top rank,
        // and declaring god classes last pushes them to the bottom rank.
        List<ClassDTO> interfaces   = filter(classList, true,  false, false);
        List<ClassDTO> abstracts    = filter(classList, false, true,  false);
        List<ClassDTO> regular      = filter(classList, false, false, false);
        List<ClassDTO> godClasses   = classList.stream()
                                               .filter(ClassDTO::isGodClass)
                                               .collect(Collectors.toList());

        // Within each bucket sort alphabetically for stable, readable output
        Comparator<ClassDTO> alpha = Comparator.comparing(c -> cleanName(c.getClassName()));
        interfaces.sort(alpha);
        abstracts.sort(alpha);
        regular.sort(alpha);
        godClasses.sort(alpha);

        StringBuilder sb = new StringBuilder();
        appendHeader(sb);
        appendSkinParams(sb);

        // ── 1. Interfaces ─────────────────────────────────────────────────────
        if (!interfaces.isEmpty()) {
            sb.append("' ── Interfaces ──────────────────────────────────\n");
            for (ClassDTO dto : interfaces) appendClassBlock(sb, dto);
        }

        // ── 2. Abstract classes ───────────────────────────────────────────────
        if (!abstracts.isEmpty()) {
            sb.append("' ── Abstract Classes ────────────────────────────\n");
            for (ClassDTO dto : abstracts) appendClassBlock(sb, dto);
        }

        // ── 3. Regular classes ────────────────────────────────────────────────
        if (!regular.isEmpty()) {
            sb.append("' ── Classes ─────────────────────────────────────\n");
            for (ClassDTO dto : regular) appendClassBlock(sb, dto);
        }

        // ── 4. God classes (separated at the bottom) ──────────────────────────
        if (!godClasses.isEmpty()) {
            sb.append("' ── God Classes ─────────────────────────────────\n");
            // A PlantUML package block visually groups and separates god classes
            sb.append("package \"God Classes\" #FFD6D6 {\n");
            for (ClassDTO dto : godClasses) appendClassBlock(sb, dto);
            sb.append("}\n\n");
        }

        // ── Relationships (after all class definitions) ───────────────────────
        for (ClassDTO dto : classList) {
            appendRelationships(sb, dto, classList);
        }

        sb.append("@enduml");
        return sb.toString();
    }

    // ── Private section builders ──────────────────────────────────────────────

    private void appendHeader(StringBuilder sb) {
        sb.append("@startuml\n");
        // "elk" replaced "smetana" as the recommended built-in Java layout engine
        // in PlantUML 1.2022+. It correctly honours "linetype ortho" (right-angle
        // arrows) which smetana partially ignores.
        sb.append("!pragma layout elk\n");
        if (leftToRight) sb.append("left to right direction\n");
    }

    private void appendSkinParams(StringBuilder sb) {
        sb.append("skinparam dpi ").append(dpi).append("\n");
        sb.append("skinparam backgroundcolor white\n");
        sb.append("skinparam shadowing false\n");

        // ── Orthogonal (angled) arrows — no curves ────────────────────────────
        // Both directives are needed: skinparam targets PlantUML's renderer,
        // while the <style> block in appendHeader targets the smetana engine
        // directly. Together they ensure no curved connectors appear.
        sb.append("skinparam linetype ortho\n");
        sb.append("skinparam ArrowColor #444444\n");
        sb.append("skinparam ArrowThickness 1\n");

        sb.append("skinparam nodesep ").append(nodesep).append("\n");
        sb.append("skinparam ranksep ").append(ranksep).append("\n");
        sb.append("skinparam margin ").append(margin).append("\n");
        sb.append("skinparam padding ").append(padding).append("\n");

        sb.append("skinparam class {\n")
          .append("  BackgroundColor White\n")
          .append("  ArrowColor #444444\n")
          .append("  BorderColor #666666\n")
          .append("}\n");

        sb.append("skinparam interface {\n")
          .append("  BackgroundColor #E1F5FE\n")
          .append("  BorderColor #01579B\n")
          .append("}\n");

        // God class stereotype styling
        sb.append("skinparam class<<GodClass>> {\n")
          .append("  BackgroundColor #FFAAAA\n")
          .append("  BorderColor #CC0000\n")
          .append("  FontColor #CC0000\n")
          .append("}\n");

        sb.append("skinparam classAttributeIconSize 0\n");
        sb.append("set namespaceSeparator none\n\n");
    }

    private void appendClassBlock(StringBuilder sb, ClassDTO dto) {
        String name  = cleanName(dto.getClassName());
        String type  = resolveType(dto);
        // God classes get the <<GodClass>> stereotype for skinparam targeting
        // (the package block already groups them visually)
        String stereo = dto.isGodClass() ? " <<GodClass>>" : "";

        sb.append(type).append(quoted(name)).append(stereo).append(" {\n");
        appendMembers(sb, dto.getFields());
        sb.append("  --\n");
        appendMembers(sb, dto.getMethods());
        sb.append("}\n\n");
    }

    private void appendMembers(StringBuilder sb, List<String> members) {
        if (members == null) return;
        for (String member : members) sb.append("  ").append(member).append("\n");
    }

    private void appendRelationships(StringBuilder sb, ClassDTO dto, List<ClassDTO> all) {
        String current = cleanName(dto.getClassName());

        if (dto.getSuperClassName() != null) {
            String parent = cleanName(dto.getSuperClassName());
            sb.append(quoted(parent)).append(" <|-- ").append(quoted(current)).append("\n");
        }

        for (String iface : dto.getImplementedInterfaces()) {
            sb.append(quoted(cleanName(iface))).append(" <|.. ").append(quoted(current)).append("\n");
        }

        if (dto.getDependencies() != null) {
            for (String dep : dto.getDependencies()) {
                String cleanDep = cleanName(dep);
                if (isInList(cleanDep, all) && !cleanDep.equals(current)) {
                    sb.append(quoted(current)).append(" ..> ").append(quoted(cleanDep)).append("\n");
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns classes matching the given type flags, excluding god classes
     * (god classes are always handled in their own bucket).
     */
    private static List<ClassDTO> filter(List<ClassDTO> list,
                                         boolean isInterface,
                                         boolean isAbstract,
                                         boolean isGod) {
        return list.stream()
                   .filter(c -> c.isInterface() == isInterface
                             && c.isAbstract()  == isAbstract
                             && c.isGodClass()  == isGod)
                   .collect(Collectors.toList());
    }

    private static String cleanName(String name) {
        return name == null ? "" : name.replace(".java", "");
    }

    private static String quoted(String name) {
        return "\"" + name + "\"";
    }

    private static String resolveType(ClassDTO dto) {
        if (dto.isInterface()) return "interface ";
        if (dto.isAbstract())  return "abstract class ";
        return "class ";
    }

    private static boolean isInList(String name, List<ClassDTO> list) {
        return list.stream().anyMatch(c -> cleanName(c.getClassName()).equals(name));
    }
}