package flashcards;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
 
public class Main {
    public static void main(String[] args) {
 
        FlashCardsGame flashCardsGame = new FlashCardsGame(args);
        flashCardsGame.play();
 
    }
 
    static class FlashCardsGame {
 
        private Scanner scanner;
        private Map<String, String> cards;
        private Map<String, Integer> stats;
        private List<String> logs;
        private boolean exportBeforeExit = false;
        private File exportFile;
 
        FlashCardsGame(String[] args) {
            scanner = new Scanner(System.in);
            cards = new HashMap<>();
            stats = new HashMap<>();
            logs = new ArrayList<>();
 
            initializeGame(args);
        }
 
        private void initializeGame(String[] args) {
            List<String> argsList = Arrays.asList(args);
 
            if (argsList.contains("-import")) {
                String importPath = argsList.get(argsList.indexOf("-import") + 1);
                File importFile = new File(importPath);
                readCardsFromFile(importFile);
            }
 
            if (argsList.contains("-export")) {
                String exportPath = argsList.get(argsList.indexOf("-export") + 1);
                exportFile = new File(exportPath);
                exportBeforeExit = true;
            }
        }
 
        void play() {
            String action = "";
            while (!"exit".equals(action)) {
                writeAndLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats");
                action = readAndLog();
                performAction(action);
            }
 
        }
 
        private void performAction(String action) {
            switch (action) {
                case "exit":
                    exit();
                    break;
                case "ask":
                    askCards();
                    break;
                case "export":
                    exportCards();
                    break;
                case "import":
                    importCards();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "add":
                    addCard();
                    break;
                case "log":
                    exportLogs();
                    break;
                case "hardest card":
                    printHardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                default:
                    System.out.println("Wrong command. Try again.");
                    break;
            }
        }
 
        private void printHardestCard() {
            int highestMistakeCount = getHighestMistakeCount();
            String feedback = "";
            if (highestMistakeCount == 0) {
                feedback = "There are no cards with errors";
            } else {
                List<String> cardsWithErrors = new ArrayList<>();
                for (String term: stats.keySet()) {
                    if (stats.get(term) == highestMistakeCount) {
                        cardsWithErrors.add(term);
                    }
                }
                if (cardsWithErrors.size() == 1) {
                    feedback = "The hardest card is \"" + cardsWithErrors.get(0) + "\". You have " +
                            highestMistakeCount + " errors answering it.";
                } else {
                    feedback = "The hardest cards are ";
                    for (String card: cardsWithErrors) {
                        feedback += "\"" + card + "\", ";
                    }
                    feedback = feedback.substring(0, feedback.length() - 2) + ". You have " +
                            highestMistakeCount + "errors answering them";
                }
            }
 
            writeAndLog(feedback);
        }
 
        private int getHighestMistakeCount() {
            int highestMistakeCount = 0;
            for (Integer mistakes: stats.values()) {
                if (mistakes > highestMistakeCount) {
                    highestMistakeCount = mistakes;
                }
            }
            return highestMistakeCount;
        }
 
        private void resetStats() {
 
            for (String term: stats.keySet()) {
                stats.put(term, 0);
            }
 
            writeAndLog("Card statistics has been reset.");
        }
 
        private void exportLogs() {
            File file = getAndLogFile();
            boolean error = false;
 
            try (PrintWriter printWriter = new PrintWriter(file)) {
 
                for (String log : logs) {
                    printWriter.println(log);
                }
 
            } catch (FileNotFoundException e) {
                error = true;
            }
 
            String feedback = error ? "File not found." : "The log has been saved";
            writeAndLog(feedback);
 
        }
 
        private void importCards() {
            File importFile = getAndLogFile();
            readCardsFromFile(importFile);
        }
 
        private void exportCards() {
            File file = getAndLogFile();
            writeCardsToFile(file);
        }
 
 
        private void removeCard() {
            writeAndLog("The card");
            String term = readAndLog();
            if (cards.containsKey(term)) {
                cards.remove(term);
                stats.remove(term);
                writeAndLog("The card has been removed");
            } else {
                writeAndLog("Can't remove \"" + term + "\": there is no such card.");
            }
        }
 
