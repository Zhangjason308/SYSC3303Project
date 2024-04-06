package SYSC3303Project.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class View extends JFrame {
    private JPanel[] elevators;

    public View() {
        setTitle("Elevators");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);

        // Create a layout manager for the frame
        setLayout(new GridLayout(1, 4)); // 1 row and 4 columns

        // Create elevator panels
        elevators = new JPanel[4]; // 4 elevators

        for (int i = 0; i < 4; i++) {
            // Create elevator panel
            JPanel elevatorPanel = new JPanel();
            elevatorPanel.setLayout(null); // Use null layout for precise positioning
            elevatorPanel.setPreferredSize(new Dimension(100, 150)); // Set preferred size (taller vertically)

            // Load image representing elevator
            ImageIcon icon = createImageIcon("elevator.png"); // Provide the path to your image file
            if (icon != null) {
                JLabel imageLabel = new JLabel(icon);
                imageLabel.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
                elevatorPanel.add(imageLabel);

                // Create a new flow layout with a small horizontal gap
                FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 5, 0); // Center align, 5px gap, no vertical gap
                JPanel labelPanel = new JPanel(flowLayout);

                JLabel numberLabel = new JLabel("1", SwingConstants.CENTER);
                numberLabel.setForeground(Color.BLACK); // Set label color

                JLabel directionLabel = new JLabel("Up", SwingConstants.CENTER);
                directionLabel.setForeground(Color.BLACK); // Set label color

                // Add labels to the label panel
                labelPanel.add(numberLabel);
                labelPanel.add(directionLabel);

                // Add the label panel to the elevator panel, positioned below the image
                labelPanel.setBounds(0, imageLabel.getHeight(), 100, 50); // Adjust height as needed
                elevatorPanel.add(labelPanel);
            } else {
                // Add a placeholder label or handle the missing image in another way
                JLabel errorLabel = new JLabel("Error: Could not find elevator image");
                elevatorPanel.add(errorLabel);
            }

            // Add elevator panel to the frame
            add(elevatorPanel);

            // Add elevator panel to the elevators array
            elevators[i] = elevatorPanel;
        }

        // Make the frame visible
        setVisible(true);
    }

    // Method to load image icon
    protected ImageIcon createImageIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void main(String[] args) {
        // Create an instance of the View class
        SwingUtilities.invokeLater(View::new);
    }
}
