
import java.util.ArrayList;
import java.util.Scanner;

class Student {
    String name;
    double marks;

    Student(String name, double marks) {
        this.name = name;
        this.marks = marks;
    }
}

public class StudentGradeTracker {
    static ArrayList<Student> students = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int choice;

        do {
            System.out.println("\n===== STUDENT GRADE TRACKER =====");
            System.out.println("1. Add Student");
            System.out.println("2. Display All Students");
            System.out.println("3. Show Summary");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    displayStudents();
                    break;
                case 3:
                    showSummary();
                    break;
                case 4:
                    System.out.println("Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 4);

        sc.close();
    }

    static void addStudent() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine();

        System.out.print("Enter marks (0-100): ");
        double marks = sc.nextDouble();
        sc.nextLine();

        if (marks < 0 || marks > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return;
        }

        students.add(new Student(name, marks));
        System.out.println("Student added successfully!");
    }

    static void displayStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }

        System.out.println("\n--- Student Records ---");
        for (Student s : students) {
            System.out.println(
                "Name: " + s.name +
                " | Marks: " + s.marks +
                " | Grade: " + getGrade(s.marks)
            );
        }
    }

    static void showSummary() {
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }

        double total = 0;
        double highest = students.get(0).marks;
        double lowest = students.get(0).marks;
        String topper = students.get(0).name;

        for (Student s : students) {
            total += s.marks;

            if (s.marks > highest) {
                highest = s.marks;
                topper = s.name;
            }

            if (s.marks < lowest) {
                lowest = s.marks;
            }
        }

        System.out.println("\n===== SUMMARY REPORT =====");
        System.out.println("Total students: " + students.size());
        System.out.printf("Average marks: %.2f%n",
                          total / students.size());
        System.out.println("Highest marks: " + highest);
        System.out.println("Lowest marks: " + lowest);
        System.out.println("Topper: " + topper);
    }

    static String getGrade(double marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 40) return "D";
        return "Fail";
    }
}
