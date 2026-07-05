import javax.swing.*;

/**
 * Main
 * ----
 * Entry point for the AI FAQ Chatbot application.
 * Wires together the FAQ knowledge base, the NLP/rule-based engine,
 * and the Swing GUI, then launches the window.
 *
 * Run with:
 *   javac *.java
 *   java Main
 */
public class Main {
    public static void main(String[] args) {
        // Use the system look-and-feel where available for a native feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back silently to the default cross-platform look and feel
        }

        FaqRepository repository = new FaqRepository("faqs_data.txt");
        ChatBotEngine engine = new ChatBotEngine(repository);

        SwingUtilities.invokeLater(() -> {
            ChatBotGUI gui = new ChatBotGUI(engine);
            gui.setVisible(true);
        });
    }
}
