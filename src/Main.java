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
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the newline buffer

            switch (choice) {
                case 1:
                    addStudent(scanner);
                    break;
                case 2:
                    viewStudents();
                    break;
                case 3:
                    System.out.println("Exiting application. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please choose 1, 2, or 3.");
            }
        }
    }

    // 1. Add Student (INSERT query)
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

    // 2. View All Students (SELECT query)
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
}