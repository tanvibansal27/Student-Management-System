// Task_5 -> STUDENT MANAGEMENT SYSTEM

// import packages
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

// public class extends JFrame to enable GUI window
public class Student_Management_System extends JFrame {

    //Represents a student and is serializable (can be saved to file).
    static class Student implements Serializable {

        //Fields to store student data.
        private String name;
        private String rollNum;
        private String grade;
        private int age;

        //Constructor to initialize a new student.
        public Student(String name, String rollNumber, String grade, int age) {
            this.name = name;
            this.rollNum = rollNumber;
            this.grade = grade;
            this.age = age;
        }

        //Standard getters and setters for accessing/modifying fields.
        public String getName() { return name; }
        public String getRollNumber() { return rollNum; }
        public String getGrade() { return grade; }
        public int getAge() { return age; }

        public void setName(String name) { this.name = name; }
        public void setGrade(String grade) { this.grade = grade; }
        public void setAge(int age) { this.age = age; }

        //This lets a student be displayed as a formatted string.
        @Override
        public String toString() {
            return "Name: " + name + ", Roll: " + rollNum + ", Grade: " + grade + ", Age: " + age;
        }
    }

    //Responsible for managing student list
    static class StudentManager {
        //Stores all students in memory, and filename for saving.
        private ArrayList<Student> students = new ArrayList<>();
        private final String FILE_NAME = "students.ser";

        //When app starts, load existing student data from file.
        public StudentManager() { load(); }

        //Adds a new student to the list and saves to file.
        public void add(Student s) {
            students.add(s);
            save();
        }

        //Removes a student by matching roll number.
        public boolean remove(String rollNumber) {
            boolean removed = students.removeIf(s -> s.getRollNumber().equalsIgnoreCase(rollNumber));
            if (removed) save();
            return removed;
        }

        //Finds and returns a student if roll matches.
        public Student search(String rollNumber) {
            for (Student s : students) {
                if (s.getRollNumber().equalsIgnoreCase(rollNumber)) return s;
            }
            return null;
        }

        //Returns the full list of students.
        public ArrayList<Student> getAll() {
            return students;
        }

        //Returns a new sorted list by roll number
        public ArrayList<Student> getSortedByRollNumber() {
            ArrayList<Student> sorted = new ArrayList<>(students);
            sorted.sort(Comparator.comparing(Student::getRollNumber, String.CASE_INSENSITIVE_ORDER));
            return sorted;
        }

        //Returns students matching selected grade.
        public ArrayList<Student> filterByGrade(String grade) {
            ArrayList<Student> filtered = new ArrayList<>();
            for (Student s : students) {
                if (s.getGrade().equalsIgnoreCase(grade)) {
                    filtered.add(s);
                }
            }
            return filtered;
        }

