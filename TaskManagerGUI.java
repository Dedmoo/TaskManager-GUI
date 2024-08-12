
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Task implements Serializable {
    private String name;
    private String description;
    private int priority;
    private boolean isCompleted;

    public Task(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.isCompleted = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void completeTask() {
        this.isCompleted = true;
    }

    public void editTask(String newName, String newDescription, int newPriority) {
        this.name = newName;
        this.description = newDescription;
        this.priority = newPriority;
    }

    @Override
    public String toString() {
        return name + " - " + (isCompleted ? "Completed" : "Pending");
    }
}

class TaskManager {
    private Map<String, LinkedList<Task>> categoryTasks;
    private TreeSet<Task> prioritizedTasks;

    public TaskManager() {
        this.categoryTasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparingInt(Task::getPriority));
    }

    public void addTask(String category, Task task) {
        categoryTasks.putIfAbsent(category, new LinkedList<>());
        categoryTasks.get(category).add(task);
        prioritizedTasks.add(task);
    }

    public void completeTask(Task task) {
        task.completeTask();
    }

    public void deleteTask(Task task) {
        prioritizedTasks.remove(task);
        for (LinkedList<Task> tasks : categoryTasks.values()) {
            tasks.remove(task);
        }
    }

    public List<Task> getTasksByCategory(String category) {
        return categoryTasks.getOrDefault(category, new LinkedList<>());
    }

    public List<Task> getTasksByPriority() {
        return new ArrayList<>(prioritizedTasks);
    }

    public List<Task> searchTasks(String query) {
        List<Task> results = new ArrayList<>();
        for (Task task : prioritizedTasks) {
            if (task.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(task);
            }
        }
        return results;
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (LinkedList<Task> tasks : categoryTasks.values()) {
            allTasks.addAll(tasks);
        }
        return allTasks;
    }

    public void saveTasks(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(categoryTasks);
            out.writeObject(prioritizedTasks);
        }
    }

    public void loadTasks(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            categoryTasks = (Map<String, LinkedList<Task>>) in.readObject();
            prioritizedTasks = (TreeSet<Task>) in.readObject();
        }
    }
}

public class TaskManagerGUI extends JFrame {
    private TaskManager taskManager;
    private JComboBox<String> categoryComboBox;
    private JTextField taskNameField;
    private JTextArea taskDescriptionField;
    private JSpinner prioritySpinner;
    private DefaultListModel<Task> taskListModel;
    private JTextField searchField;
    private JButton saveButton, loadButton, deleteButton, editButton;

    public TaskManagerGUI() {
        taskManager = new TaskManager();
        setTitle("Task Manager");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Task input panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        add(inputPanel, BorderLayout.NORTH);

        inputPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>(new String[]{"Work", "Personal", "Other"});
        inputPanel.add(categoryComboBox);

        inputPanel.add(new JLabel("Task Name:"));
        taskNameField = new JTextField();
        inputPanel.add(taskNameField);

        inputPanel.add(new JLabel("Task Description:"));
        taskDescriptionField = new JTextArea(3, 20);
        inputPanel.add(taskDescriptionField);

        inputPanel.add(new JLabel("Priority:"));
        prioritySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        inputPanel.add(prioritySpinner);

        JButton addButton = new JButton("Add Task");
        inputPanel.add(addButton);

        // Task list panel
        taskListModel = new DefaultListModel<>();
        JList<Task> taskList = new JList<>(taskListModel);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        add(southPanel, BorderLayout.SOUTH);

        deleteButton = new JButton("Delete Task");
        editButton = new JButton("Edit Task");
        JButton completeButton = new JButton("Complete Task");
        saveButton = new JButton("Save Tasks");
        loadButton = new JButton("Load Tasks");

        southPanel.add(deleteButton);
        southPanel.add(editButton);
        southPanel.add(completeButton);
        southPanel.add(saveButton);
        southPanel.add(loadButton);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        add(searchPanel, BorderLayout.SOUTH);

        searchPanel.add(new JLabel("Search Task:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Add task action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String category = (String) categoryComboBox.getSelectedItem();
                String taskName = taskNameField.getText();
                String taskDescription = taskDescriptionField.getText();
                int priority = (int) prioritySpinner.getValue();

                Task task = new Task(taskName, taskDescription, priority);
                taskManager.addTask(category, task);
                taskListModel.addElement(task);

                // Clear input fields
                taskNameField.setText("");
                taskDescriptionField.setText("");
                prioritySpinner.setValue(1);
            }
        });

        // Complete task action
        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    taskManager.completeTask(selectedTask);
                    taskList.repaint();
                }
            }
        });

        // Delete task action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    taskManager.deleteTask(selectedTask);
                    taskListModel.removeElement(selectedTask);
                }
            }
        });

        // Edit task action
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    String newName = JOptionPane.showInputDialog("New Task Name:", selectedTask.getName());
                    String newDescription = JOptionPane.showInputDialog("New Task Description:", selectedTask.getDescription());
                    int newPriority = Integer.parseInt(JOptionPane.showInputDialog("New Priority:", selectedTask.getPriority()));
                    selectedTask.editTask(newName, newDescription, newPriority);
                    taskList.repaint();
                }
            }
        });

        // Search task action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                List<Task> results = taskManager.searchTasks(query);
                taskListModel.clear();
                for (Task task : results) {
                    taskListModel.addElement(task);
                }
            }
        });

        // Save tasks action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    taskManager.saveTasks("tasks.ser");
                    JOptionPane.showMessageDialog(null, "Tasks saved successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error saving tasks!");
                }
            }
        });

        // Load tasks action
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    taskManager.loadTasks("tasks.ser");
                    taskListModel.clear();
                    for (Task task : taskManager.getAllTasks()) {
                        taskListModel.addElement(task);
                    }
                    JOptionPane.showMessageDialog(null, "Tasks loaded successfully!");
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading tasks!");
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TaskManagerGUI().setVisible(true);
            }
        });
    }
}
