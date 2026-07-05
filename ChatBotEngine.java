import java.time.LocalTime;
import java.util.*;

/**
 * ChatBotEngine
 * -------------
 * The "brain" of the chatbot. Combines two layers of logic:
 *
 *  LAYER 1 - Rule-based responses:
 *      Fast, deterministic handling of conversational staples (greetings,
 *      farewells, thanks, small talk, identity questions) using keyword
 *      rules over the NLP-normalized tokens.
 *
 *  LAYER 2 - NLP-driven FAQ retrieval ("machine learning style" matching):
 *      For anything else, the user's message is compared against every
 *      trained FAQ question using a weighted blend of:
 *          - Jaccard similarity (token set overlap)
 *          - Cosine similarity (term-frequency vectors)
 *          - Fuzzy token overlap (Levenshtein-tolerant, handles typos)
 *      The FAQ with the highest combined confidence score is returned,
 *      provided it clears a minimum confidence threshold. This is a classic
 *      retrieval-based conversational AI approach and improves automatically
 *      as more Q&A pairs are trained into the knowledge base.
 */
public class ChatBotEngine {

    private static final double CONFIDENCE_THRESHOLD = 0.30;

    private final FaqRepository repository;
    private String pendingTrainQuestion = null; // used by simple "teach me" conversational flow

    public ChatBotEngine(FaqRepository repository) {
        this.repository = repository;
    }

    /** Result of a single response computation: message + how it was derived. */
    public static class BotResponse {
        public final String message;
        public final String source;   // "rule", "faq", "fallback"
        public final double confidence;

        BotResponse(String message, String source, double confidence) {
            this.message = message;
            this.source = source;
            this.confidence = confidence;
        }
    }

    /** Main entry point: given raw user text, produce the bot's reply. */
    public BotResponse respond(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return new BotResponse("I didn't quite catch that -- could you type something?", "rule", 1.0);
        }

        List<String> tokens = NlpUtils.preprocess(userInput);

        // ---- Layer 1: rule-based conversational intents ----
        BotResponse ruleResponse = matchRules(tokens, userInput);
        if (ruleResponse != null) return ruleResponse;

        // ---- Layer 2: NLP FAQ retrieval ----
        return matchFaq(tokens);
    }

    private BotResponse matchRules(List<String> tokens, String raw) {
        Set<String> tokenSet = new HashSet<>(tokens);

        if (tokenSet.contains("greeting")) {
            String hour = timeGreeting();
            return new BotResponse(hour + " I'm your AI FAQ assistant. Ask me anything about our " +
                    "hours, pricing, account, or support -- or teach me something new!", "rule", 1.0);
        }
        if (tokenSet.contains("farewell")) {
            return new BotResponse("Goodbye! Feel free to come back anytime you have a question. 👋", "rule", 1.0);
        }
        if (tokenSet.contains("thankyou")) {
            return new BotResponse("You're very welcome! Is there anything else I can help with?", "rule", 1.0);
        }
        if (tokenSet.contains("how") || raw.toLowerCase().contains("how are you")) {
            if (raw.toLowerCase().contains("how are you")) {
                return new BotResponse("I'm running smoothly, thanks for asking! How can I help you today?", "rule", 1.0);
            }
        }
        if (raw.toLowerCase().matches(".*\\b(your name|who are you|what are you)\\b.*")) {
            return new BotResponse("I'm an AI-powered FAQ chatbot written in Java. You can ask me questions, " +
                    "and I'll match them against my trained knowledge base!", "rule", 1.0);
        }
        if (raw.toLowerCase().matches(".*\\b(help|what can you do)\\b.*") && tokens.size() <= 3) {
            return new BotResponse("I can answer common questions about hours, pricing, accounts, security, " +
                    "cancellations, and support. Try asking one, or use the 'Train the Bot' menu to teach me new answers!",
                    "rule", 1.0);
        }
        return null; // no rule matched -- fall through to FAQ retrieval
    }

    private String timeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning!";
        if (hour < 17) return "Good afternoon!";
        return "Good evening!";
    }

    private BotResponse matchFaq(List<String> queryTokens) {
        FaqRepository.FaqEntry best = null;
        double bestScore = -1;

        for (FaqRepository.FaqEntry entry : repository.getAll()) {
            double jaccard = NlpUtils.jaccardSimilarity(queryTokens, entry.tokens);
            double cosine = NlpUtils.cosineSimilarity(queryTokens, entry.tokens);
            double fuzzy = NlpUtils.fuzzyTokenOverlap(queryTokens, entry.tokens);

            // Weighted ensemble score -- blends exact set overlap, frequency-weighted
            // similarity, and typo-tolerant matching.
            double score = (0.4 * jaccard) + (0.35 * cosine) + (0.25 * fuzzy);

            if (score > bestScore) {
                bestScore = score;
                best = entry;
            }
        }

        if (best != null && bestScore >= CONFIDENCE_THRESHOLD) {
            return new BotResponse(best.answer, "faq", bestScore);
        }

        return new BotResponse(
                "I'm not fully sure how to answer that yet. Could you rephrase, or would you like to " +
                "teach me the answer using the \"Train the Bot\" option in the Bot menu?",
                "fallback", bestScore < 0 ? 0 : bestScore);
    }

    public FaqRepository getRepository() {
        return repository;
    }
}
