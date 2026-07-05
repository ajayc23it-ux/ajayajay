import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ChatBot extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private Map<String, String> knowledgeBase;

    public ChatBot() {
        // Set up window
        super("Java ChatBot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null); // center on screen

        // Create chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Create input field
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));

        // Add components to frame
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        // Load bot responses
        loadKnowledgeBase();

        // Handle user input
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userText = inputField.getText().trim();
                chatArea.append("You: " + userText + "\n");
                inputField.setText("");
                String botReply = getResponse(userText.toLowerCase());
                chatArea.append("Bot: " + botReply + "\n");
            }
        });

        setVisible(true); // show frame after everything is added
    }

    private void loadKnowledgeBase() {
        knowledgeBase = new HashMap<>();
        knowledgeBase.put("hi", "Hello! How can I assist you?");
        knowledgeBase.put("hello", "Hi there! Need help?");
        knowledgeBase.put("how are you", "I'm just code, but I'm functioning perfectly!");
        knowledgeBase.put("what is your name", "I'm a Java chatbot.");
        knowledgeBase.put("bye", "Goodbye! Have a great day!");
        knowledgeBase.put("help", "Try asking about Java, AI, or my name.");
        knowledgeBase.put("what is java", "Java is a high-level, object-oriented programming language.");
        knowledgeBase.put("who created java", "James Gosling created Java at Sun Microsystems.");
        knowledgeBase.put("what is ai", "AI stands for Artificial Intelligence.");
    }

    private String getResponse(String input) {
        for (String key : knowledgeBase.keySet()) {
            if (input.contains(key)) {
                return knowledgeBase.get(key);
            }
        }
        return "I'm sorry, I don't understand that.";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatBot());
    }
}