        //Serializes students list to a file.
        public void save() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
                oos.writeObject(students);
            } catch (IOException e) {
                System.out.println("Error saving: " + e.getMessage());
            }
        }

        //Loads serialized student data if file exists.
        private void load() {
            File file = new File(FILE_NAME);
            if (!file.exists()) return;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                students = (ArrayList<Student>) ois.readObject();
            } catch (Exception e) {
                System.out.println("Error loading: " + e.getMessage());
            }
        }
    }


    //GUI Components
    private final StudentManager manager = new StudentManager();
    private final JTextField nameField = new JTextField(15);
    private final JTextField rollField = new JTextField(15);
    private final JTextField gradeField = new JTextField(15);
    private final JTextField ageField = new JTextField(15);
    private final JTextArea outputArea = new JTextArea(10, 40);
    private final JComboBox<String> gradeFilterBox = new JComboBox<>(new String[]{"All", "A", "B", "C"});
    private boolean darkMode = false;

    //Constructor: GUI Setup
    public Student_Management_System() {
        setTitle("------Student Management System------");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Form
        JPanel formPanel = new JPanel(new GridLayout(6, 2));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Roll Number:"));
        formPanel.add(rollField);
        formPanel.add(new JLabel("Grade:"));
        formPanel.add(gradeField);
        formPanel.add(new JLabel("Age:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("Filter by Grade:"));
        formPanel.add(gradeFilterBox);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton searchBtn = new JButton("Search");
        JButton deleteBtn = new JButton("Delete");
        JButton displayBtn = new JButton("Display All");
        JButton editBtn = new JButton("Edit");
        JButton sortBtn = new JButton("Sort by Roll Number");
        JButton themeBtn = new JButton("Toggle Dark Mode");

        // adding buttons
        buttonPanel.add(addBtn);
        buttonPanel.add(searchBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(displayBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(sortBtn);
        buttonPanel.add(themeBtn);

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        // Action Listeners
        addBtn.addActionListener(e -> add());
        searchBtn.addActionListener(e -> search());
        deleteBtn.addActionListener(e -> delete());
        displayBtn.addActionListener(e -> displayAll());
        editBtn.addActionListener(e -> edit());
        sortBtn.addActionListener(e -> displaySortedStudents());
        themeBtn.addActionListener(e -> toggleDarkMode());
        gradeFilterBox.addActionListener(e -> filterStudentsByGrade());

        // Live search on typing
        rollField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String roll = rollField.getText().trim();
                if (!roll.isEmpty()) {
                    Student s = manager.search(roll);
                    outputArea.setText(s != null ? s.toString() : "No student with that roll.");
                } else {
                    outputArea.setText("");
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void add() {
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String grade = gradeField.getText().trim();
        String ageText = ageField.getText().trim();

        if (name.isEmpty() || roll.isEmpty() || grade.isEmpty() || ageText.isEmpty()) {
            showMessage("Please fill all fields.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            if (manager.search(roll) != null) {
                showMessage("Roll number already exists. Use Edit to update.");
                return;
            }
            manager.add(new Student(name, roll, grade, age));
            showMessage("Student added successfully.");
            clearFields();
        } catch (NumberFormatException e) {
            showMessage("Invalid age.");
        }
    }

    private void search() {
        String roll = rollField.getText().trim();
        if (roll.isEmpty()) {
            showMessage("Enter roll number to search.");
            return;
        }
        Student s = manager.search(roll);
        outputArea.setText(s != null ? s.toString() : "Student not found.");
    }

    private void delete() {
        String roll = rollField.getText().trim();
        if (roll.isEmpty()) {
            showMessage("Enter roll number to delete.");
            return;
        }
        boolean removed = manager.remove(roll);
        showMessage(removed ? "Student deleted." : "Student not found.");
    }

    private void edit() {
        String roll = rollField.getText().trim();
        if (roll.isEmpty()) {
            showMessage("Enter roll number to edit.");
            return;
        }

        Student existing = manager.search(roll);
        if (existing == null) {
            showMessage("Student not found.");
            return;
        }

        String name = nameField.getText().trim();
        String grade = gradeField.getText().trim();
        String ageText = ageField.getText().trim();

        if (name.isEmpty() || grade.isEmpty() || ageText.isEmpty()) {
            showMessage("Fill all fields to update.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            existing.setName(name);
            existing.setGrade(grade);
            existing.setAge(age);
            manager.save();
            showMessage("Student updated successfully.");
            clearFields();
        } catch (NumberFormatException e) {
            showMessage("Invalid age.");
        }
    }

    private void displayAll() {
        outputArea.setText("");
        for (Student s : manager.getAll()) {
            outputArea.append(s + "\n");
        }
    }

    private void displaySortedStudents() {
        outputArea.setText("");
        for (Student s : manager.getSortedByRollNumber()) {
            outputArea.append(s + "\n");
        }
    }

    private void filterStudentsByGrade() {
        String selectedGrade = (String) gradeFilterBox.getSelectedItem();
        outputArea.setText("");
        if (selectedGrade.equals("All")) {
            displayAll();
        } else {
            for (Student s : manager.filterByGrade(selectedGrade)) {
                outputArea.append(s + "\n");
            }
        }
    }

    private void toggleDarkMode() {
        Color bg, fg;
        darkMode = !darkMode;

        if (darkMode) {
            bg = Color.DARK_GRAY;
            fg = Color.WHITE;
        } else {
            bg = Color.WHITE;
            fg = Color.BLACK;
        }

        getContentPane().setBackground(bg);
        for (Component c : getContentPane().getComponents()) {
            c.setBackground(bg);
            c.setForeground(fg);
            if (c instanceof JPanel) {
                for (Component inner : ((JPanel) c).getComponents()) {
                    inner.setBackground(bg);
                    inner.setForeground(fg);
                }
            }
        }
        outputArea.setBackground(bg);
        outputArea.setForeground(fg);
    }

    private void clearFields() {
        nameField.setText("");
        rollField.setText("");
        gradeField.setText("");
        ageField.setText("");
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    //Main method
    public static void main(String[] args) {
        new Student_Management_System();
    }
}

