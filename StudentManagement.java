import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ProjectStudent implements Serializable, Comparable<ProjectStudent> {
    private static final long serialVersionUID = 1L;

    int serialNumber;
    String name;
    int roll;
    String sec;
    String dep;
    int year;
    long phoneNumber;
    float GPa;

    // Constructor
    public ProjectStudent(int serialNumber, String name, int roll, String sec, String dep, int year, long phoneNumber, float GPa) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.roll = roll;
        this.sec = sec;
        this.dep = dep;
        this.year = year;
        this.phoneNumber = phoneNumber;
        this.GPa = GPa;
    }

    // Getters and setters for GPA
    public float getGPa() {
        return GPa;
    }

    public void setGPa(float GPa) {
        this.GPa = GPa;
    }

    // Comparable interface implementation for sorting by roll number
    @Override
    public int compareTo(ProjectStudent other) {
        return Integer.compare(this.roll, other.roll);
    }

    // Custom comparator for sorting by GPA
    public static class CompareByGpa implements java.util.Comparator<ProjectStudent> {
        @Override
        public int compare(ProjectStudent s1, ProjectStudent s2) {
            return Float.compare(s2.GPa, s1.GPa); // Note: we use s2.GPa - s1.GPa for a max-heap behavior
        }
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private byte[] passwordHash;

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public boolean authenticate(String password) {
        return MessageDigest.isEqual(hashPassword(password), passwordHash);
    }

    private byte[] hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}

