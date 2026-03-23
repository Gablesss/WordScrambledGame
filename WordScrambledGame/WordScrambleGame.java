import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*;
import javax.swing.text.*;

public class WordScrambleGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel glassPane;

    private Font customFont;
    private Font customFontTitle;
    private Font customFontSmall;
    private Image bgImage;

    // Game state
    private int score = 0;
    private int timeLeft = 20;
    private String difficulty = "easy";
    private String currentCategory;
    private String currentWord;
    private Timer gameTimer;

    // Hint, Skip & Wrong Input
    private int currentHintLevel = 0;
    private int globalSkipsLeft = 3;
    private int maxHintsForWord = 0;
    private int hintsRemainingForWord = 0;
    private int wordsCorrect = 0;

    // UI elements
    private JLabel scoreLabel;
    private JLabel skipsLabel;
    private JLabel wordDisplay;
    private JLabel wordHintLabel;
    private StyledTextField inputField;
    private JLabel timeLabel;
    private TimerBar timerBar;
    private JLabel categoryLabel;
    private StyledButton hintButton;
    private StyledButton skipBtn;

    // Results UI
    private JLabel resultTitleLabel;
    private JLabel resultScoreLabel;

    // High Scores
    private java.util.List<ScoreEntry> highScores = new ArrayList<>();
    private JPanel highScoreListPanel;

    // Data
    private final Map<String, Map<String, String>> wordsWithHints = new HashMap<>();
    private Set<String> usedWords = new HashSet<>();

    // Colors
    private static final Color BG_COLOR = new Color(15, 23, 42); // Deep blue
    private static final Color ACCENT_GOLD = new Color(245, 158, 11);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color HINT_GOLD = new Color(255, 179, 0);
    private static final Color HINT_HOVER = new Color(255, 200, 51);
    private static final Color WRONG_RED = new Color(239, 68, 68);

    private boolean isFlashing = false;
    private float flashAlpha = 0f;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WordScrambleGame());
    }

    public WordScrambleGame() {
        setTitle("Word Scramble: Master the Letters");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadAssets();
        fillWordHintMaps();
        loadHighScores();

        SoundPlayer.setMusicVolume(0.5f);
        SoundPlayer.setSoundEffectsVolume(0.5f);
        SoundPlayer.playBackgroundMusic();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(15, 23, 42, 200));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(),
                            new Color(2, 6, 23));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Setup GlassPane for Red flash overlay
        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (isFlashing) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(239, 68, 68, (int) (flashAlpha * 80))); // translucent red overlay
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setFont(customFontTitle.deriveFont(80f));
                    g2.setColor(new Color(255, 50, 50, (int) (flashAlpha * 255)));
                    String msg = "❌ WRONG!";
                    FontMetrics fm = g2.getFontMetrics();
                    int cw = fm.stringWidth(msg);
                    g2.drawString(msg, (getWidth() - cw) / 2, (getHeight() + fm.getAscent()) / 2 - 50);

                    g2.dispose();
                }
            }
        };
        glassPane.setOpaque(false);
        setGlassPane(glassPane);
        glassPane.setVisible(true);

        mainPanel.add(createMainMenu(), "MainMenu");
        mainPanel.add(createDifficultyMenu(), "DifficultyMenu");
        mainPanel.add(createGameScreen(), "GameScreen");
        mainPanel.add(createResultsScreen(), "ResultsScreen");
        mainPanel.add(createHighScoreScreen(), "HighScoreScreen");

        add(mainPanel);
        setVisible(true);
    }

    private void loadAssets() {
        try {
            File fontFile = new File("lufgabold.ttf");
            if (fontFile.exists()) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(24f);
                customFontTitle = customFont.deriveFont(60f);
                customFontSmall = customFont.deriveFont(16f);
            } else {
                customFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
                customFontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 60);
                customFontSmall = new Font(Font.SANS_SERIF, Font.BOLD, 16);
            }

            File bgFile = new File("background_wrcscrambled.jpg");
            if (bgFile.exists()) {
                bgImage = new ImageIcon(bgFile.getAbsolutePath()).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            customFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
            customFontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 60);
            customFontSmall = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        }
    }

    private void fillWordHintMaps() {
        Map<String, String> easyFruits = new HashMap<>();
        easyFruits.put("watermelon", "hint");
        easyFruits.put("pineapple", "hint");
        easyFruits.put("avocado", "hint");
        easyFruits.put("guava", "hint");
        easyFruits.put("strawberry", "hint");
        easyFruits.put("apple", "hint");
        easyFruits.put("banana", "hint");
        easyFruits.put("orange", "hint");
        easyFruits.put("grape", "hint");
        easyFruits.put("cherries", "hint");

        Map<String, String> easyAnimals = new HashMap<>();
        easyAnimals.put("lion", "hint");
        easyAnimals.put("gorilla", "hint");
        easyAnimals.put("tiger", "hint");
        easyAnimals.put("grizzlybear", "hint");
        easyAnimals.put("shark", "hint");
        easyAnimals.put("dog", "hint");
        easyAnimals.put("cat", "hint");
        easyAnimals.put("zebra", "hint");
        easyAnimals.put("snake", "hint");
        easyAnimals.put("bird", "hint");

        Map<String, String> mediumWords = new HashMap<>();
        mediumWords.put("greece", "hint");
        mediumWords.put("nigeria", "hint");
        mediumWords.put("south korea", "hint");
        mediumWords.put("spain", "hint");
        mediumWords.put("south africa", "hint");
        mediumWords.put("italy", "hint");
        mediumWords.put("australia", "hint");
        mediumWords.put("usa", "hint");
        mediumWords.put("egypt", "hint");
        mediumWords.put("argentina", "hint");

        Map<String, String> hardWords = new HashMap<>();
        hardWords.put("version control", "hint");
        hardWords.put("exception", "hint");
        hardWords.put("data structure", "hint");
        hardWords.put("recursion", "hint");
        hardWords.put("conditional", "hint");
        hardWords.put("serialization", "hint");
        hardWords.put("byte", "hint");
        hardWords.put("bit", "hint");
        hardWords.put("framework", "hint");
        hardWords.put("compilation", "hint");

        wordsWithHints.put("fruits", easyFruits);
        wordsWithHints.put("animals", easyAnimals);
        wordsWithHints.put("flags of countries", mediumWords);
        wordsWithHints.put("programming terms", hardWords);
    }

    // --- UI Builders ---

    private JPanel createMainMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title1 = new JLabel("WORD");
        title1.setFont(customFontTitle);
        title1.setForeground(TEXT_WHITE);
        panel.add(title1, gbc);

        gbc.gridy++;
        JLabel title2 = new JLabel("SCRAMBLE");
        title2.setFont(customFontTitle);
        title2.setForeground(ACCENT_GOLD);
        panel.add(title2, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(40, 10, 10, 10);
        StyledButton startBtn = new StyledButton("START GAME", ACCENT_GOLD, BG_COLOR);
        startBtn.setPreferredSize(new Dimension(300, 60));
        startBtn.addActionListener(e -> cardLayout.show(mainPanel, "DifficultyMenu"));
        panel.add(startBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        StyledButton scoresBtn = new StyledButton("HIGH SCORES", new Color(51, 65, 85), TEXT_WHITE);
        scoresBtn.setPreferredSize(new Dimension(300, 60));
        scoresBtn.addActionListener(e -> {
            updateHighScoreUI();
            cardLayout.show(mainPanel, "HighScoreScreen");
        });
        panel.add(scoresBtn, gbc);

        gbc.gridy++;
        StyledButton exitBtn = new StyledButton("EXIT", new Color(153, 27, 27), TEXT_WHITE);
        exitBtn.setPreferredSize(new Dimension(300, 60));
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn, gbc);

        return panel;
    }

    private JPanel createDifficultyMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("SELECT LEVEL");
        title.setFont(customFontTitle.deriveFont(40f));
        title.setForeground(TEXT_WHITE);
        panel.add(title, gbc);

        gbc.gridy++;
        StyledButton easyBtn = new StyledButton("EASY", new Color(34, 197, 94), Color.BLACK);
        easyBtn.setPreferredSize(new Dimension(300, 80));
        easyBtn.addActionListener(e -> startGame("easy"));
        panel.add(easyBtn, gbc);

        gbc.gridy++;
        StyledButton medBtn = new StyledButton("MEDIUM", ACCENT_GOLD, Color.BLACK);
        medBtn.setPreferredSize(new Dimension(300, 80));
        medBtn.addActionListener(e -> startGame("medium"));
        panel.add(medBtn, gbc);

        gbc.gridy++;
        StyledButton hardBtn = new StyledButton("HARD", new Color(239, 68, 68), Color.WHITE);
        hardBtn.setPreferredSize(new Dimension(300, 80));
        hardBtn.addActionListener(e -> startGame("hard"));
        panel.add(hardBtn, gbc);

        gbc.gridy++;
        StyledButton backBtn = new StyledButton("BACK", new Color(51, 65, 85), TEXT_WHITE);
        backBtn.setPreferredSize(new Dimension(200, 50));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        panel.add(backBtn, gbc);

        return panel;
    }

    private JPanel createGameScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 30, 10, 30));

        JPanel statsPanel = new JPanel(new GridLayout(1, 2));
        statsPanel.setOpaque(false);

        scoreLabel = new JLabel("SCORE: 0");
        scoreLabel.setFont(customFont.deriveFont(20f));
        scoreLabel.setForeground(ACCENT_GOLD);

        skipsLabel = new JLabel("Skips Remaining: 3", SwingConstants.RIGHT);
        skipsLabel.setFont(
                customFontSmall != null ? customFontSmall.deriveFont(18f) : new Font("SansSerif", Font.BOLD, 18));
        skipsLabel.setForeground(new Color(148, 163, 184));

        statsPanel.add(scoreLabel);
        statsPanel.add(skipsLabel);

        timerBar = new TimerBar();
        timerBar.setPreferredSize(new Dimension(100, 10));

        JPanel timerContainer = new JPanel(new BorderLayout(0, 5));
        timerContainer.setOpaque(false);
        timeLabel = new JLabel("Time: 0:60", SwingConstants.CENTER);
        timeLabel.setFont(
                customFontSmall != null ? customFontSmall.deriveFont(16f) : new Font("SansSerif", Font.PLAIN, 16));
        timeLabel.setForeground(TEXT_WHITE);
        timerContainer.add(timeLabel, BorderLayout.NORTH);
        timerContainer.add(timerBar, BorderLayout.SOUTH);

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);
        topContainer.add(statsPanel, BorderLayout.NORTH);
        topContainer.add(timerContainer, BorderLayout.SOUTH);

        categoryLabel = new JLabel("Category: ", SwingConstants.CENTER);
        categoryLabel.setFont(customFontSmall);
        categoryLabel.setForeground(new Color(148, 163, 184));
        topContainer.add(categoryLabel, BorderLayout.CENTER);

        topBar.add(topContainer, BorderLayout.NORTH);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        wordDisplay = new JLabel("SCRAMBLE");
        wordDisplay.setFont(customFontTitle.deriveFont(70f));
        wordDisplay.setForeground(TEXT_WHITE);
        wordDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(wordDisplay, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        wordHintLabel = new JLabel("", SwingConstants.CENTER);
        wordHintLabel.setFont(customFont.deriveFont(40f));
        wordHintLabel.setForeground(TEXT_WHITE);
        centerPanel.add(wordHintLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 30, 40, 30));

        inputField = new StyledTextField("TYPE YOUR ANSWER...");
        inputField.setFont(customFont);
        inputField.setPreferredSize(new Dimension(450, 60));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> checkAnswer());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        bottomPanel.add(inputField, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 0, 10);

        skipBtn = new StyledButton("SKIP (3 LEFT)", new Color(51, 65, 85), TEXT_WHITE);
        skipBtn.setPreferredSize(new Dimension(150, 50));
        skipBtn.addActionListener(e -> skipWord());
        gbc.gridx = 0;
        bottomPanel.add(skipBtn, gbc);

        StyledButton submitBtn = new StyledButton("SUBMIT", ACCENT_GOLD, BG_COLOR);
        submitBtn.setPreferredSize(new Dimension(160, 50));
        submitBtn.addActionListener(e -> checkAnswer());
        gbc.gridx = 1;
        bottomPanel.add(submitBtn, gbc);

        hintButton = new StyledButton("\uD83D\uDCA1 HINT (0/0)", HINT_GOLD, Color.BLACK) {
            @Override
            protected Color getHoverColor() {
                return HINT_HOVER;
            }
        };
        hintButton.setPreferredSize(new Dimension(180, 50));
        hintButton.addActionListener(e -> showHint());
        gbc.gridx = 2;
        bottomPanel.add(hintButton, gbc);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createResultsScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        resultTitleLabel = new JLabel("GAME OVER");
        resultTitleLabel.setFont(customFontTitle);
        resultTitleLabel.setForeground(WRONG_RED);
        panel.add(resultTitleLabel, gbc);

        gbc.gridy++;
        resultScoreLabel = new JLabel("FINAL SCORE: 0");
        resultScoreLabel.setFont(customFont.deriveFont(30f));
        resultScoreLabel.setForeground(TEXT_WHITE);
        panel.add(resultScoreLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(40, 10, 10, 10);
        StyledButton playAgainBtn = new StyledButton("PLAY AGAIN", ACCENT_GOLD, BG_COLOR);
        playAgainBtn.setPreferredSize(new Dimension(300, 60));
        playAgainBtn.addActionListener(e -> cardLayout.show(mainPanel, "DifficultyMenu"));
        panel.add(playAgainBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        StyledButton menuBtn = new StyledButton("MAIN MENU", new Color(51, 65, 85), TEXT_WHITE);
        menuBtn.setPreferredSize(new Dimension(300, 60));
        menuBtn.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        panel.add(menuBtn, gbc);

        return panel;
    }

    private JPanel createHighScoreScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        title.setFont(customFontTitle.deriveFont(40f));
        title.setForeground(ACCENT_GOLD);
        panel.add(title, BorderLayout.NORTH);

        highScoreListPanel = new JPanel();
        highScoreListPanel.setLayout(new BoxLayout(highScoreListPanel, BoxLayout.Y_AXIS));
        highScoreListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(highScoreListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(scroll, BorderLayout.CENTER);

        StyledButton backBtn = new StyledButton("BACK", new Color(51, 65, 85), TEXT_WHITE);
        backBtn.setPreferredSize(new Dimension(200, 50));
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.add(backBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // --- Logic ---

    private void startGame(String diff) {
        difficulty = diff;
        score = 0;
        globalSkipsLeft = 3;
        wordsCorrect = 0;
        usedWords.clear();
        scoreLabel.setText("SCORE: 0");
        updateSkipsDisplay();

        if (difficulty.equals("easy")) {
            currentCategory = new Random().nextBoolean() ? "fruits" : "animals";
        } else if (difficulty.equals("medium")) {
            currentCategory = "flags of countries";
        } else {
            currentCategory = "programming terms";
        }
        categoryLabel.setText("CATEGORY: " + currentCategory.toUpperCase());

        cardLayout.show(mainPanel, "GameScreen");
        inputField.requestFocusInWindow();
        nextWord();
    }

    private void nextWord() {
        inputField.setText("");
        inputField.resetBg();

        Map<String, String> wordList = wordsWithHints.get(currentCategory);
        if (usedWords.size() >= wordList.size()) {
            usedWords.clear();
        }

        String[] keys = wordList.keySet().toArray(new String[0]);
        do {
            currentWord = keys[new Random().nextInt(keys.length)];
        } while (usedWords.contains(currentWord));
        usedWords.add(currentWord);

        currentWord = currentWord.toUpperCase().replaceAll("\\s+", ""); // remove spaces

        // HINT vars
        currentHintLevel = 0;

        int len = currentWord.length();
        if (len <= 4)
            maxHintsForWord = 1;
        else if (len <= 6)
            maxHintsForWord = 2;
        else if (len <= 8)
            maxHintsForWord = 3;
        else
            maxHintsForWord = 4;

        hintsRemainingForWord = maxHintsForWord;

        updateHintDisplay();

        // Scramble
        char[] chars = currentWord.toCharArray();
        Random rnd = new Random();
        for (int i = 0; i < chars.length; i++) {
            int j = rnd.nextInt(chars.length);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        String scrambled = new String(chars);
        StringBuilder spaced = new StringBuilder();
        for (char c : scrambled.toCharArray()) {
            spaced.append(c).append(" ");
        }
        wordDisplay.setText(spaced.toString().trim());

        timeLeft = 600;
        timeLabel.setText("Time: 0:60");
        if (gameTimer != null)
            gameTimer.stop();
        gameTimer = new Timer(100, e -> {
            timeLeft--;
            timerBar.setProgress((float) timeLeft / 600f);
            int seconds = (int) Math.ceil(timeLeft / 10.0);
            timeLabel.setText("Time: 0:" + String.format("%02d", seconds));
            if (timeLeft <= 0) {
                gameTimer.stop();
                timeLabel.setText("Time: 0:00");
                showGameOverPopup();
            }
        });
        gameTimer.start();
    }

    private void updateHintDisplay() {
        int total = currentWord.length();

        if (hintsRemainingForWord <= 0) {
            hintButton.setText("No Hints Left");
            hintButton.setEnabled(false);
        } else {
            hintButton.setText("\uD83D\uDCA1 HINT (" + hintsRemainingForWord + " LEFT)");
            hintButton.setEnabled(true);
        }

        if (currentHintLevel >= total) {
            hintButton.setEnabled(false);
        }

        StringBuilder b = new StringBuilder("<html>");
        for (int i = 0; i < total; i++) {
            if (i < currentHintLevel) {
                b.append("<font color='#FFB300'>").append(currentWord.charAt(i)).append("</font>");
            } else {
                b.append("<font color='#94A3B8'>_</font>");
            }
            if (i < total - 1)
                b.append("&nbsp;&nbsp;");
        }
        b.append("</html>");
        wordHintLabel.setText(b.toString());
    }

    private void showHint() {
        if (hintsRemainingForWord > 0 && currentHintLevel < currentWord.length()) {
            currentHintLevel++;
            hintsRemainingForWord--;

            updateHintDisplay();

            String hintText = currentWord.substring(0, currentHintLevel);
            inputField.setText(hintText);
            inputField.requestFocusInWindow();

            // Deduct 10 points
            score = Math.max(0, score - 10);
            animateScoreLoss();
        }
    }

    private void checkAnswer() {
        if (gameTimer == null || !gameTimer.isRunning() || inputField.getText().trim().isEmpty())
            return;
        String ans = inputField.getText().trim().toUpperCase().replaceAll("\\s+", "");
        if (ans.equals(currentWord)) {
            // Correct
            gameTimer.stop();
            SoundPlayer.playCorrectSound();
            int points = difficulty.equals("hard") ? 20 : (difficulty.equals("medium") ? 15 : 10);
            score += points;
            wordsCorrect++;
            scoreLabel.setText("SCORE: " + score);
            animateScoreGain();

            inputField.setText("");
            inputField.setPlaceholder("Correct! +" + points + " points");
            new Timer(1000, evt -> {
                inputField.setPlaceholder("TYPE YOUR ANSWER...");
            }).start();

            nextWord();
        } else {
            handleIncorrect();
        }
    }

    private void handleIncorrect() {
        SoundPlayer.playIncorrectSound();
        inputField.setText("");
        inputField.flashRedBg();
        shakeAnimation(inputField);

        inputField.setPlaceholder("Wrong! Try again");
        new Timer(1500, evt -> {
            inputField.setPlaceholder("TYPE YOUR ANSWER...");
        }).start();
    }

    private void showGameOverPopup() {
        if (score > 0) {
            String name = JOptionPane.showInputDialog(this, "Game Over! Enter name for high score:", "New Record",
                    JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                addHighScore(name.trim(), score);
                updateHighScoreUI();
            }
        }

        JDialog dialog = new JDialog(this, "Game Over", true); // modal
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel roundedPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 30, 30);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 30, 30);
                g2.dispose();
            }
        };
        roundedPanel.setOpaque(false);
        roundedPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("GAME OVER");
        title.setFont(customFontTitle != null ? customFontTitle.deriveFont(50f) : new Font("SansSerif", Font.BOLD, 50));
        title.setForeground(new Color(239, 68, 68));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        roundedPanel.add(title, gbc);

        gbc.gridy++;
        JLabel subTitle = new JLabel("Time's Up!");
        subTitle.setFont(
                customFontSmall != null ? customFontSmall.deriveFont(24f) : new Font("SansSerif", Font.PLAIN, 24));
        subTitle.setForeground(new Color(239, 68, 68));
        subTitle.setHorizontalAlignment(SwingConstants.CENTER);
        roundedPanel.add(subTitle, gbc);

        gbc.gridy++;
        JLabel scoreLbl = new JLabel("Your Score: " + score);
        scoreLbl.setFont(customFont != null ? customFont.deriveFont(24f) : new Font("SansSerif", Font.PLAIN, 24));
        scoreLbl.setForeground(Color.WHITE);
        roundedPanel.add(scoreLbl, gbc);

        gbc.gridy++;
        JLabel wordsLbl = new JLabel("Total Words Correct: " + wordsCorrect);
        wordsLbl.setFont(
                customFontSmall != null ? customFontSmall.deriveFont(18f) : new Font("SansSerif", Font.PLAIN, 18));
        wordsLbl.setForeground(Color.LIGHT_GRAY);
        roundedPanel.add(wordsLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        StyledButton playBtn = new StyledButton("Play Again", new Color(76, 175, 80), Color.WHITE);
        playBtn.setPreferredSize(new Dimension(150, 50));
        playBtn.addActionListener(e -> {
            dialog.dispose();
            startGame(difficulty);
        });

        StyledButton quitBtn = new StyledButton("Quit", new Color(244, 67, 54), Color.WHITE);
        quitBtn.setPreferredSize(new Dimension(150, 50));
        quitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(playBtn);
        btnPanel.add(quitBtn);
        roundedPanel.add(btnPanel, gbc);

        dialog.add(roundedPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void skipWord() {
        if (globalSkipsLeft > 0) {
            globalSkipsLeft--;
            updateSkipsDisplay();

            if (gameTimer != null)
                gameTimer.stop();
            nextWord();
        }
    }

    // Old endGame replaced by showGameOverPopup

    // --- Animations and Visuals ---

    private void triggerBigRedFlash() {
        isFlashing = true;
        flashAlpha = 1.0f;
        glassPane.repaint();

        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            flashAlpha -= 0.1f;
            if (flashAlpha <= 0) {
                flashAlpha = 0;
                isFlashing = false;
                t.stop();
            }
            glassPane.repaint();
        });
        t.start();
    }

    private void animateScoreGain() {
        final Color original = ACCENT_GOLD;
        final Color flash = new Color(34, 197, 94);
        scoreLabel.setForeground(flash);
        Timer t = new Timer(300, e -> scoreLabel.setForeground(original));
        t.setRepeats(false);
        t.start();
    }

    private void animateScoreLoss() {
        final Color original = ACCENT_GOLD;
        scoreLabel.setForeground(WRONG_RED);
        scoreLabel.setText("SCORE: " + score + " (Hint -10)");
        Timer t = new Timer(1000, e -> {
            scoreLabel.setForeground(original);
            scoreLabel.setText("SCORE: " + score);
        });
        t.setRepeats(false);
        t.start();
    }

    private void shakeAnimation(JComponent comp) {
        final int originalX = comp.getLocation().x;
        final int originalY = comp.getLocation().y;
        Timer timer = new Timer(30, null);
        timer.addActionListener(new ActionListener() {
            int count = 0;
            boolean left = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count >= 14) {
                    comp.setLocation(originalX, originalY);
                    timer.stop();
                } else {
                    int offset = left ? -15 : 15;
                    comp.setLocation(originalX + offset, originalY);
                    left = !left;
                    count++;
                }
            }
        });
        timer.start();
    }

    private void updateSkipsDisplay() {
        if (skipsLabel != null) {
            skipsLabel.setText("Skips Remaining: " + globalSkipsLeft);
        }
        if (skipBtn != null) {
            if (globalSkipsLeft <= 0) {
                skipBtn.setText("No Skips Left");
                skipBtn.setEnabled(false);
            } else {
                skipBtn.setText("SKIP (" + globalSkipsLeft + " LEFT)");
                skipBtn.setEnabled(true);
            }
        }
    }

    // --- High Scores ---

    private void loadHighScores() {
        highScores.clear();
        try {
            File f = new File("highscores.txt");
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] pts = line.split(":");
                    if (pts.length == 2) {
                        highScores.add(new ScoreEntry(pts[0], Integer.parseInt(pts[1])));
                    }
                }
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(highScores);
    }

    private void saveHighScores() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("highscores.txt"));
            for (ScoreEntry se : highScores) {
                bw.write(se.name + ":" + se.score + "\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addHighScore(String name, int score) {
        highScores.add(new ScoreEntry(name, score));
        Collections.sort(highScores);
        if (highScores.size() > 10)
            highScores = highScores.subList(0, 10);
        saveHighScores();
    }

    private void updateHighScoreUI() {
        highScoreListPanel.removeAll();
        for (int i = 0; i < highScores.size(); i++) {
            ScoreEntry se = highScores.get(i);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(true);
            row.setBackground(new Color(30, 41, 59));
            row.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            row.setMaximumSize(new Dimension(500, 50));

            JLabel num = new JLabel(String.format("%02d", i + 1));
            num.setFont(customFontSmall);
            num.setForeground(ACCENT_GOLD);
            num.setBorder(new EmptyBorder(0, 0, 0, 15));

            JLabel name = new JLabel(se.name);
            name.setFont(customFontSmall);
            name.setForeground(TEXT_WHITE);

            JLabel sc = new JLabel(String.valueOf(se.score));
            sc.setFont(customFontSmall);
            sc.setForeground(ACCENT_GOLD);

            row.add(num, BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            row.add(sc, BorderLayout.EAST);

            highScoreListPanel.add(row);
            highScoreListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        highScoreListPanel.revalidate();
        highScoreListPanel.repaint();
    }

    private static class ScoreEntry implements Comparable<ScoreEntry> {
        String name;
        int score;

        ScoreEntry(String n, int s) {
            name = n;
            score = s;
        }

        @Override
        public int compareTo(ScoreEntry o) {
            return Integer.compare(o.score, this.score);
        }
    }

    // --- Custom Components ---

    class StyledButton extends JButton {
        private Color bgColor;
        private Color fgColor;
        private boolean isHovered = false;

        public StyledButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fgColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (customFont != null)
                setFont(customFont.deriveFont(20f));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        protected Color getHoverColor() {
            return bgColor.brighter();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 25, 25);

            if (isHovered && isEnabled()) {
                g2.setColor(getHoverColor());
            } else {
                g2.setColor(isEnabled() ? bgColor : Color.GRAY);
            }
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 25, 25);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    class StyledTextField extends JTextField {
        private String placeholder;
        private boolean isError = false;

        public StyledTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(TEXT_WHITE);
            setCaretColor(TEXT_WHITE);
            setBorder(new EmptyBorder(5, 15, 5, 15));
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    if (string != null)
                        super.insertString(fb, offset, string.toUpperCase(), attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    if (text != null)
                        super.replace(fb, offset, length, text.toUpperCase(), attrs);
                }
            });
        }

        public void setPlaceholder(String text) {
            this.placeholder = text;
            repaint();
        }

        public void flashRedBg() {
            isError = true;
            repaint();
            Timer t = new Timer(500, e -> resetBg());
            t.setRepeats(false);
            t.start();
        }

        public void resetBg() {
            isError = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isError ? new Color(220, 38, 38, 200) : new Color(30, 41, 59, 200));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2.setColor(isFocusOwner() && !isError ? ACCENT_GOLD : new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                int x = (getWidth() - fm.stringWidth(placeholder)) / 2;
                g2.drawString(placeholder, x, y);
            }
            g2.dispose();
        }
    }

    class TimerBar extends JPanel {
        private float progress = 1.0f; // 0.0 to 1.0
        private boolean isFlashingRed = false;

        public TimerBar() {
            setOpaque(false);
        }

        public void setProgress(float p) {
            this.progress = Math.max(0, Math.min(1, p));
            repaint();
        }

        public void flashRed() {
            isFlashingRed = true;
            repaint();
            Timer t = new Timer(500, e -> {
                isFlashingRed = false;
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 41, 59));
            int h = 10;
            int y = (getHeight() - h) / 2;
            g2.fillRoundRect(0, y, getWidth(), h, h, h);
            Color barColor = isFlashingRed ? new Color(239, 68, 68)
                    : (progress > 0.5f ? new Color(34, 197, 94)
                            : (progress > 0.2f ? ACCENT_GOLD : new Color(239, 68, 68)));
            g2.setColor(barColor);
            g2.fillRoundRect(0, y, (int) (getWidth() * progress), h, h, h);
            g2.dispose();
        }
    }
}

