import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

public class WordScrambleGame extends JFrame implements ActionListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WordScrambleGame();
        });
    }
    private JLabel wordLabel = new JLabel();
    private JButton submitButton;
    private JTextField inputField;
    private JLabel scoreLabel;
    private JLabel categoryLabel;
    private JLabel hintsLeftLabel;
    private JLabel timeLabel;
    private JLabel livesLabel;
    private int hintsLeft = 3;
    private int score = 0;
    private int timeLeft = 20;
    private int lives = 3;
    private String currentWord;
    private String currentCategory;

    private String difficulty = "easy";
    private final Map<String, Map<String, String>> wordsWithHints = new HashMap<>();

    private Set<String> usedWords = new HashSet<>();

    private JPanel buttonPanel;
    private JButton hintButton;

    private Timer timer;

    public WordScrambleGame() {
        setTitle("Word Scramble Game");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        JPanel panel = new JPanel(new BorderLayout());
    
        JLabel headerLabel = new JLabel("Word Scramble Game");
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 50));
        headerLabel.setForeground(Color.green);
        panel.add(headerLabel, BorderLayout.NORTH);
    
        JButton startButton = new JButton("\u27A1\ufe0f START GAME");
        startButton.setPreferredSize(new Dimension(390,130));
        startButton.setFont(new Font("BOLD", Font.BOLD, 28));
        startButton.addActionListener(e -> {
            startGame();
            startButton.setEnabled(false);
        });
        startButton.setBackground(Color.blue);
        startButton.setForeground(Color.green);
        startButton.setBorder(BorderFactory.createEmptyBorder());
        startButton.setFocusPainted(false);
    
        JButton exitButton = new JButton("\uD83d\udd34 EXIT GAME");
        exitButton.setPreferredSize(new Dimension(390, 130));
        exitButton.setFont(new Font("BOLD", Font.BOLD, 28));
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit the game?", "Exit Game", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        exitButton.setBackground(Color.DARK_GRAY);
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
    
        JButton soundSettingButton = new JButton("\uD83C\uDFB5 VOLUME"); // 🎵 OPTION
        soundSettingButton.setPreferredSize(new Dimension(390, 130));
        soundSettingButton.setFont(new Font("PLAIN", Font.BOLD, 28));
        soundSettingButton.addActionListener(e -> {
            showSoundSettingDialog();
        });
        soundSettingButton.setBackground(Color.blue);
        soundSettingButton.setForeground(Color.WHITE);
        soundSettingButton.setBorder(BorderFactory.createEmptyBorder());
        soundSettingButton.setFocusPainted(false);
    
        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(soundSettingButton); 
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, BorderLayout.CENTER);
    
        add(panel);
    
        setLocationRelativeTo(null);
    
        setVisible(true);
    
        fillWordHintMaps();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        handleSubmit();
    }

    private void fillWordHintMaps() {
        Map<String, String> easyFruits = new HashMap<>();
        easyFruits.put("watermelon", "Its Green outside and Red inside.");
        easyFruits.put("pineapple", "It's a tropical plant with stiff spiny sword-shaped leaves");
        easyFruits.put("avocado", "Its rich of source of vitamin C and consuming them may reduce the risk of hear.");
        easyFruits.put("guava", "Its a green fruit and when ripe it is pink .");
        easyFruits.put("strawberry", "Like heart-shaped treasures of summer, offering a juicy, sweet-tart flavor.");
        easyFruits.put("apple", "It's a fruit and it's red or green.");
        easyFruits.put("banana", "It's a long yellow fruit.");
        easyFruits.put("orange", "It's a citrus fruit.");
        easyFruits.put("grape", "It's a small round fruit often used to make wine.");
        easyFruits.put("cherries", "It's a small color red with a seed in the middle it taste slightly sour.");

        Map<String, String> easyAnimals = new HashMap<>();
        easyAnimals.put("lion", "The king of the jungle");
        easyAnimals.put("gorilla", "Its big and strongest and have a muscle");
        easyAnimals.put("tiger", "Regal predators of the ocean.");
        easyAnimals.put("grizzlybear", "Dominant lords of the wilderness.");
        easyAnimals.put("shark", "Dominant predators of the ocean.");
        easyAnimals.put("dog", "It's a common pet known for its loyalty.");
        easyAnimals.put("cat", "It's a furry animal often kept as a pet.");
        easyAnimals.put("zebra", "It's an animal with black and white stripes.");
        easyAnimals.put("snake", "It's a long reptile without legs.");
        easyAnimals.put("bird", "It's a feathered animal that can fly.");

        Map<String, String> mediumWords = new HashMap<>();
        mediumWords.put("greece", "Renowned for its ancient history, mythology, stunning Mediterranean landscapes, and contributions to philosophy, democracy, and architecture..");
        mediumWords.put("nigeria", "Renowned for its diverse cultures, Nollywood film industry, and music..");
        mediumWords.put("south korea", "Known for its technological advancements, pop culture, and cuisine like kimchi..");
        mediumWords.put("spain", "Famous for its fiestas, flamenco dancing, delicious cuisine, and historical sites..");  
        mediumWords.put("south africa", "Renowned for its diverse landscapes, wildlife, and cultural heritage. .");
        mediumWords.put("italy", " Famous for its rich history, art, architecture, and delicious cuisine. .");
        mediumWords.put("australia", "Renowned for its stunning landscapes, wildlife, and outdoor lifestyle. .");
        mediumWords.put("usa", "Known for its diverse culture, innovation, and economic power .");
        mediumWords.put("egypt", " Renowned for its ancient civilization, iconic pyramids, and rich history. .");
        mediumWords.put("argentina", " Known for its tango dancing, beef cuisine, and passion for football. .");
        mediumWords.put("philippines", "Asia's pearl of the ocient for the richness of its culture and the beuty of its lanscape .");
        mediumWords.put("canada", " Known for its natural beauty, friendly people, and multicultural society.");
        mediumWords.put("mexico", "Known for its vibrant culture, ancient civilizations, and flavorful cuisine..");
        mediumWords.put("france", "Renowned for its art, cuisine, fashion, and iconic landmarks like the Eiffel Tower..");
        mediumWords.put("germany", "Known for its engineering prowess, beer culture, and historical significance..");
        mediumWords.put("china", "Famous for its rich history, rapid economic growth, and cultural heritage.");
        mediumWords.put("japan", "Famous for its technological advancements, traditional culture, and cuisine like sushi..");
        mediumWords.put("india", " Known for its ancient civilization, diverse culture, and Bollywood film industry..");
        mediumWords.put("brazil", " Renowned for its vibrant culture, Amazon Rainforest, and passion for football..");
        mediumWords.put("russia", "Known for its vast land area, rich history, and influence on global politics.");

        //hardwords for hints and scrambled word
        Map<String, String> hardWords = new HashMap<>();
        hardWords.put("version control", " A system that tracks and manages changes to source code over time, allowing multiple developers to collaborate on a project.");
        hardWords.put("exception", "An event that disrupts the normal flow of a program's execution, often due to errors or unexpected conditions..");
        hardWords.put("data structure", "A way of organizing and storing data in a computer's memory, such as arrays, lists, trees, or graphs..");
        hardWords.put("recursion", " A programming technique where a function calls itself directly or indirectly in order to solve a problem");
        hardWords.put("conditional Statement", "A programming construct that performs different actions depending on whether a certain condition evaluates to true or false.");
        hardWords.put("serialization", " The process of converting an object into a format that can be easily stored, transmitted, or reconstructed later.");
        hardWords.put("byte", "A unit of digital information consisting of 8 bits, often used to represent a single character or data element..");
        hardWords.put("bit", "The smallest unit of data in a computer, representing either a 0 or a 1 in binary notation..");
        hardWords.put("framework", "A pre-written set of reusable code libraries, tools, and components that provides a foundation for developing software applications..");
        hardWords.put("compilation", "he process of translating source code written in a high-level programming language into machine code or bytecode that can be executed by a computer..");
        hardWords.put("semantic:", "The meaning or interpretation of the symbols and words in a programming language, determining the behavior of the program");
        hardWords.put("method", "It's a set of code which is referred to by name and can be called at any point in a program.");
        hardWords.put("class", "It's a blueprint or template for creating objects.");
        hardWords.put("object", "An instance of a class in object-oriented programming, encapsulating data (attributes) and behavior (methods)..");
        hardWords.put("inheritance", "It's a mechanism in which one class inherits properties and behaviors from another class.");
        hardWords.put("polymorphism", "It's the ability of a single function or method to perform different tasks based on the object that it is called on.");
        hardWords.put("encapsulation", "It's the bundling of data and methods that operate on the data into a single unit or class.");
        hardWords.put("interface", "It's a reference type in Java that is similar to a class but can only contain method signatures and fields.");
        hardWords.put("constructor", "It's a special type of method that is automatically called when an instance of a class is created.");
        hardWords.put("algorithm", "It's a step-by-step procedure for solving a problem.");

        wordsWithHints.put("fruits",  easyFruits);
        wordsWithHints.put("animals",  easyAnimals);
        wordsWithHints.put("flags of countries", mediumWords);
        wordsWithHints.put("programming terms", hardWords);
    }

    private void startGame() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(this, "Select Difficulty Level", "Difficulty", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        SoundPlayer.setMusicVolume(0.5f);
        SoundPlayer.setSoundEffectsVolume(0.5f);
        SoundPlayer.playBackgroundMusic();

        switch (choice) {
            case 0:
                difficulty = "easy";
                break;
            case 1:
                difficulty = "medium";
                break;
            case 2:
                difficulty = "hard";
                break;
            default:
                difficulty = "easy";
                break;
        }

        
        String instructions = getInstructions(difficulty);
        JOptionPane.showMessageDialog(this, instructions, "Instructions", JOptionPane.INFORMATION_MESSAGE);

        JPanel gamePanel = new JPanel(new BorderLayout());

        inputField = new JTextField();
        inputField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setPreferredSize(new Dimension(20, 20));

        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        submitButton.setBackground(new Color(9, 115, 62, 255));
        submitButton.setOpaque(true);
        submitButton.setBorderPainted(false);
        submitButton.setFocusPainted(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        gamePanel.add(inputPanel, BorderLayout.SOUTH);

        hintButton = new JButton("\uD83D\uDC40 Hints");
        hintButton.addActionListener(e -> showHint());
        hintButton.setFont(new Font(Font.SERIF, Font.ITALIC, 29));
        hintButton.setFocusPainted(false);
        gamePanel.add(hintButton, BorderLayout.EAST);

        // Top status bar showing score, lives, hints, time, and category
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        livesLabel = new JLabel("Lives: " + lives);
        livesLabel.setHorizontalAlignment(JLabel.CENTER);
        livesLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        hintsLeftLabel = new JLabel("Hints Left: " + hintsLeft);
        hintsLeftLabel.setHorizontalAlignment(JLabel.CENTER);
        hintsLeftLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        timeLabel = new JLabel("Time Left: " + timeLeft);
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        categoryLabel = new JLabel("");
        categoryLabel.setHorizontalAlignment(JLabel.CENTER);
        categoryLabel.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        JPanel topStatus = new JPanel(new GridLayout(1, 5));
        topStatus.add(scoreLabel);
        topStatus.add(livesLabel);
        topStatus.add(hintsLeftLabel);
        topStatus.add(timeLabel);
        topStatus.add(categoryLabel);
        gamePanel.add(topStatus, BorderLayout.NORTH);

        wordLabel.setHorizontalAlignment(JLabel.CENTER);
        wordLabel.setFont(new Font(Font.SERIF, Font.BOLD, 50));
        wordLabel.setForeground(Color.DARK_GRAY);
        gamePanel.add(wordLabel, BorderLayout.CENTER);

        // time label already included in the topStatus bar

        getContentPane().removeAll();
        add(gamePanel);
        revalidate();
        repaint();

        newGame();

        if (timer != null) {
            timer.stop();
        }
        startTimer();
    }
    
    private void showSoundSettingDialog() {
        JDialog dialog = new JDialog(this, "Option", true);
        dialog.setLayout(new BorderLayout());
    
        JLabel musicVolumeLabel = new JLabel("Music Volume:");
        JSlider musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (SoundPlayer.getMusicVolume() * 100));
        musicVolumeSlider.setMajorTickSpacing(10);
        musicVolumeSlider.setPaintTicks(true);
        musicVolumeSlider.setPaintLabels(true);
    
        JLabel soundVolumeLabel = new JLabel("Sound Effects Volume:");
        JSlider soundVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (SoundPlayer.getSoundEffectsVolume() * 100));
        soundVolumeSlider.setMajorTickSpacing(10);
        soundVolumeSlider.setPaintTicks(true);
        soundVolumeSlider.setPaintLabels(true);
    
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));  
        panel.add(musicVolumeLabel);
        panel.add(musicVolumeSlider);
        panel.add(soundVolumeLabel);
        panel.add(soundVolumeSlider);
    
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int musicVolumeValue = musicVolumeSlider.getValue();
                float musicVolume = (float) musicVolumeValue / 100;
                SoundPlayer.setMusicVolume(musicVolume);
    
                int soundEffectsVolumeValue = soundVolumeSlider.getValue();
                float soundEffectsVolume = (float) soundEffectsVolumeValue / 100;
                SoundPlayer.setSoundEffectsVolume(soundEffectsVolume);
    
                dialog.dispose(); // Isara ang sound setting dialog matapos ma-save ang mga pagbabago
            }
        });
    
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    

    private String getInstructions(String difficulty) {
        String instructions = "";
        switch (difficulty) {
            case "easy":
                instructions = "Welcome to the Easy Mode!\n\nInstructions:\n\n"
                        + "1)"+"The score will show only  when your answer is correct.\n" 
                        + "2)" +" The timer will reset if your answer is correct\n"
                        + "3)" +"You Have Three chances to answer.\n" 
                        + "4)"+"You have 3 hints available to help you .\n" 
                        + "5)"+ "You have 20 seconds only to answer the messy word. \n"
                        + "Good luck!.";
                break;
            case "medium":
                instructions = "Welcome to the Medium Mode!\n\nInstructions:\n\n"
                + "1)"+"The score will show only  when your answer is correct.\n"
                + "2)" +" The timer will reset if your answer is  correct\n" 
                + "3)"+"You Have Three chances to answer.\n" 
                + "4)"+"You have 3 hints available to help you .\n" 
                + "5)"+ "You have 20 seconds only to answer the messy word. \n"
                + "Good luck!.";
                break;
            case "hard":
                instructions = "Welcome to the Hard Mode!\n\nInstructions:\n\n"
                + "1)"+"The score will show only  when you answer is true.\n" 
                + "2)" +" The timer will reset if your answer correct\n"
                + "3)"+"You Have Three chances to answer.\n" 
                + "4)"+"You have 3 hints available to help you .\n" 
                + "5)"+ "You have 20 seconds only to answer the messy word. \n"
                + "Good luck!.";             
                   break;
        }
        return instructions;
    }

    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timeLabel.setText("Time Left: " + timeLeft);
                if (timeLeft <= 0) {
                    timer.stop();
                    gameOver(difficulty);
                }
            }
        });
        timer.start();
    }

    private void newGame() {
        timeLeft = 20;
        timeLabel.setText("Time Left: " + timeLeft);

        hintsLeft = 3;
        hintsLeftLabel.setText("Hints Left: " + hintsLeft);

        lives = 3;
        livesLabel.setText("Lives: " + lives);

        Random random = new Random();
        String category;
        if (difficulty.equals("easy")) {
            category = random.nextBoolean() ? "fruits" : "animals";
        } else if (difficulty.equals("medium")) {
            category = "flags of countries";
        } else {
            category = "programming terms";
        }
        Map<String, String> wordList = wordsWithHints.get(category);
        // reset used words when all words in the category are used
        if (usedWords.size() >= wordList.size()) {
            usedWords.clear();
        }
        String newWord;
        do {
            newWord = getRandomWord(wordList.keySet().toArray(new String[0]));
        } while (usedWords.contains(newWord));
        currentWord = newWord;
        usedWords.add(newWord);
        var hints = wordList.get(currentWord);
        currentCategory = category;
        categoryLabel.setText("Category: " + category);

        String scrambledWord = scrambleWord(currentWord);
        wordLabel.setText(scrambledWord);

        inputField.setText("");
        inputField.requestFocusInWindow();
        enableHintButton();
        if (hintButton != null) {
            hintButton.setText("\uD83D\uDC40 Hints (" + hintsLeft + ")");
        }
    }

    private String getRandomWord(String[] words) {
        Random random = new Random();
        return words[random.nextInt(words.length)];
    }

    private String scrambleWord(String word) {
        char[] chars = word.toCharArray();
        Random random = new Random();
        for (int i = 0; i < chars.length; i++) {
            int j = random.nextInt(chars.length);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    } 

    private void showHint() {
        if (hintsLeft > 0) {
            String hint;
            Map<String, String> wordList = wordsWithHints.get(currentCategory);
            hint = wordList.get(currentWord);
            JOptionPane optionPane = new JOptionPane("Hint: " + hint, JOptionPane.PLAIN_MESSAGE);
            JDialog dialog = optionPane.createDialog("Hint");
            dialog.setPreferredSize(new Dimension(dialog.getPreferredSize().width / 2, dialog.getPreferredSize().height));
            dialog.setVisible(true);
            hintsLeft--;
            hintsLeftLabel.setText("Hints Left: " + hintsLeft);
            if (hintButton != null) {
                hintButton.setText("\uD83D\uDC40 Hints (" + hintsLeft + ")");
            }

            if (hintsLeft == 0) {
                disableHintButton();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No more hints left!", "Hints", JOptionPane.INFORMATION_MESSAGE);
            disableHintButton();
        }
    }
    private void handleSubmit() {               
        String input = inputField.getText();
        if (!input.isEmpty() && input.equalsIgnoreCase(currentWord)) {
            score++;
            updateScoreLabel();
            if (score == 10) {
                JOptionPane.showMessageDialog(this, "You won!", "Congratulations!", JOptionPane.PLAIN_MESSAGE);
                // Add any additional logic you need when the player wins
            } else {
                SoundPlayer.playCorrectSound(); // Play correct sound
                JOptionPane.showMessageDialog(this, "\n" +"SCORE " + score + "\n" + "Correct!", "Congratulations!", JOptionPane.CLOSED_OPTION);
            }
    
            updateScoreLabel();
            if (score >= 10) {
                gameOver(difficulty);
            } else if (lives >1) {
                disableHintButton();
                newGame();
            }
        } else {
            SoundPlayer.playIncorrectSound(); // Play incorrect sound
            UIManager.put("OptionPane.background", new Color(118, 217, 39, 1));
            UIManager.put("OptionPane.messageForeground", Color.RED);
            JOptionPane.showMessageDialog(this,  "Lives left: " + (lives - 1) + "\n" + "Please Try again!", "Incorrect", JOptionPane.ERROR_MESSAGE);
            lives--;
            livesLabel.setText("Lives: " + lives);
            if (lives == 0) {
                gameOver(difficulty);
            } else {
                disableHintButton();
            }
        }
        if (hintsLeft == 0 && input.equalsIgnoreCase(currentWord)) {
            disableHintButton();
        }
    }
    

    private void gameOver(String difficulty) {
        timer.stop();
        String message;
        if (score >= 10) {
            message = "Congratulations! You finished the " + difficulty + " mode!";
        } else {
            message = "You failed the " + difficulty + " mode";
        }
        JOptionPane.showMessageDialog(null,"Your score: " + score + "\n" + message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        int choice = JOptionPane.showConfirmDialog(null, "Do you want to start a new game?", "Choose", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            score = 0;
            updateScoreLabel();
            startGame();
        } else {
            System.exit(0);
        }
    }
    

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    private void enableHintButton() {
        if (hintButton != null) {
            hintButton.setEnabled(true);
        }
    }

    private void disableHintButton() {
        if (hintButton != null) {
            hintButton.setEnabled(false);
        }
    }

    public static int getVolume() {
        return (int) (SoundPlayer.getMusicVolume() * 100);
    }
}

class SoundPlayer {
    protected static Clip backgroundMusic;
    protected static Clip soundEffects;
    private static float volume = 0.5f; // Initial volume value, can be adjusted as needed

    public static void playCorrectSound() {
        try {
            InputStream is = WordScrambleGame.class.getResourceAsStream("/correct.wav");
            if (is == null) return; // resource not available
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            setVolume(clip, volume);
            soundEffects = clip;
            clip.start();
        } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void playIncorrectSound() {
        try {
            InputStream is = WordScrambleGame.class.getResourceAsStream("/incorrect.wav");
            if (is == null) {
                is = WordScrambleGame.class.getResourceAsStream("/incorrect-buzzer-sound-147336 (1) (1).wav");
            }
            if (is == null) return;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            setVolume(clip, volume);
            soundEffects = clip;
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playBackgroundMusic() {
        try {
            InputStream is = WordScrambleGame.class.getResourceAsStream("/game-music-loop-7-145285.wav");
            if (is == null) {
                is = WordScrambleGame.class.getResourceAsStream("/background.wav");
            }
            if (is == null) return;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            setVolume(backgroundMusic, volume); // Set initial volume for background music
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMusicVolume(float vol) {
        volume = vol;
        setVolume(backgroundMusic, volume); // Adjust the volume of background music
    }

    public static void setSoundEffectsVolume(float vol) {
        volume = vol;
        if (soundEffects != null) {
            setVolume(soundEffects, volume); // Adjust the volume of sound effects
        }
    }

    public static void setVolume(Clip clip, float vol) {
        volume = vol;
        if (clip != null) { // Make sure the Clip object is not null before performing volume setting
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volume <= 0f) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    float dB = (float) (Math.log(Math.max(volume, 0.0001f)) / Math.log(10.0) * 20.0);
                    gainControl.setValue(dB);
                }
            } catch (IllegalArgumentException ex) {
                // clip may not support MASTER_GAIN
            }
        }
    }

    public static float getMusicVolume() {
        return volume;
        
    }

    public static float getSoundEffectsVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "SoundPlayer []";
    }

    public static Clip getBackgroundMusic() {
        return backgroundMusic;
    }

    public static void setBackgroundMusic(Clip backgroundMusic) {
        SoundPlayer.backgroundMusic = backgroundMusic;
    }
}
