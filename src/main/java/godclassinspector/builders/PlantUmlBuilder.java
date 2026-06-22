package godclassinspector.builders;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import godclassinspector.models.UmlClassDTO;

public class PlantUmlBuilder {
	private final boolean DEFAULT_LEFTTORIGHT = false;

	private boolean leftToRight = DEFAULT_LEFTTORIGHT;

	public PlantUmlBuilder leftToRight(boolean b) {
		this.leftToRight = b;
		return this;
	}

	public String build(List<UmlClassDTO> classList) {
		if (classList == null || classList.isEmpty()) {
			return "";
		}

		List<UmlClassDTO> interfaces = filter(classList, true, false, false);
		List<UmlClassDTO> abstracts = filter(classList, false, true, false);
		List<UmlClassDTO> regular = filter(classList, false, false, false);
		List<UmlClassDTO> godClasses = filterGodClasses(classList);

		Comparator<UmlClassDTO> alpha = Comparator.comparing(c -> cleanName(c.getClassName()));
		interfaces.sort(alpha);
		abstracts.sort(alpha);
		regular.sort(alpha);
		godClasses.sort(alpha);

		StringBuilder sb = new StringBuilder();
		appendHeader(sb);
		appendSkinParams(sb);
		appendLegend(sb);
		appendSection(sb, "' ── Interfaces ──────────────────────────────────\n", interfaces);
		appendSection(sb, "' ── Abstract Classes ────────────────────────────\n", abstracts);
		appendSection(sb, "' ── Classes ─────────────────────────────────────\n", regular);
		appendSection(sb, "' ── God Classes ─────────────────────────────────\n", godClasses);

		sb.append("' ── Relationships ───────────────────────────────\n");
		for (UmlClassDTO dto : classList) {
			appendRelationships(sb, dto, classList);
		}

		sb.append("@enduml");
		return sb.toString();
	}

	private void appendHeader(StringBuilder sb) {
		sb.append("@startuml\n");
		if (leftToRight) {
			sb.append("left to right direction\n");
		}
		sb.append("\n");
	}

	private void appendSkinParams(StringBuilder sb) {
		sb.append("skinparam linetype ortho\n");
		appendGodClassSkinParams(sb);
		appendUnrefactableClassParams(sb);
		sb.append("\n");
	}

	private void appendGodClassSkinParams(StringBuilder sb) {
		sb.append("skinparam class<<GodClass>> {\n");
		sb.append("  BackgroundColor #FFAAAA\n");
		sb.append("  BorderColor #CC0000\n");
		sb.append("  FontColor #CC0000\n");
		sb.append("}\n");
	}

	private void appendUnrefactableClassParams(StringBuilder sb) {
		sb.append("skinparam class<<Unrefactable Large Class>> {\n");
		sb.append("  BackgroundColor #FFFF66\n");
		sb.append("  BorderColor #CCAA00\n");
		sb.append("  FontColor #CCAA00\n");
		sb.append("}\n");
	}

	private void appendLegend(StringBuilder sb) {
		sb.append("legend right\n");
		sb.append("  |= Line   |= Meaning                          |\n");
		sb.append("  | <\\|--   | Inheritance (extends)             |\n");
		sb.append("  | <\\|..   | Interface realization (implements)|\n");
		sb.append("  | -->     | Association (field reference)     |\n");
		sb.append("  | ..>     | Dependency (param / return / new) |\n");
		sb.append("endlegend\n\n");
	}

	private void appendSection(StringBuilder sb, String comment, List<UmlClassDTO> classes) {
		if (classes.isEmpty()) {
			return;
		}
		sb.append(comment);
		for (UmlClassDTO dto : classes) {
			appendClassBlock(sb, dto);
		}
	}

	private void appendClassBlock(StringBuilder sb, UmlClassDTO dto) {
		String name = cleanName(dto.getClassName());
		String type = resolveType(dto);
		String stereo;

		if (dto.isGodClass()) {
			stereo = " <<GodClass>>";
		} else if (dto.isUnrefactableClass()) {
			stereo = " <<Unrefactable Large Class>>";
		} else {
			stereo = "";
		}

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
		if (members == null) {
			return;
		}
		for (String member : members) {
			sb.append("  ");
			sb.append(member);
			sb.append("\n");
		}
	}