class SoundPlayer {
    protected static Clip backgroundMusic;
    protected static Clip soundEffects;
    private static float volume = 0.5f;

    public static void playCorrectSound() {
        try {
            InputStream is = WordScrambleGame.class.getResourceAsStream("/correct.wav");
            if (is == null)
                return;
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
            if (is == null)
                return;
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
            if (is == null)
                return;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            setVolume(backgroundMusic, volume);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMusicVolume(float vol) {
        volume = vol;
        setVolume(backgroundMusic, volume);
    }

    public static void setSoundEffectsVolume(float vol) {
        volume = vol;
        if (soundEffects != null) {
            setVolume(soundEffects, volume);
        }
    }

    public static void setVolume(Clip clip, float vol) {
        volume = vol;
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volume <= 0f) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    float dB = (float) (Math.log(Math.max(volume, 0.0001f)) / Math.log(10.0) * 20.0);
                    gainControl.setValue(dB);
                }
            } catch (IllegalArgumentException ex) {
            }
        }
    }

    public static float getMusicVolume() {
        return volume;
    }

    public static float getSoundEffectsVolume() {
        return volume;
    }

    public static Clip getBackgroundMusic() {
        return backgroundMusic;
    }

    public static void setBackgroundMusic(Clip backgroundMusic) {
        SoundPlayer.backgroundMusic = backgroundMusic;
    }
}