public class StudentManagement {
    static ProjectStudent[] students = new ProjectStudent[5];
    static PriorityQueue<ProjectStudent> waitingList = new PriorityQueue<>(new ProjectStudent.CompareByGpa());
    static User user = new User("admin", "password");
    static int studentCount = 0;
    static int serialCounter = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        if (login(scanner)) {
            while (true) {
                switch (menu(scanner)) {
                    case 1:
                        insert(scanner);
                        break;
                    case 2:
                        System.out.println("\n\t\tData you have entered:-\n ");
                        viewList();
                        break;
                    case 3:
                        save();
                        break;
                    case 4:
                        search(scanner);
                        break;
                    case 5:
                        delete(scanner);
                        break;
                    case 6:
                        sort(scanner);
                        break;
                    case 7:
                        viewWaitingList();
                        break;
                    case 0:
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("\nENTER YOUR CHOICE BETWEEN 1 & 7\n");
                }
            }
        }
    }

    static boolean login(Scanner scanner) {
        System.out.print("\n\t\tEnter username: ");
        String username = scanner.next();

        Console console = System.console();
        if (console == null) {
            System.out.println("No console available. Exiting...");
            return false;
        }

        char[] passwordArray = console.readPassword("\n\t\tEnter password: ");
        String password = new String(passwordArray);

        if (user.getUsername().equals(username) && user.authenticate(password)) {
            Arrays.fill(passwordArray, ' ');
            System.out.println("\n\t\tLogin successful!\n");
            return true;
        } else {
            Arrays.fill(passwordArray, ' ');
            System.out.println("\n\t\tInvalid username or password. Access denied.\n");
            return false;
        }
    }

    static void insert(Scanner scanner) {
        System.out.print("\n\t\tEnter the name: ");
        String name = scanner.next();
        int roll = getIntInput(scanner, "roll");
        System.out.print("\n\t\tEnter the sec: ");
        String sec = scanner.next();
        System.out.print("\n\t\tEnter the department: ");
        String dep = scanner.next();
        int year = getIntInput(scanner, "year of admission");
        long phoneNumber = getLongInput(scanner, "phone number");
        float GPa = getFloatInput(scanner, "GPa");

        ProjectStudent temp = new ProjectStudent(serialCounter++, name, roll, sec, dep, year, phoneNumber, GPa);

        if (studentCount >= 5) {
            System.out.println("\n\t\tMaximum student limit reached (5). Adding to waiting list based on GPA.\n");
            waitingList.add(temp);
        } else {
            int i;
            for (i = studentCount - 1; (i >= 0 && students[i].roll > roll); i--) {
                students[i + 1] = students[i];
            }
            students[i + 1] = temp;
            studentCount++;
        }
    }

    static void viewList() {
        if (studentCount == 0) {
            System.out.println("\n\t\tList is empty.\n");
        } else {
            for (int i = 0; i < studentCount; i++) {
                printStudentInfo(students[i]);
            }
            System.out.println("\n\n\t\t" + studentCount + " students are in the batch.\n");
        }
    }

    static void viewWaitingList() {
        if (waitingList.isEmpty()) {
            System.out.println("\n\t\tWaiting list is empty.\n");
        } else {
            System.out.println("\n\t\tStudents in the waiting list based on GPA:");
            for (ProjectStudent student : waitingList) {
                printStudentInfo(student);
            }
        }
    }

    static void save() {
        if (studentCount == 0) {
            System.out.println("\n\t\tNo data is Entered\n");
        } else {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Project.dat"))) {
                oos.writeObject(students);
                System.out.println("\n\t\t[*Data saved successfully]\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void search(Scanner scanner) {
        System.out.println("\n\t\tSearch Options:");
        System.out.println("\t\t1. Search by roll number");
        System.out.println("\t\t2. Search by name");
        int choice = getIntInput(scanner, "search option");

        switch (choice) {
            case 1:
                int roll = getIntInput(scanner, "roll number");
                ProjectStudent foundByRoll = binarySearchByRoll(roll);
                if (foundByRoll != null) {
                    printStudentInfo(foundByRoll);
                } else {
                    System.out.println("\n\t\tStudent with roll number " + roll + " not found.\n");
                }
                break;
            case 2:
                System.out.print("\n\t\tEnter name to search: ");
                String name = scanner.next();
                ProjectStudent foundByName = searchByName(name);
                if (foundByName != null) {
                    printStudentInfo(foundByName);
                } else {
                    System.out.println("\n\t\tStudent with name '" + name + "' not found.\n");
                }
                break;
            default:
                System.out.println("\n\t\tInvalid choice. Please enter 1 or 2.\n");
        }
    }

    static ProjectStudent binarySearchByRoll(int roll) {
        int left = 0, right = studentCount - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (students[mid].roll == roll) {
                return students[mid];
            } else if (students[mid].roll < roll) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    static ProjectStudent searchByName(String name) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].name.equals(name)) {
                return students[i];
            }
        }
        return null;
    }

    static void delete(Scanner scanner) {
        System.out.println("\n\t\tDelete Options:");
        System.out.println("\t\t1. Delete by roll number");
        System.out.println("\t\t2. Delete last entry");
        int choice = getIntInput(scanner, "delete option");

        switch (choice) {
            case 1:
                deleteByRoll(scanner);
                break;
            case 2:
                deleteLastEntry();
                break;
            default:
                System.out.println("\n\t\tInvalid choice. Please enter 1 or 2.\n");
        }
    }

    static void deleteByRoll(Scanner scanner) {
        int roll = getIntInput(scanner, "roll number to delete");
        int i;
        for (i = 0; i < studentCount; i++) {
            if (students[i].roll == roll) {
                break;
            }
        }

        if (i == studentCount) {
            System.out.println("\n\t\tStudent with roll number " + roll + " not found.\n");
        } else {
            for (int j = i; j < studentCount - 1; j++) {
                students[j] = students[j + 1];
            }
            students[--studentCount] = null;
            System.out.println("\n\t\tStudent with roll number " + roll + " deleted successfully.\n");

            // If waiting list is not empty, add the highest GPA student to the array
            if (!waitingList.isEmpty()) {
                students[studentCount++] = waitingList.poll();
                System.out.println("\n\t\tStudent from waiting list added to the main list.\n");
            }
        }
    }

    static void deleteLastEntry() {
        if (studentCount == 0) {
            System.out.println("\n\t\tList is empty. Nothing to delete.\n");
        } else {
            students[--studentCount] = null;
            System.out.println("\n\t\tLast student entry deleted successfully.\n");

            // If waiting list is not empty, add the highest GPA student to the array
            if (!waitingList.isEmpty()) {
                students[studentCount++] = waitingList.poll();
                System.out.println("\n\t\tStudent from waiting list added to the main list.\n");
            }
        }
    }

    static void sort(Scanner scanner) {
        if (studentCount < 2) {
            System.out.println("\n\t\tInsufficient data to perform sorting.\n");
            return;
        }

        System.out.println("\n\t\tSort Options:");
        System.out.println("\t\t1. Sort by year");
        System.out.println("\t\t2. Sort by GPA");
        int choice = getIntInput(scanner, "sort option");

        switch (choice) {
            case 1:
                sortByYear();
                break;
            case 2:
                sortByGpa();
                break;
            default:
                System.out.println("\n\t\tInvalid choice. Please enter 1 or 2.\n");
        }
    }

    static void sortByYear() {
        for (int i = 0; i < studentCount - 1; i++) {
            for (int j = 0; j < studentCount - i - 1; j++) {
                if (students[j].year > students[j + 1].year) {
                    ProjectStudent temp = students[j];
                    students[j] = students[j + 1];
                    students[j + 1] = temp;
                }
            }
        }
        System.out.println("\n\t\tList sorted by year.\n");
    }

    static void sortByGpa() {
        Arrays.sort(students, 0, studentCount, new ProjectStudent.CompareByGpa());
        System.out.println("\n\t\tList sorted by GPA.\n");
    }

    static int menu(Scanner scanner) {
        System.out.println("\n1: Add Student Details");
        System.out.println("\n2: View Student List");
        System.out.println("\n3: Save Data");
        System.out.println("\n4: Find Student Information");
        System.out.println("\n5: Delete Student Details");
        System.out.println("\n6: Sorting Data");
        System.out.println("\n7: View Waiting List");
        System.out.println("\n0: Exit");

        return getIntInput(scanner, "Choice");
    }

    static int getIntInput(Scanner scanner, String field) {
        while (true) {
            try {
                System.out.print("\n\t\tEnter " + field + ": ");
                return scanner.nextInt();
            } catch (Exception e) {
                System.out.println("\n\t\tInvalid input. Please enter a valid integer.");
                scanner.next(); // consume invalid input
            }
        }
    }

    static long getLongInput(Scanner scanner, String field) {
        while (true) {
            try {
                System.out.print("\n\t\tEnter " + field + ": ");
                return scanner.nextLong();
            } catch (Exception e) {
                System.out.println("\n\t\tInvalid input. Please enter a valid long integer.");
                scanner.next(); // consume invalid input
            }
        }
    }

    static float getFloatInput(Scanner scanner, String field) {
        while (true) {
            try {
                System.out.print("\n\t\tEnter " + field + ": ");
                return scanner.nextFloat();
            } catch (Exception e) {
                System.out.println("\n\t\tInvalid input. Please enter a valid float value.");
                scanner.next(); // consume invalid input
            }
        }
    }

    static void printStudentInfo(ProjectStudent student) {
        System.out.println("\n\t\t[*Information of Student]");
        System.out.println("\t\tSerial Number: " + student.serialNumber);
        System.out.println("\t\tName: " + student.name);
        System.out.println("\t\tRoll: " + student.roll);
        System.out.println("\t\tSec: " + student.sec);
        System.out.println("\t\tDepartment: " + student.dep);
        System.out.println("\t\tYear of admission: " + student.year);
        System.out.println("\t\tPhone Number: " + student.phoneNumber);
        System.out.println("\t\tGPa: " + student.getGPa() + "\n\n");
    }
}
