CRAFTY User Interface

Interface Functionalities
a.	Data visualization and management:
The user interface display input and output data across spatial, temporal, and various scenario dimen-sions. It allows users to interactively navigate through maps of different capitals under distinct scenarios and years. The interface features a 'General View' window, which presents the list of capitals, services, scenarios, and Agent Functional Types (AFTs) used in the project.
The Spatial Data View window facilitates the exploration of maps representing different types of capital, tailored to different scenarios and time frames. As users navigate through these maps, a corresponding histogram is generated, illustrating the frequency distribution of capital values. In addition, the interface feature allows for an interactive visualization of demand within each scenario, which can be set using the 'Scenario' button. A baseline map of AFTs is also displayed, showing the distribution of each AFT.
b.	Agents functional types (AFTs) configurations: 
Each cell is managed by a land-manager agent. The population of agents is drawn from an agent function-al types (AFT) typology that defines their general characteristics. Land-manager agents can leverage the capitals available in a cell to produce a range of ecoservice. These can represent anything that is produced from the land such as food, timber or recreation. Formally, each land manager agent has a production function to map capital levels in a cell onto provision of a suite of services. A CobbeDouglas-style function is used by default to combine optimal production levels with dependence on each capital to give service productivity.
AFT parameters are categorized into two groups: behavior and productivity. The productivity parameters include the optimal productivity level and a sensitivity matrix. The AFTs configuration window is designed for a visual representation of AFT data, facilitating easy modifications of parameters. Through the Choice-Box, users can access and modify the parameters of any selected AFT.


c.	Simulation configuration and monitoring:
Before starting a simulation with CRAFTY, users can tailor the model's behavior through the configura-tion Tab. This allows for the selection of specific mechanisms to be considered during the simulation.
•	Consider negative marginal utility: This option addresses the scenario where the supply of an eco-system service exceeds societal demand, potentially reducing its competitiveness. When enabled, negative marginal utilities are set to zero, preventing penalties for agents producing in excess of demand.
•	Consider Land abandonment (Give-Up) mechanism: Agents are assigned a minimum acceptable return level for land cells. If an agent's competitiveness falls below this threshold, it will abandon the cell based on the assigned give-up probability.
•	Selection of search algorithm for competition:  The user can choose which AFTs can compete for a land, AFTs can be selected from the neighbors of radius “Radius” or from the list of all AFTs. The user can also define the percentage of selection of the most competitive AFTs or random selection.
•	Annual Cell Competition Percentage: Users define the proportion of land cells available for AFT competition each year (Percentage of cells could change for each tick).
During the simulation, the interface dynamically updates to reflect agent distribution and service productivity on the map. A synchronized chart tracks demand and supply for each ecosystem service. 
 

1.	Installation
a.	Java Development Kit (JDK)
Java Development Kit (JDK) is a software development environment used for developing Java applications. It includes the Java Runtime Environment (JRE), an interpreter/loader (Java), a compiler (javac), an archiver (jar), a documentation generator (Javadoc), and other tools needed in Java development.
Installing JDK:
Visit the official Oracle website: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html, Choose the appropriate JDK version for your operating system (Windows, macOS, Linux) and download the installer then Install Java JDK.
b.	Eclipse IDE
Eclipse IDE (Integrated Development Environment) is a popular, open-source development environment that supports multiple programming languages. It is widely used for Java development but also supports other languages through plugins. Eclipse offers features like code completion, debugging, syntax highlighting, and an extensible plugin system.
Installing Eclipse:
Visit the Eclipse download page: https://www.eclipse.org/downloads/, and download the Eclipse Installer for your operating system.
Run the Eclipse Installer. When prompted, choose either the “Eclipse IDE for Java Developers” or the “Enterprise Java and Web Developers” option. Choose the installation folder and follow the on-screen instructions to complete the installation.
Potentially two errors occur after installation. 
•	The project cannot be built until build path errors are resolved to fix this:
 Right click your project –> Build Path –> Configure Build Path
