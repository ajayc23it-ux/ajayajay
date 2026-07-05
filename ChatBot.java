import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatBot extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    ChatBotEngine bot = new ChatBotEngine();

    public ChatBot() {

        setTitle("Artificial Intelligence ChatBot");
        setSize(500,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial",Font.PLAIN,15));

        JScrollPane scroll = new JScrollPane(chatArea);

        JPanel bottom = new JPanel(new BorderLayout());

        inputField = new JTextField();

        sendButton = new JButton("Send");

        bottom.add(inputField,BorderLayout.CENTER);
        bottom.add(sendButton,BorderLayout.EAST);

        add(scroll,BorderLayout.CENTER);
        add(bottom,BorderLayout.SOUTH);

        chatArea.append("Bot : Hello! I am AI ChatBot.\n");
        chatArea.append("Bot : Ask me anything.\n\n");

        sendButton.addActionListener(e -> sendMessage());

        inputField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void sendMessage(){

        String user = inputField.getText();

        if(user.isEmpty())
            return;

        chatArea.append("You : " + user + "\n");

        String reply = bot.getResponse(user);

        chatArea.append("Bot : " + reply + "\n\n");

        inputField.setText("");
    }

    public static void main(String args[]){

        SwingUtilities.invokeLater(() -> new ChatBot());

    }

}
import java.util.*;

public class ChatBotEngine {

    private HashMap<String,String> faq;

    public ChatBotEngine(){

        faq = new HashMap<>();

        trainBot();

    }

    private void trainBot(){

        faq.put("hello","Hello! Nice to meet you.");
        faq.put("hi","Hi! How can I help?");
        faq.put("good morning","Good Morning!");
        faq.put("good evening","Good Evening!");

        faq.put("your name","I am an AI ChatBot.");
        faq.put("who are you","I am an intelligent chatbot written in Java.");

        faq.put("java","Java is an object-oriented programming language.");

        faq.put("ai","Artificial Intelligence enables machines to think and learn.");

        faq.put("nlp","Natural Language Processing helps computers understand human language.");

        faq.put("machine learning","Machine Learning allows systems to improve from experience.");

        faq.put("bye","Goodbye! Have a nice day.");

        faq.put("thank","You're Welcome!");

        faq.put("how are you","I'm doing great.");

    }

    public String getResponse(String input){

        input = preprocess(input);

        for(String key : faq.keySet()){

            if(input.contains(key))
                return faq.get(key);

        }

        return intelligentReply(input);

    }

    private String preprocess(String text){

        text = text.toLowerCase();

        text = text.replaceAll("[^a-z ]","");

        return text.trim();

    }

    private String intelligentReply(String text){

        if(text.contains("weather"))
            return "I cannot check live weather yet.";

        if(text.contains("time"))
            return "Please check your system clock.";

        if(text.contains("college"))
            return "I can answer only trained questions.";

        if(text.contains("course"))
            return "I can help with Java, AI, NLP and programming.";

        if(text.contains("help"))
            return "You can ask about Java, AI, NLP, Machine Learning and greetings.";

        return "Sorry, I don't understand. Please ask another question.";

    }

}