# God Class Inspector

God Class Inspector is an Eclipse IDE plugin developed to automate the detection of the "God Class" (or God Object) anti-pattern within Java codebases and provide data-driven refactoring suggestions.

Developed as a thesis project at the Department of Computer Science and Engineering, University of Ioannina, this repository serves as an academic proof-of-concept and a research tool for software architecture analysis. It is not actively maintained or distributed as a standalone commercial product.

The God Class anti-pattern manifests when a single class assumes excessive responsibilities and centralizes system control, thereby violating the Single Responsibility Principle (SRP). Such classes degrade software maintainability, testability, and extensibility. To address this, the God Class Inspector seamlessly integrates into the Eclipse environment to parse Java source code, compute structural object-oriented metrics, generate visual architectural representations, and provide actionable refactoring recommendations.

## Features

* **Seamless Eclipse Integration:** Run analysis directly from the main menu bar or via context menus in the Package/Project Explorer.
* **Advanced Code Parsing:** Utilizes JavaParser alongside the Eclipse JDT to accurately parse and analyze Java source code into Abstract Syntax Trees (AST).
* **Automated God Class Detection:** Analyzes code based on primary object-oriented metrics (WMC, ATFD, TCC).
* **Refactoring Suggestions:** Goes beyond detection by providing actionable refactoring advice based on deep code analysis (Extract Method, Move Method, Extract Class).
* **Visual Architecture (UML):** Integrates with PlantUML to automatically generate and display UML diagrams highlighting the God Classes within the project's architecture, including precise mappings of fields, methods, inheritance, associations, and dependencies.
* **Customizable Detection:** Includes a dedicated settings menu allowing users to tweak the thresholds and parameters for God Class detection.

## How it Works

The plugin utilizes a custom detection and suggestion engine built with JavaParser to analyze ASTs.

### Metric Calculations
God Classes are detected based on the following customizable default thresholds:
* **WMC (Weighted Method Count):** `>= 47`. Calculates method complexity based on loops, conditional statements, and boolean expressions.
* **ATFD (Access to Foreign Data):** `> 5`. Counts the number of times a class accesses fields or getter methods of other classes.
* **TCC (Tight Class Cohesion):** `< 0.33`. Evaluates how tightly coupled the methods of the class are by measuring shared field usage.

### Refactoring Engine
The suggestion engine provides specific guidance for flagged classes:
* **Extract Method:** Identifies methods with a high complexity level (default `>= 3`) by analyzing the nesting depth of loops, conditionals, and try-catch blocks.
* **Move Method:** Highlights methods tightly coupled to other classes based on Locality of Attribute Access (`LAA < 0.33`) and suggests the target class that provides the most data to that method.
* **Extract Class:** Suggests breaking apart classes by grouping methods that share a high Jaccard Similarity in field usage (`> 0.30`) or contain internal method calls to one another.

## Repository Structure

Built as an Eclipse Plug-in, it follows the standard Plug-in Development Environment (PDE) architecture:

* **`plugin.xml`**: The core configuration file that registers the custom commands, context menus, and the UI views (FoundFilesUI, UmlDiagramUI, RefactoringSuggestionsUI).
* **`META-INF/MANIFEST.MF`**: The OSGi manifest defining plugin dependencies (including required Eclipse bundles like `org.eclipse.jdt.core` and libraries like `javaparser-core` and `plantuml`).
* **`src/`**: Contains the Java source code for the plugin, structured into:
  * **`handlers/`**: Eclipse UI action handlers.
  * **`services/`**: Core logic for Discovery, Detection, UML generation, and Suggestions.
  * **`models/`**: DTOs and Data models.
  * **`ui/`**: Custom Eclipse Views and Dialogs.
  * **`builders/`**: PlantUML generation logic.

## Running the Project

Because this is an academic thesis project, there are no pre-compiled `.jar` releases provided. To test or use the plugin, it must be run directly from source using the Eclipse Plug-in Development Environment (PDE).

### Prerequisites
* **Eclipse IDE:** Eclipse IDE for Enterprise Java and Web Developers (must include PDE).
* **Java:** JavaSE-21 (Required execution environment).

### Build and Launch Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/ptrypos/GodClassInspector.git
   ```
2. Open your Eclipse IDE.
3. Go to **File > Import > General > Existing Projects into Workspace**.
4. Select the cloned directory and import the plugin project.
5. Right-click the imported project in the Package Explorer.
6. Select **Run As > Eclipse Application**.
7. A secondary Eclipse instance will launch with the GodClassInspector plugin loaded and ready for testing.

## Usage

### Running an Analysis
1. Open or create a Java project in the new Eclipse instance.
2. Select the project in the Package Explorer.
3. Trigger the plugin in two ways:
   * **Main Menu:** Click **God Class Inspector** in the top menu bar.
   * **Context Menu:** Right-click the project, open the **God Class Inspector** menu.
4. Select **Scan And Analyze Project** (or perform a Scan and Analysis separately).
5. To adjust metrics, click **Change Settings** from the God Class Inspector menu.

### Viewing Results
The plugin provides specialized views. To open them, navigate to **Window -> Show View -> Other...**, expand the **God Class Inspector** folder, and open:
* **God Class Inspector:** The main view listing the detected files, highlighting which ones are classified as God Classes or Unrefactable Large Classes.
* **UML With God Classes:** A PlantUML-generated visual representation of the classes. God Classes are highlighted in red, and unrefactable large classes in yellow.
* **Refactoring Suggestions:** A searchable tree view providing actionable advice on how to split or improve the flagged classes via Extract Method, Move Method, or Extract Class operations.

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

**Author:** ptrypos
