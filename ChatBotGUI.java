import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.text.*;

/**
 * ChatBotGUI
 * ----------
 * Swing-based real-time chat interface for the AI FAQ chatbot.
 * Features:
 *   - Scrollable chat transcript with styled, colored message bubbles
 *   - Text input with Enter-to-send and a Send button
 *   - Menu bar to train new Q&A pairs live, view the knowledge base,
 *     and clear the conversation
 *   - Automatically greets the user on launch
 */
public class ChatBotGUI extends JFrame {

    private final ChatBotEngine engine;
    private final JTextPane chatPane;
    private final StyledDocument doc;
    private final JTextField inputField;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    private static final Color BG = new Color(0x1E1F26);
    private static final Color PANEL_BG = new Color(0x2A2C38);
    private static final Color USER_BUBBLE = new Color(0x3A7BFF);
    private static final Color BOT_BUBBLE = new Color(0x3A3D4D);
    private static final Color TEXT_LIGHT = new Color(0xF2F2F5);
    private static final Color ACCENT = new Color(0x4CC9F0);

    public ChatBotGUI(ChatBotEngine engine) {
        super("AI FAQ Chatbot - Java NLP Assistant");
        this.engine = engine;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 720);
        setMinimumSize(new Dimension(420, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(buildHeader(), BorderLayout.NORTH);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG);
        chatPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        doc = chatPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        add(buildInputPanel(), BorderLayout.SOUTH);

        // Enter key sends the message
        inputField.addActionListener(e -> sendUserMessage());

        // Greet the user automatically on startup
        SwingUtilities.invokeLater(() -> {
            ChatBotEngine.BotResponse greet = engine.respond("hello");
            appendMessage("Bot", greet.message, false);
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("🤖  AI FAQ Chatbot");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel("Java  •  NLP-driven  •  Trainable knowledge base");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(ACCENT);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBackground(new Color(0x3A3D4D));
        inputField.setForeground(TEXT_LIGHT);
        inputField.setCaretColor(TEXT_LIGHT);
        inputField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setBackground(USER_BUBBLE);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendButton.addActionListener(e -> sendUserMessage());

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu botMenu = new JMenu("Bot");
        JMenuItem trainItem = new JMenuItem("Train the Bot (Add Q&A)...");
        trainItem.addActionListener(e -> openTrainDialog());
        JMenuItem viewFaqsItem = new JMenuItem("View Knowledge Base");
        viewFaqsItem.addActionListener(e -> openFaqViewer());
        JMenuItem clearItem = new JMenuItem("Clear Chat");
        clearItem.addActionListener(e -> clearChat());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        botMenu.add(trainItem);
        botMenu.add(viewFaqsItem);
        botMenu.addSeparator();
        botMenu.add(clearItem);
        botMenu.addSeparator();
        botMenu.add(exitItem);

        menuBar.add(botMenu);
        return menuBar;
    }

    private void sendUserMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        appendMessage("You", text, true);
        inputField.setText("");

        // Simulate a brief "thinking" delay for a more natural feel, then respond.
        Timer timer = new Timer(350, e -> {
            ChatBotEngine.BotResponse response = engine.respond(text);
            appendMessage("Bot", response.message, false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Appends a styled chat bubble line to the transcript. */
    private void appendMessage(String sender, String message, boolean isUser) {
        try {
            String time = LocalTime.now().format(timeFormat);

            SimpleAttributeSet nameStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(nameStyle, isUser ? USER_BUBBLE : ACCENT);
            StyleConstants.setBold(nameStyle, true);
            StyleConstants.setFontSize(nameStyle, 13);
            StyleConstants.setAlignment(nameStyle, isUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(bodyStyle, TEXT_LIGHT);
            StyleConstants.setFontSize(bodyStyle, 14);
            StyleConstants.setAlignment(bodyStyle, isUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            StyleConstants.setSpaceBelow(bodyStyle, 12);

            doc.setParagraphAttributes(doc.getLength(), 0, nameStyle, false);
            doc.insertString(doc.getLength(), sender + "  ·  " + time + "\n", nameStyle);

            int bodyStart = doc.getLength();
            doc.insertString(doc.getLength(), message + "\n\n", bodyStyle);
            doc.setParagraphAttributes(bodyStart, message.length() + 2, bodyStyle, false);

            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void clearChat() {
        chatPane.setText("");
    }

    /** Dialog that lets a user teach the bot a brand-new question/answer pair. */
    private void openTrainDialog() {
        JTextField questionField = new JTextField();
        JTextField answerField = new JTextField();
        JTextField categoryField = new JTextField("general");

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Question the bot should recognize:"));
        panel.add(questionField);
        panel.add(new JLabel("Answer it should give:"));
        panel.add(answerField);
        panel.add(new JLabel("Category (optional):"));
        panel.add(categoryField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Train the Bot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String q = questionField.getText().trim();
            String a = answerField.getText().trim();
            String c = categoryField.getText().trim();
            if (q.isEmpty() || a.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both a question and an answer are required.",
                        "Missing information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            engine.getRepository().addEntry(q, a, c);
            appendMessage("Bot", "Thanks! I've learned a new answer for: \"" + q + "\"", false);
        }
    }

    private void openFaqViewer() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (FaqRepository.FaqEntry entry : engine.getRepository().getAll()) {
            sb.append(i++).append(". [").append(entry.category).append("] ")
              .append(entry.question).append("\n   -> ").append(entry.answer).append("\n\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 420));
        JOptionPane.showMessageDialog(this, scroll, "Trained Knowledge Base (" + engine.getRepository().size() + " entries)",
                JOptionPane.PLAIN_MESSAGE);
    }
}