        private void addCard() {
            writeAndLog("The card:");
            String term = readAndLog();
            if (cards.containsKey(term)) {
                writeAndLog("The card \"" + term + "\" already exists.");
            } else {
                writeAndLog("The definition of the card:");
                String definition = readAndLog();
 
                if (cards.containsValue(definition)) {
                    writeAndLog("The definition \"" + definition + "\" already exists");
                } else {
                    cards.put(term, definition);
                    stats.put(term, 0);
                    writeAndLog("The pair (\"" + term + "\":\"" + definition + "\") has been added.");
                }
            }
        }
 
        void exit() {
            writeAndLog("Bye bye!");
            if (exportBeforeExit) {
                writeCardsToFile(exportFile);
            }
        }
 
        private void askCards() {
            Random random = new Random();
            String[] terms = cards.keySet().toArray(new String[0]);
            int chosenCard = random.nextInt(terms.length);
 
            int numberOfQuestions = 0;
            String term = "";
            String correctTerm = "";
            String definition = "";
            String answer = "";
            String feedback = "";
 
            writeAndLog("How many times to ask?");
            numberOfQuestions = readAndLogNumberOfQuestions();
 
            for (int i = 0; i < numberOfQuestions; i++) {
                term = terms[chosenCard];
                writeAndLog("Print the definition of \"" + term + "\":");
                answer = readAndLog();
                definition = cards.get(term);
                if (definition.equals(answer)) {
                    feedback = "Correct answer.";
                } else {
                    stats.put(term, stats.get(term) + 1);
                    feedback = "Wrong answer. The correct one is \"" + definition + "\"";
                    if (cards.containsValue(answer)) {
                        for (String question : terms) {
                            if (cards.get(question).equals(answer)) {
                                correctTerm = question;
                            }
                        }
                        feedback += ", you've just written the definition of \"" + correctTerm + "\"";
                        correctTerm = "";
                    }
                    feedback += ".";
                }
 
                writeAndLog(feedback);
                chosenCard = random.nextInt(terms.length);
            }
        }
 
        private void readCardsFromFile(File importFile) {
            int count = 0;
            String term = "";
            String definition = "";
            Integer numberOfMistakes = 0;
            boolean error = false;
 
            try (Scanner fileScanner = new Scanner(importFile)) {
 
                while (fileScanner.hasNext()) {
                    term = fileScanner.nextLine();
                    definition = fileScanner.nextLine();
                    numberOfMistakes = Integer.valueOf(fileScanner.nextLine());
                    cards.put(term, definition);
                    stats.put(term, numberOfMistakes);
                    count++;
                }
 
            } catch (FileNotFoundException e) {
                error = true;
            }
 
            String feedback = error ? "File not found." : (count + " cards have been loaded");
            writeAndLog(feedback);
        }
 
        private void writeCardsToFile(File exportFile) {
            int count = 0;
            boolean error = false;
 
            try (PrintWriter printWriter = new PrintWriter(exportFile)) {
 
                for (var entry : cards.entrySet()) {
                    printWriter.println(entry.getKey());
                    printWriter.println(entry.getValue());
                    printWriter.println(stats.get(entry.getKey()));
                    count++;
                }
 
            } catch (FileNotFoundException e) {
                error = true;
            }
 
            String feedback = error ? "File not found." : (count + " cards have been saved");
            writeAndLog(feedback);
        }
 
        private File getAndLogFile() {
            writeAndLog("File name:");
            String filePath = readAndLog();
            return new File(filePath);
        }
 
        private int readAndLogNumberOfQuestions() {
            return Integer.parseInt(readAndLog());
        }
 
        private void writeAndLog(String output) {
            System.out.println(output);
            logs.add(output);
        }
 
        private String readAndLog() {
            String input = scanner.nextLine();
            logs.add(input);
            return  input;
        }
 
    }
 
}
