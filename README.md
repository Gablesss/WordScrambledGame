# 🔤 Word Scramble Game

![Java](https://img.shields.io/badge/Java-25.0.2-orange?style=for-the-badge&logo=java)
![Platform](https://img.shields.io/badge/Platform-Windows-blue?style=for-the-badge&logo=windows)
![IDE](https://img.shields.io/badge/IDE-VS%20Code-blue?style=for-the-badge&logo=visualstudiocode)
![Status](https://img.shields.io/badge/Status-Active-green?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

> A fun and interactive Word Scramble desktop game built with Java Swing. Unscramble words before the timer runs out — test your vocabulary and speed!

---

## 📸 Preview

![Game Preview](WordScrambledGame/background_wrcscrambled.jpg)

---

## 🎮 Gameplay

- A scrambled word is displayed on screen
- You have **60 seconds** to guess the correct word
- Type your answer in the input field and press **Enter** or click **Submit**
- Guess correctly → timer resets to 60 seconds → next word loads
- Wrong answer → try again with remaining time (no penalty)
- Timer hits **0:00** → **GAME OVER**

---

## ✨ Features

| Feature | Description |
|---|---|
| ⏱️ **60-Second Timer** | Each word gives exactly 1 minute to solve |
| 💡 **Smart Hint System** | Hints based on word length, types letters into input field |
| ⏭️ **Skip System** | 3 total skips per game, resets timer on use |
| 🔊 **Sound Effects** | Buzzer sound plays on wrong answer |
| 🎨 **Custom UI** | Background image and custom font (lufgabold) |
| 🏆 **Score Tracking** | Points awarded for each correct answer |
| 💀 **Game Over Popup** | Shows final score and words solved with Play Again / Quit |

---

## 💡 Hint System

Hints are based on the length of the scrambled word:

| Word Length | Hints Available |
|---|---|
| 3 - 4 letters | 1 hint |
| 5 - 6 letters | 2 hints |
| 7 - 8 letters | 3 hints |
| 9+ letters | 4 hints |

- Each hint click **types one letter** into the input field
- Hints **reset** for every new word
- Hint button is **disabled** when all hints are used

---

## ⏭️ Skip System

- Player gets **3 total skips** for the whole game
- Skipping loads the next word and **resets the timer**
- Skip is **completely separate** from the scoring system
- Skip button is **disabled** when all 3 skips are used

---

## ⏱️ Timer Rules

| Event | Timer Behavior |
|---|---|
| ✅ Correct Answer | Resets to 60 seconds |
| ❌ Wrong Answer | Keeps counting down |
| ⏭️ Skip | Resets to 60 seconds |
| 💀 Timer = 0:00 | Game Over immediately |

---

## 🗂️ Project Structure

```
WordScrambledGame/
│
├── WordScrambledGame/
│   ├── WordScrambleGame.java        # Main game source code
│   ├── WordScrambleGame.class       # Compiled main class
│   ├── WordScrambleGame$1.class     # Compiled inner class
│   ├── WordScrambleGame$2.class     # Compiled inner class
│   ├── SoundPlayer.class            # Sound handler class
│   ├── background_wrcscrambled.jpg  # Game background image
│   ├── incorrect-buzzer-sound.mp3   # Wrong answer sound effect
│   └── lufgabold.ttf                # Custom game font
│
├── lib/                             # External libraries (if any)
├── .vscode/                         # VS Code settings
├── WordScrambledGame.jar            # Runnable JAR file
└── README.md                        # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 21 or higher** installed
- Download from: [https://adoptium.net](https://adoptium.net)
- Make sure Java is added to your system **PATH**

### Verify Java Installation

```powershell
java -version
```

Expected output:
```
openjdk version "25.0.2" 2026-01-20 LTS
```

---

## ▶️ How to Run

### Option 1: Run JAR directly (Recommended)

```powershell
cd C:\Users\YourName\Desktop\WordScrambledGame
java -jar WordScrambledGame.jar
```

### Option 2: Double-click the JAR

- Navigate to the project folder
- Double-click `WordScrambledGame.jar`
- If it doesn't open: Right-click → Open with → **Java Platform SE Binary**

### Option 3: Use the Batch File

Create a `RunGame.bat` file in the project folder:

```batch
@echo off
"C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin\java.exe" -jar WordScrambledGame.jar
pause
```

Then double-click `RunGame.bat` to launch the game.

### Option 4: Run from VS Code

1. Open `WordScrambleGame.java` in VS Code
2. Install **Extension Pack for Java** by Microsoft
3. Click the ▶️ **Run** button above the `main()` method

---

## 🛠️ Build from Source

```powershell
# Navigate to source folder
cd C:\Users\YourName\Desktop\WordScrambledGame\WordScrambledGame

# Compile
javac WordScrambleGame.java

# Run compiled class
java WordScrambleGame
```

---

## ⚙️ Environment Setup (If Java Not Recognized)

1. Press **Windows + R** → type `sysdm.cpl` → Enter
2. Click **Advanced** tab → **Environment Variables**
3. Under **System Variables** → find **Path** → click **Edit**
4. Click **New** and paste:
```
C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin
```
5. Add a new System Variable:
   - Name: `JAVA_HOME`
   - Value: `C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot`
6. Click **OK** on all windows and restart your PC

---

## 🎯 How to Play

```
1. Launch the game
2. A scrambled word appears on screen
3. Type your answer in the input field
4. Press Enter or click Submit
5. Correct? → Next word + timer resets to 60s
6. Wrong? → Try again (timer keeps running)
7. Use Hint to reveal letters into the input field
8. Use Skip to jump to next word (3 skips total)
9. Timer hits 0:00 → Game Over!
10. Try to score as high as possible!
```

---

## 🐛 Known Issues Fixed

- ✅ Java not recognized in PATH — fixed via Environment Variables
- ✅ Hint resetting between words — fixed to be per-word based on length
- ✅ Skip causing game over — fixed, skip is now independent
- ✅ Timer not resetting on correct answer — fixed
- ✅ Attempts/lives system — removed, timer is now the only game over trigger
- ✅ Hint not typing into input field — fixed, hint now fills input field

---

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a **Pull Request**

---

## 📄 License

This project is licensed under the **MIT License**.
See the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

Made with ❤️ by **[Your Name]**

[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=for-the-badge&logo=github)](https://github.com/yourusername)

---

## ⭐ Show Your Support

If you like this project, please give it a ⭐ on GitHub — it means a lot!
