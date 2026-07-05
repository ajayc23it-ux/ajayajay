import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * FaqRepository
 * -------------
 * Stores the chatbot's "trained" knowledge base: a growing list of
 * (question, answer, category) entries loaded from / persisted to a plain
 * text file (pipe-delimited: question|answer|category).
 *
 * This is the "training data" for the rule-based / retrieval-based model:
 * new Q&A pairs added at runtime via the GUI's "Train the Bot" dialog are
 * appended to this file, so the bot remembers them across restarts.
 */
public class FaqRepository {

    public static class FaqEntry {
        public final String question;
        public final String answer;
        public final String category;
        public final List<String> tokens; // pre-processed tokens, cached for speed

        public FaqEntry(String question, String answer, String category) {
            this.question = question;
            this.answer = answer;
            this.category = category;
            this.tokens = NlpUtils.preprocess(question);
        }
    }

    private final Path dataFile;
    private final List<FaqEntry> entries = new ArrayList<>();

    public FaqRepository(String filePath) {
        this.dataFile = Paths.get(filePath);
        load();
        if (entries.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    public List<FaqEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    /** Loads Q&A pairs from disk; each non-comment line is: question|answer|category */
    private void load() {
        entries.clear();
        if (!Files.exists(dataFile)) return;
        try (BufferedReader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length >= 2) {
                    String category = parts.length == 3 ? parts[2].trim() : "general";
                    entries.add(new FaqEntry(parts[0].trim(), parts[1].trim(), category));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load FAQ data file: " + e.getMessage());
        }
    }

    /** Persists the current in-memory FAQ list back to disk. */
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write("# AI Chatbot knowledge base -- format: question|answer|category\n");
            writer.write("# Add lines here (or use the GUI 'Train the Bot' option) to teach the bot.\n");
            for (FaqEntry e : entries) {
                writer.write(escapePipe(e.question) + "|" + escapePipe(e.answer) + "|" + e.category);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save FAQ data file: " + e.getMessage());
        }
    }

    private String escapePipe(String s) {
        return s.replace("|", "/");
    }

    /** Adds a brand-new trained Q&A pair and persists it immediately. */
    public void addEntry(String question, String answer, String category) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) return;
        entries.add(new FaqEntry(question.trim(), answer.trim(),
                (category == null || category.isBlank()) ? "general" : category.trim()));
        save();
    }

    public int size() {
        return entries.size();
    }

    /** Seeds a starter knowledge base so the bot is useful out-of-the-box.
     *  Users can edit faqs_data.txt directly, or train the bot live via the GUI. */
    private void seedDefaults() {
        addEntry("What are your business hours?",
                "We're open Monday to Friday, 9 AM to 6 PM, and Saturday 10 AM to 2 PM. We're closed on Sundays.",
                "hours");
        addEntry("What is the cost of your service?",
                "Our standard plan starts at $19/month. We also offer a free tier with limited features. Would you like pricing details for a specific plan?",
                "pricing");
        addEntry("How can I reset my password?",
                "You can reset your password from the Login page by clicking 'Forgot Password' and following the emailed instructions.",
                "account");
        addEntry("How do I create an account?",
                "Click 'Sign Up' on the homepage, enter your email and a password, then verify your email address to activate your account.",
                "account");
        addEntry("What is your cancellation and refund policy?",
                "You can cancel anytime from your account settings. Refunds are provided in full within 14 days of purchase, no questions asked.",
                "policy");
        addEntry("How do I contact customer support?",
                "You can reach our support team via email at support@example.com or through the live chat option on our website, 24/7.",
                "support");
        addEntry("Where are you located?",
                "Our headquarters is located in Chennai, Tamil Nadu, India, with remote support teams worldwide.",
                "general");
        addEntry("Do you offer a free trial?",
                "Yes! We offer a 14-day free trial with full access to all premium features, no credit card required.",
                "pricing");
        addEntry("What payment methods do you accept?",
                "We accept all major credit/debit cards, UPI, PayPal, and net banking.",
                "pricing");
        addEntry("Is my data secure with you?",
                "Absolutely. We use industry-standard AES-256 encryption at rest and TLS in transit, and we never sell your data.",
                "security");
        addEntry("Can I upgrade or downgrade my plan?",
                "Yes, you can change your plan anytime from Account Settings > Billing. Changes take effect at the next billing cycle.",
                "pricing");
        addEntry("What is this chatbot?",
                "I'm an AI-powered FAQ assistant built in Java. I use NLP techniques to understand your question and match it to the best answer I know!",
                "about");
        addEntry("Who created you?",
                "I was built as a Java-based AI chatbot project, combining rule-based responses with NLP-driven FAQ matching.",
                "about");
    }
}