	private void appendRelationships(StringBuilder sb, UmlClassDTO dto, List<UmlClassDTO> all) {
		String current = cleanName(dto.getClassName());
		Set<String> drawn = new HashSet<>();

		appendInheritance(sb, dto, current, drawn);
		appendInterfaceRealisations(sb, dto, current, drawn);
		appendAssociations(sb, dto, current, all, drawn);
		appendDependencies(sb, dto, current, all, drawn);
	}

	private void appendInheritance(StringBuilder sb, UmlClassDTO dto, String current, Set<String> drawn) {
		if (dto.getSuperClassName() == null) {
			return;
		}
		String parent = cleanName(dto.getSuperClassName());
		sb.append(quoted(parent));
		sb.append(" <|-- ");
		sb.append(quoted(current));
		sb.append("\n");
		drawn.add(parent);
	}

	private void appendInterfaceRealisations(StringBuilder sb, UmlClassDTO dto, String current, Set<String> drawn) {
		for (String iface : dto.getImplementedInterfaces()) {
			String cleanIface = cleanName(iface);
			if (!drawn.add(cleanIface)) {
				continue;
			}
			sb.append(quoted(cleanIface));
			sb.append(" <|.. ");
			sb.append(quoted(current));
			sb.append("\n");
		}
	}

	private void appendAssociations(StringBuilder sb, UmlClassDTO dto, String current, List<UmlClassDTO> all,
			Set<String> drawn) {
		if (dto.getAssociations() == null) {
			return;
		}
		for (String assoc : dto.getAssociations()) {
			String cleanAssoc = cleanName(assoc);
			boolean isInDiagram = isInList(cleanAssoc, all);
			boolean isNotSelf = !cleanAssoc.equals(current);
			if (isInDiagram && isNotSelf && drawn.add(cleanAssoc)) {
				sb.append(quoted(current));
				sb.append(" --> ");
				sb.append(quoted(cleanAssoc));
				sb.append("\n");
			}
		}
	}

	private void appendDependencies(StringBuilder sb, UmlClassDTO dto, String current, List<UmlClassDTO> all,
			Set<String> drawn) {
		if (dto.getDependencies() == null) {
			return;
		}
		for (String dep : dto.getDependencies()) {
			String cleanDep = cleanName(dep);
			boolean isInDiagram = isInList(cleanDep, all);
			boolean isNotSelf = !cleanDep.equals(current);
			if (isInDiagram && isNotSelf && drawn.add(cleanDep)) {
				sb.append(quoted(current));
				sb.append(" ..> ");
				sb.append(quoted(cleanDep));
				sb.append("\n");
			}
		}
	}

	private static List<UmlClassDTO> filter(List<UmlClassDTO> list, boolean isInterface, boolean isAbstract, boolean isGod) {
		Stream<UmlClassDTO> stream = list.stream();
		Predicate<UmlClassDTO> matchesInterface = c -> c.isInterface() == isInterface;
		Predicate<UmlClassDTO> matchesAbstract = c -> c.isAbstract() == isAbstract;
		Predicate<UmlClassDTO> matchesGod = c -> c.isGodClass() == isGod;
		Predicate<UmlClassDTO> combined = matchesInterface.and(matchesAbstract).and(matchesGod);
		return stream.filter(combined).collect(Collectors.toList());
	}

	private static List<UmlClassDTO> filterGodClasses(List<UmlClassDTO> list) {
		Stream<UmlClassDTO> stream = list.stream();
		Predicate<UmlClassDTO> isGod = UmlClassDTO::isGodClass;
		return stream.filter(isGod).collect(Collectors.toList());
	}

	private static boolean isInList(String name, List<UmlClassDTO> list) {
		Stream<UmlClassDTO> stream = list.stream();
		Predicate<UmlClassDTO> nameMatches = c -> cleanName(c.getClassName()).equals(name);
		return stream.anyMatch(nameMatches);
	}

	private static String cleanName(String name) {
		if (name == null) {
			return "";
		}
		return name.replace(".java", "");
	}

	private static String quoted(String name) {
		return "\"" + name + "\"";
	}

	private static String resolveType(UmlClassDTO dto) {
		if (dto.isInterface()) {
			return "interface ";
		}
		if (dto.isAbstract()) {
			return "abstract class ";
		}
		return "class ";
	}
}