import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== STUDENT MANAGEMENT SYSTEM =====");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student Department");
            System.out.println("4. Delete Student");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            switch (choice) {
                case 1:
                    addStudent(scanner);
                    break;
                case 2:
                    viewStudents();
                    break;
                case 3:
                    updateStudent(scanner);
                    break;
                case 4:
                    deleteStudent(scanner);
                    break;
                case 5:
                    System.out.println("Exiting application. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1 to 5.");
            }
        }
    }

    // 1. CREATE
    private static void addStudent(Scanner scanner) {
        System.out.print("Enter Register Number (e.g., REG101): ");
        String regNo = scanner.nextLine();

        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Department (e.g., CSE): ");
        String dept = scanner.nextLine();

        String sql = "INSERT INTO students (reg_number, full_name, department) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, regNo);
            pstmt.setString(2, name);
            pstmt.setString(3, dept);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println(">> Student added successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 2. READ
    private static void viewStudents() {
        String sql = "SELECT * FROM students";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n------------------------------------------------------------");
            System.out.printf("%-5s | %-15s | %-25s | %-10s%n", "ID", "Reg Number", "Name", "Department");
            System.out.println("------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("student_id");
                String reg = rs.getString("reg_number");
                String name = rs.getString("full_name");
                String dept = rs.getString("department");

                System.out.printf("%-5d | %-15s | %-25s | %-10s%n", id, reg, name, dept);
            }

            if (!found) {
                System.out.println("No records found in database.");
            }
            System.out.println("------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 3. UPDATE
    private static void updateStudent(Scanner scanner) {
        System.out.print("Enter Register Number of student to update: ");
        String regNo = scanner.nextLine();

        System.out.print("Enter New Department: ");
        String newDept = scanner.nextLine();

        String sql = "UPDATE students SET department = ? WHERE reg_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDept);
            pstmt.setString(2, regNo);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(">> Department updated successfully!");
            } else {
                System.out.println(">> No student found with Register Number: " + regNo);
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 4. DELETE
    private static void deleteStudent(Scanner scanner) {
        System.out.print("Enter Register Number of student to delete: ");
        String regNo = scanner.nextLine();

        String sql = "DELETE FROM students WHERE reg_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, regNo);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println(">> Student record deleted successfully!");
            } else {
                System.out.println(">> No student found with Register Number: " + regNo);
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}