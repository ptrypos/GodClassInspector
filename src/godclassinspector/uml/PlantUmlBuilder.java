package godclassinspector.uml;

import godclassinspector.model.ClassDTO;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlantUmlBuilder {

	private int dpi = 96;
	private int nodesep = 60;
	private int ranksep = 80;
	private int margin = 30;
	private int padding = 10;
	private boolean leftToRight = false;

	public PlantUmlBuilder dpi(int dpi) {
		this.dpi = dpi;
		return this;
	}

	public PlantUmlBuilder nodesep(int n) {
		this.nodesep = n;
		return this;
	}

	public PlantUmlBuilder ranksep(int r) {
		this.ranksep = r;
		return this;
	}

	public PlantUmlBuilder margin(int m) {
		this.margin = m;
		return this;
	}

	public PlantUmlBuilder padding(int p) {
		this.padding = p;
		return this;
	}

	public PlantUmlBuilder leftToRight(boolean b) {
		this.leftToRight = b;
		return this;
	}

	public String build(List<ClassDTO> classList) {
		if (classList == null || classList.isEmpty()) return "";

		List<ClassDTO> interfaces = filter(classList, true, false, false);
		List<ClassDTO> abstracts = filter(classList, false, true, false);
		List<ClassDTO> regular = filter(classList, false, false, false);
		List<ClassDTO> godClasses = filterGodClasses(classList);

		Comparator<ClassDTO> alpha = Comparator.comparing(c -> cleanName(c.getClassName()));
		interfaces.sort(alpha);
		abstracts.sort(alpha);
		regular.sort(alpha);
		godClasses.sort(alpha);

		StringBuilder sb = new StringBuilder();
		appendHeader(sb);
		appendSkinParams(sb);
		appendSection(sb, "' ── Interfaces ──────────────────────────────────\n", interfaces);
		appendSection(sb, "' ── Abstract Classes ────────────────────────────\n", abstracts);
		appendSection(sb, "' ── Classes ─────────────────────────────────────\n", regular);
		appendSection(sb, "' ── God Classes ─────────────────────────────────\n", godClasses);

		for (ClassDTO dto : classList) {
			appendRelationships(sb, dto, classList);
		}

		sb.append("@enduml");
		return sb.toString();
	}

	private void appendHeader(StringBuilder sb) {
		sb.append("@startuml\n");
		sb.append("!pragma layout elk\n");
		if (leftToRight) sb.append("left to right direction\n");
	}

	private void appendSkinParams(StringBuilder sb) {
		sb.append("skinparam dpi ").append(dpi).append("\n");
		sb.append("skinparam backgroundcolor white\n");
		sb.append("skinparam shadowing false\n");
		sb.append("skinparam linetype ortho\n");
		sb.append("skinparam ArrowColor #444444\n");
		sb.append("skinparam ArrowThickness 1\n");
		sb.append("skinparam nodesep ").append(nodesep).append("\n");
		sb.append("skinparam ranksep ").append(ranksep).append("\n");
		sb.append("skinparam margin ").append(margin).append("\n");
		sb.append("skinparam padding ").append(padding).append("\n");
		appendClassSkinParams(sb);
		appendInterfaceSkinParams(sb);
		appendGodClassSkinParams(sb);
		sb.append("skinparam classAttributeIconSize 0\n");
		sb.append("set namespaceSeparator none\n\n");
	}

	private void appendClassSkinParams(StringBuilder sb) {
		sb.append("skinparam class {\n");
		sb.append("  BackgroundColor White\n");
		sb.append("  ArrowColor #444444\n");
		sb.append("  BorderColor #666666\n");
		sb.append("}\n");
	}

	private void appendInterfaceSkinParams(StringBuilder sb) {
		sb.append("skinparam interface {\n");
		sb.append("  BackgroundColor #E1F5FE\n");
		sb.append("  BorderColor #01579B\n");
		sb.append("}\n");
	}

	private void appendGodClassSkinParams(StringBuilder sb) {
		sb.append("skinparam class<<GodClass>> {\n");
		sb.append("  BackgroundColor #FFAAAA\n");
		sb.append("  BorderColor #CC0000\n");
		sb.append("  FontColor #CC0000\n");
		sb.append("}\n");
	}

	private void appendSection(StringBuilder sb, String comment, List<ClassDTO> classes) {
		if (classes.isEmpty()) return;
		sb.append(comment);
		for (ClassDTO dto : classes) {
			appendClassBlock(sb, dto);
		}
	}

	private void appendClassBlock(StringBuilder sb, ClassDTO dto) {
		String name = cleanName(dto.getClassName());
		String type = resolveType(dto);
		String stereo = dto.isGodClass() ? " <<GodClass>>" : "";
		sb.append(type);
		sb.append(quoted(name));
		sb.append(stereo);
		sb.append(" {\n");
		appendMembers(sb, dto.getFields());
		sb.append("  --\n");
		appendMembers(sb, dto.getMethods());
		sb.append("}\n\n");
	}

	private void appendMembers(StringBuilder sb, List<String> members) {
		if (members == null) return;
		for (String member : members) {
			sb.append("  ");
			sb.append(member);
			sb.append("\n");
		}
	}

	private void appendRelationships(StringBuilder sb, ClassDTO dto, List<ClassDTO> all) {
		String current = cleanName(dto.getClassName());
		appendInheritance(sb, dto, current);
		appendInterfaceRealisations(sb, dto, current);
		appendDependencies(sb, dto, current, all);
	}

	private void appendInheritance(StringBuilder sb, ClassDTO dto, String current) {
		if (dto.getSuperClassName() == null) return;
		String parent = cleanName(dto.getSuperClassName());
		sb.append(quoted(parent));
		sb.append(" <|-- ");
		sb.append(quoted(current));
		sb.append("\n");
	}

	private void appendInterfaceRealisations(StringBuilder sb, ClassDTO dto, String current) {
		for (String iface : dto.getImplementedInterfaces()) {
			String cleanIface = cleanName(iface);
			sb.append(quoted(cleanIface));
			sb.append(" <|.. ");
			sb.append(quoted(current));
			sb.append("\n");
		}
	}

	private void appendDependencies(StringBuilder sb, ClassDTO dto, String current, List<ClassDTO> all) {
		if (dto.getDependencies() == null) return;
		for (String dep : dto.getDependencies()) {
			String cleanDep = cleanName(dep);
			boolean isInDiagram = isInList(cleanDep, all);
			boolean isNotSelf = !cleanDep.equals(current);
			if (isInDiagram && isNotSelf) {
				sb.append(quoted(current));
				sb.append(" ..> ");
				sb.append(quoted(cleanDep));
				sb.append("\n");
				}
		}
	}

	private static List<ClassDTO> filter(List<ClassDTO> list, boolean isInterface, boolean isAbstract, boolean isGod) {
		Stream<ClassDTO> stream = list.stream();
		Predicate<ClassDTO> matchesInterface = c -> c.isInterface() == isInterface;
		Predicate<ClassDTO> matchesAbstract = c -> c.isAbstract() == isAbstract;
		Predicate<ClassDTO> matchesGod = c -> c.isGodClass() == isGod;
		Predicate<ClassDTO> combined = matchesInterface.and(matchesAbstract).and(matchesGod);
		return stream.filter(combined).collect(Collectors.toList());
	}

	private static List<ClassDTO> filterGodClasses(List<ClassDTO> list) {
		Stream<ClassDTO> stream = list.stream();
		Predicate<ClassDTO> isGod = ClassDTO::isGodClass;
		return stream.filter(isGod).collect(Collectors.toList());
	}

	private static boolean isInList(String name, List<ClassDTO> list) {
		Stream<ClassDTO> stream = list.stream();
		Predicate<ClassDTO> nameMatches = c -> cleanName(c.getClassName()).equals(name);
		return stream.anyMatch(nameMatches);
	}

	private static String cleanName(String name) {
		if (name == null) return "";
		return name.replace(".java", "");
	}

	private static String quoted(String name) {
		return "\"" + name + "\"";
	}

	private static String resolveType(ClassDTO dto) {
		if (dto.isInterface()) return "interface ";
		if (dto.isAbstract()) return "abstract class ";
		return "class ";
	}
}