import java.util.*;

/**
 * NlpUtils
 * ---------
 * Lightweight, dependency-free NLP toolkit used by the chatbot engine.
 *
 * Techniques implemented (no external NLP libraries required, so the
 * project compiles with plain javac):
 *   1. Tokenization              - split raw text into normalized word tokens
 *   2. Stop-word removal         - drop low-information filler words
 *   3. Simple suffix stemming    - crude Porter-style stemming (ing/ed/es/s ...)
 *   4. Synonym normalization     - map common synonyms to one canonical token
 *   5. Jaccard similarity        - set-overlap similarity between token sets
 *   6. Cosine similarity (TF)    - term-frequency vector similarity
 *   7. Levenshtein distance      - edit-distance based fuzzy word matching
 *      (used to tolerate small typos, e.g. "pasword" -> "password")
 */
public final class NlpUtils {

    private NlpUtils() {}

    /** Common English stop-words that carry little topical meaning. */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "am", "was", "were", "be", "been", "being",
            "do", "does", "did", "doing", "have", "has", "had", "having",
            "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "its", "our", "their",
            "to", "of", "in", "on", "at", "for", "with", "about", "against", "between",
            "into", "through", "during", "before", "after", "above", "below",
            "up", "down", "out", "off", "over", "under", "again", "further",
            "and", "or", "but", "if", "so", "than", "too", "very", "just",
            "can", "could", "should", "would", "will", "shall", "may", "might", "must",
            "this", "that", "these", "those", "there", "here", "what", "which", "who",
            "whom", "how", "please", "kindly"
    ));

    /** Synonym map: many surface forms collapse to one canonical token,
     *  which lets the matcher recognize intent even with different wording. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        put("hi", "greeting"); put("hello", "greeting"); put("hey", "greeting");
        put("howdy", "greeting"); put("yo", "greeting"); put("greetings", "greeting");

        put("bye", "farewell"); put("goodbye", "farewell"); put("seeya", "farewell");
        put("later", "farewell"); put("exit", "farewell"); put("quit", "farewell");

        put("thanks", "thankyou"); put("thank", "thankyou"); put("thx", "thankyou");
        put("appreciate", "thankyou");

        put("cost", "price"); put("costs", "price"); put("pricing", "price");
        put("fee", "price"); put("fees", "price"); put("charge", "price");

        put("hours", "schedule"); put("timing", "schedule"); put("timings", "schedule");
        put("open", "schedule"); put("opening", "schedule");

        put("cancel", "cancellation"); put("canceling", "cancellation");
        put("cancelling", "cancellation"); put("refund", "cancellation");

        put("password", "password"); put("pasword", "password"); put("pwd", "password");
        put("login", "account"); put("signin", "account"); put("signup", "account");
        put("register", "account");
    }
    private static void put(String k, String v) { SYNONYMS.put(k, v); }

    /** Split raw input into lowercase word tokens, stripping punctuation. */
    public static List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9'\\s]", " ");
        String[] rawTokens = cleaned.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (t.isEmpty()) continue;
            tokens.add(t);
        }
        return tokens;
    }

    /** Full preprocessing pipeline: tokenize -> stop-word removal -> synonym
     *  normalization -> stemming. Returns the resulting "concept" tokens. */
    public static List<String> preprocess(String text) {
        List<String> out = new ArrayList<>();
        for (String tok : tokenize(text)) {
            if (STOP_WORDS.contains(tok)) continue;
            if (SYNONYMS.containsKey(tok)) {
                // Already a canonical synonym token (e.g. "hi" -> "greeting") --
                // do NOT stem it further, or "greeting" would wrongly become "greet".
                out.add(SYNONYMS.get(tok));
            } else {
                out.add(stem(tok));
            }
        }
        return out;
    }

    /** Very small rule-based stemmer (not linguistically perfect, but good
     *  enough to unify plurals / verb forms for keyword matching). */
    public static String stem(String word) {
        if (word.length() > 4) {
            if (word.endsWith("ing")) return word.substring(0, word.length() - 3);
            if (word.endsWith("edly")) return word.substring(0, word.length() - 4);
        }
        if (word.length() > 3) {
            if (word.endsWith("ed")) return word.substring(0, word.length() - 2);
            if (word.endsWith("es")) return word.substring(0, word.length() - 2);
        }
        if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /** Jaccard similarity between two token collections: |A∩B| / |A∪B|. */
    public static double jaccardSimilarity(Collection<String> a, Collection<String> b) {
        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);
        if (setA.isEmpty() && setB.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    /** Cosine similarity using simple term-frequency vectors. */
    public static double cosineSimilarity(List<String> a, List<String> b) {
        Map<String, Integer> freqA = termFrequency(a);
        Map<String, Integer> freqB = termFrequency(b);
        Set<String> vocab = new HashSet<>(freqA.keySet());
        vocab.addAll(freqB.keySet());
        if (vocab.isEmpty()) return 0.0;

        double dot = 0, magA = 0, magB = 0;
        for (String term : vocab) {
            int fa = freqA.getOrDefault(term, 0);
            int fb = freqB.getOrDefault(term, 0);
            dot += fa * fb;
            magA += fa * (double) fa;
            magB += fb * (double) fb;
        }
        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    private static Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) freq.merge(t, 1, Integer::sum);
        return freq;
    }

    /** Classic Levenshtein (edit distance) between two strings. */
    public static int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                                     dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    /** Normalized fuzzy word-overlap score in [0,1], tolerating small typos.
     *  For each token in `query`, finds the best-matching token in `target`
     *  (exact or within edit-distance tolerance) and averages the scores. */
    public static double fuzzyTokenOverlap(List<String> query, List<String> target) {
        if (query.isEmpty() || target.isEmpty()) return 0.0;
        double total = 0.0;
        for (String q : query) {
            double best = 0.0;
            for (String t : target) {
                if (q.equals(t)) { best = 1.0; break; }
                int dist = levenshtein(q, t);
                int maxLen = Math.max(q.length(), t.length());
                double score = maxLen == 0 ? 0.0 : 1.0 - ((double) dist / maxLen);
                // only count as a fuzzy match if reasonably close (tolerate ~1-2 typos)
                if (score > 0.72) best = Math.max(best, score);
            }
            total += best;
        }
        return total / query.size();
    }
}
