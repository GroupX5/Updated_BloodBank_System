Blood Bank Management System (Updated Version) - README
--------------------------------------------------

### Project Overview
This is the updated version of my Blood Bank Management System project, developed in Apache NetBeans IDE with JDK 24. It helps manage blood donors and stock with improvements like validating blood groups, normalizing names, logging actions, and tracking donor eligibility. This version fixes six gaps and also one additional from the unupdated version, such as no validation and no feedback, making it safer and more reliable.

### GitHub Repository
- Repository: https://github.com/GroupX5/Updated_BloodBank_System
- Access: View or download the files directly from this link. For contributions or cloning, use Git with the URL above.

### Folder Structure
- Source_Code/: Contains Java source files (e.g., DBConnection.java, Login.java, Home.java) with updated features.
- Database/: SQL scripts (bloodbank.sql, audit_log.sql) to set up the MySQL database.
- Lib/: MySQL Connector JAR (mysql-connector-java-8.0.XX.jar) for database connectivity.
- dist/: Runnable JAR (BloodBankManagementSystem.jar) and its dependencies, built from NetBeans.

### Requirements
- JDK 24: Required to compile and run the project (download from oracle.com if needed).
- MySQL: To set up and run the local database.
- Apache NetBeans IDE: Optional, for editing and running the source code.
- MySQL Connector JAR: Included in the Lib/ folder.

### Setup Instructions
Follow these steps to set up and run the project on your computer:

1. **Set Up MySQL Database**:
   - Install MySQL and open MySQL Workbench or phpMyAdmin.
   - Create a new database: `CREATE DATABASE bloodbank;`
   - Use the database: `USE bloodbank;`
   - Run the scripts in the Database/ folder:
     - `bloodbank.sql` to create the bloodbank table.
     - `audit_log.sql` to create the audit_log table.
   - Add sample data (optional): `INSERT INTO bloodbank (name, bloodgroup, quantity, last_donation_date) VALUES ('Kaleb ', 'A+', 300, '2024-05-01');`

2. **Set Up the MySQL Connector**:
   - If using NetBeans, add the JAR from Lib/mysql-connector-java-8.0.XX.jar:
     - Right-click the project > Properties > Libraries > Add JAR/Folder.
   - If running the JAR file, the connector is included in dist/lib/.

3. **Run the Project**:
   - **Option 1: Using NetBeans**:
     - Open NetBeans with JDK 24.
     - Import the project by dragging the Source_Code/ files into a new project or using File > Open Project.
     - Right-click Login.java and select "Run File."
   - **Option 2: Using the JAR File**:
     - Navigate to the dist/ folder in a terminal.
     - Run: `java -jar BloodBankManagementSystem.jar`
     - Ensure JDK 24 is your default Java version.

### How to Use the System
- **Login**: Start with Login.java or the JAR. Enter a username and password (modify DBConnection.java for a test user if needed).
- **Home Page**: Access buttons to add, update, delete, or view donors.
- **Add Donor**: Enter name, blood group, quantity, and donation date with health checks.
- **Update/Delete**: Modify or remove donor records.
- **View Details**: Check donor lists, stock, and reminders.

### Project Features (Updated Version)
- Validates blood groups (e.g., only A+, B-, etc.).
- Normalizes donor names (e.g., fixes capitalization).
- Duplicate donor check
- Eligibility check due to the last donation date
- Stock management
- Includes status feedback.

### Notes
- Update the database URL in DBConnection.java (e.g., `jdbc:mysql://localhost:3306/bloodbank`) if your MySQL setup differs.
- The Documentation/BloodBankPresentation.pptx contains UML diagrams and more details.
- For Git issues, use a Personal Access Token (PAT) instead of a password (see GitHub Settings > Developer settings).

### Contact
For questions or help, email us at -- groupx.bbsm.project@gmail.com.

Thank you!
