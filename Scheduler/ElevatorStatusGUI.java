package SYSC3303Project.Scheduler;

import SYSC3303Project.Elevator.Direction;
import SYSC3303Project.Elevator.ElevatorStatus;
import SYSC3303Project.SharedDataImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

public class ElevatorStatusGUI extends JFrame {
    private final Map<Integer, ElevatorStatus> elevatorStatusMap = new HashMap<>();
    private final JLabel[] floorLabels = new JLabel[4];
    private final JLabel[] directionLabels = new JLabel[4];
    private SchedulerSubsystem schedulerSubsystem;

    private Icon upArrow;
    private Icon downArrow;
    private Icon elevatorImage;


    public ElevatorStatusGUI() throws Exception {
        initializeSchedulerSubsystem();
        setTitle("Elevator Status");
        setSize(800, 300); // Adjust size as needed
        setResizable(false); // Prevent resizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 4)); // 1 row, 4 elevators

        // Load and resize arrow images
        try {
            upArrow = new ImageIcon(resizeImage(ImageIO.read(getClass().getResource("../Resources/UpArrow.png")), 20, 20));
            downArrow = new ImageIcon(resizeImage(ImageIO.read(getClass().getResource("../Resources/DownArrow.png")), 20, 20));
            elevatorImage = new ImageIcon(resizeImage(ImageIO.read(getClass().getResource("../Resources/Elevator.png")), 200, 200)); // Adjust size as needed

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a border for the label panel
        Border labelPanelBorder = BorderFactory.createLineBorder(Color.BLACK);
        // Initialize labels for each elevator
        for (int i = 0; i < 4; i++) {
            // Main container panel for each elevator
            JPanel containerPanel = new JPanel(new BorderLayout());

            // Panel for holding the floor and direction labels
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Use FlowLayout for horizontal alignment
            labelPanel.setBorder(labelPanelBorder); // Set the border here

            // Floor label
            floorLabels[i] = new JLabel("--");
            floorLabels[i].setFont(new Font("Serif", Font.BOLD, 20)); // Set the font size to 20 and make it bold
            floorLabels[i].setForeground(Color.RED); // Set the font color to red
            labelPanel.add(floorLabels[i]);

            // Direction label
            directionLabels[i] = new JLabel();
            labelPanel.add(directionLabels[i]);

            // Add the label panel to the container panel, above the elevator panel
            containerPanel.add(labelPanel, BorderLayout.NORTH);

            // Label to hold the elevator image
            JLabel elevatorImageLabel = new JLabel(elevatorImage);
            elevatorImageLabel.setHorizontalAlignment(JLabel.CENTER);

            // Elevator panel with a titled border
            JPanel elevatorPanel = new JPanel();
            elevatorPanel.setBorder(BorderFactory.createTitledBorder("Elevator " + (2 * i + 10)));

            // Adding the elevator image label to the container panel
            elevatorPanel.add(elevatorImageLabel, BorderLayout.CENTER);

            containerPanel.add(elevatorPanel, BorderLayout.CENTER);

            add(containerPanel);
        }

        schedulerSubsystem.setObserver(this);
    }

    private Image resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        return resultingImage;
    }

    private void initializeSchedulerSubsystem() throws Exception {
        SharedDataImpl sharedData = new SharedDataImpl();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("SharedData", sharedData);

        schedulerSubsystem = new SchedulerSubsystem(sharedData);
        Thread schedulerThread = new Thread(schedulerSubsystem);
        schedulerThread.start();
    }

    public void updateElevatorStatuses(Map<Integer, ElevatorStatus> elevatorStatusMap) {
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<Integer, ElevatorStatus> entry : elevatorStatusMap.entrySet()) {
                ElevatorStatus status = entry.getValue();
                int index = entry.getKey();
                floorLabels[(index-10)/2].setText(String.valueOf(status.getCurrentFloor()));

                // Set direction label using Unicode arrows
                if (status.getDirection() == Direction.UP) {
                    directionLabels[(index-10)/2].setIcon(upArrow); // Unicode for upward arrow
                } else if (status.getDirection() == Direction.DOWN) {
                    directionLabels[(index-10)/2].setIcon(downArrow); // Unicode for downward arrow
                } else {
                    directionLabels[(index-10)/2].setText(""); // stationary
                }
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ElevatorStatusGUI gui = new ElevatorStatusGUI();
                gui.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}