•	Unbound class path container: 'JRE System Library[jdk1.5.0_08]'. To resolve this, select the 'JRE Library' in the project's build path settings, click 'Edit', and in the 'Edit Library' window, select an alternate JRE that has been configured with your Eclipse installation. Once selected, click 'Finish'.
c.	JavaFX
JavaFX is a software platform for creating and delivering desktop applications, as well as rich internet applications that can run across a wide variety of devices. It features a set of graphics and media APIs with high-performance hardware-accelerated graphics and media engines to simplify development of immersive visual applications.
Adding JavaFX to Eclipse:
Launch Eclipse and go to Help > Eclipse Marketplace then search for JavaFX.
You should find plugins named “e(fx)clipse" which is a popular choice for integrating JavaFX into Eclipse. 
Click the Install button next to the plugin and follow the on-screen instructions to complete the installation. This might involve accepting licenses and restarting Eclipse.
After restarting Eclipse.
Note: To confirm that the JavaFx is well installed, create a new JavaFX project:  Go to File > New > Project… Look for "JavaFX" in the list of project types. If 'JavaFX' appears in the list of project types, this confirms that it has been installed successfully.
Set Up JavaFX SDK:
•	Download JavaFX SDK from https://gluonhq.com/products/javafx/.
•	Extract the SDK to a known location.
•	Open Eclipse IDE and create a new JavaFx project (File > New > Project… Look for "JavaFX" folder > choose “JavaFX Project” > Follow the wizard to set up your project)
•	Right-click on the project in the Package Explorer and select "Properties."
•	In the properties window, go to "Java Build Path.
•	In the "Java Build Path" section, click on the "Libraries" tab.
•	Add JARs to Modulepath by Clicking on "Add External JARs..." or "Add JARs...".
•	Browse to the lib folder of the JavaFX SDK you downloaded (it should contain the javafx.base, javafx.controls, javafx.fxml, etc., JAR files).
•	Select all the JAR files in this folder for your JavaFX application and click "Apply and close" to save the configuration.
2.	CRAFTY_UI code download
 Open Eclipse.
1.	Import Project:
a.	Go to File > Import….
b.	Select Project from Git (with Smart import) and click Next.
2.	Clone Repository:
a.	Select Clone URI and click Next.
b.	Paste https://github.com/CRAFTY-ABM/CRAFTY_User_Interface  into the URI field and click Next.
3.	Continue with Defaults: Click Next for the remaining steps and Click Finish.

 
Change JavaFX SDK files by your files
•	Right click on CRAFTY-UI Project.
•	In the properties window, go to "Java Build Path.
•	In the "Java Build Path" section, click on the "Libraries" tab.
•	Delete the existing JavaFX SDK files and Add yours by Clicking on "Add External JARs..." or "Add JARs...".
•	Browse to the lib folder of the JavaFX SDK you downloaded.
•	Select all the JAR files in this folder for your JavaFX application and click "Apply and close" to save the configuration.
 
Run the Model
Run Configurations: Set VM Arguments for JavaFX:
i.	In the top menu, click "Run > Run Configurations...".
ii.	In the left panel, right-click on "Java Application" and select "New Configuration".
iii.	In the "Main" tab, ensure that “Crafty_UserInterface” is in the Project field and main class fields are “main.FxMain” correctly set to your project and main class.
 
iv.	Switch to the "Arguments" tab. In the "VM arguments" field, you will need to include the path to the JavaFX SDK lib directory. It should look something like this (make sure to replace the path with the actual path on your system):
--module-path "path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml
 
v.	Run Your Application: Click "Apply" to save the run configuration and Click "Run" to start your application. To run a CRAFTY next time, just right-click on the project or the main Java file in the 'Package Explorer', then choose 'Run As' followed by 'Java Application'. 
3.	Data
Before you can run CRAFTY, you need input data. You can find here https://osf.io/wcybv/files/osfstorage a simplified example of CARFTT-DE data with 3 km resolution from the original data (1 km resolution). Download the ZIP file and extract it to a known directory.
