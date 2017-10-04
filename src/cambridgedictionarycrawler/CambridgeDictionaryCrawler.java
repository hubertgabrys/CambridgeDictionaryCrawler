package cambridgedictionarycrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author hubert
 */
public class CambridgeDictionaryCrawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] inputListS = loadDatabase("inputList.dat").toArray(new String[0]);//Loading database

        int numberOfChunks = 10000;
        for (int j = 0; j < numberOfChunks; j++) {
            Deque<Phrase> listOfPhrases = new LinkedList<Phrase>();
            System.out.println("Chunk "+(j+1)+" from " + (int) (j * inputListS.length / numberOfChunks) + " to " + (int) ((j + 1) * inputListS.length / numberOfChunks-1));
            for (int i = (int) (j * inputListS.length / numberOfChunks); i < (int) ((j + 1) * inputListS.length / numberOfChunks); i++) {
                listOfPhrases = checkupPhrases(listOfPhrases, inputListS[i], 20);
                System.out.println("Loaded: " + (i + 1) + "/" + inputListS.length + " " + inputListS[i]);//Information about progress
            }
            new File("output/").mkdir();
            printOutputFR(listOfPhrases, "output/outputFR_"+(j+1)+".txt");
            System.out.println((j+1)+"% done");
        }
        System.out.println("100% done");
//        Deque<Phrase> listOfPhrases = new LinkedList<Phrase>();
//        for (int i = 0; i < inputListS.length; i++) {
//            listOfPhrases = checkupPhrases(listOfPhrases, inputListS[i], 20);
//            System.out.println("Loaded: " + (i + 1) + "/" + inputListS.length + " " + inputListS[i]);//Information about progress
//        }

//        Deque<Phrase> listOfPhrases = new LinkedList<Phrase>();
//        //for (int i = 0; i < inputListS.length; i++) {
//        int i = 172;
//        listOfPhrases = checkupPhrasesTest(listOfPhrases, inputListS[i], 20);
//        System.out.println("Loaded: " + (i + 1) + "/" + inputListS.length + " " + inputListS[i]);//Information about progress
//        //}

        //printOutput(listOfPhrases, "output.txt");
        //printOutputFR(listOfPhrases, "outputFR.txt");
    }

    public static Deque<String> loadDatabase(String filePath) {
        Deque<String> inputList = null;
        if (!new File(filePath).exists()) {
            try {
                System.out.println("Nie znaleziono bazy danych");
                inputList = getInput(24);
                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(new FileOutputStream(filePath));
                oos.writeObject(inputList);
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ObjectInputStream ois = null;
            try {
                System.out.println("Znaleziono plik bazy danych!");
                ois = new ObjectInputStream(new FileInputStream(filePath));
                inputList = (Deque<String>) ois.readObject();
                ois.close();
                System.out.println("Wczytano plik bazy danych!");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ois.close();
                } catch (IOException ex) {
                    Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return inputList;
    }

    public static Deque<String> getInput(int number) {
        try {
            Deque<String> result = new LinkedList<String>();
            int[] numberOfItems = new int[24];
            for (int i = 1; i <= number; i++) {
                int licznik = 0;
                System.out.println(i);
                String url;
                if (i < 10) {
                    url = "http://www.manythings.org/vocabulary/lists/l/words.php?f=3esl.0" + i;
                } else {
                    url = "http://www.manythings.org/vocabulary/lists/l/words.php?f=3esl." + i;
                }
                Document fullBody = Jsoup.connect(url).get();

                System.out.println("Wczytano URL");
                Elements elements = fullBody.select("li");
                for (Element e : elements) {
                    String text = e.text().replaceAll(" ", "-");
                    result.addLast(text);
                    licznik++;
                }
                numberOfItems[i - 1] = licznik;
            }
            for (int i = 0; i < numberOfItems.length; i++) {
                System.out.println(numberOfItems[i]);
            }

            return result;
        } catch (IOException ex) {
            //Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Coś się spieprzyło w iteracji");
            return null;
        }
    }

    public static void printOutput(Phrase input) {
        System.out.println(input.getPos() + " " + input.getName() + " " + input.getPron() + "\n");

        System.out.println("Definition:");
        int i = 1;
        while (!input.getDef().isEmpty()) {
            String text = input.getDef().first().text();
            text = text.replace(input.getName(), "***");
            System.out.println(i + ". " + text);
            input.getDef().remove(0);
            i++;
        }
        System.out.println("\nExamples:");
        i = 1;
        while (!input.getExamples().isEmpty()) {
            String text = input.getExamples().first().text();
            text = text.replace(input.getName(), "***");
            System.out.println(i + ". " + text);
            input.getExamples().remove(0);
            i++;
        }

        System.out.println("===============================\n");
    }

    public static void printOutput(Deque<Phrase> listOfPhrases, String filePath) {
        PrintWriter out = null;
        try {
            int line = 0;
            Deque<String> outputText = new LinkedList<String>();
            while (!listOfPhrases.isEmpty()) {
                Phrase input = listOfPhrases.removeFirst();
                outputText.add(input.getPos() + " " + input.getName() + " " + input.getPron() + "\n");

                outputText.add("Definition:");
                int i = 1;
                while (!input.getDef().isEmpty()) {
                    String text = input.getDef().first().text();
                    text = text.replace(input.getName(), "***");
                    outputText.add(i + ". " + text);
                    input.getDef().remove(0);
                    i++;
                }

                outputText.add("\nExamples:");
                i = 1;
                while (!input.getExamples().isEmpty()) {
                    String text = input.getExamples().first().text();
                    text = text.replace(input.getName(), "***");
                    outputText.add(i + ". " + text);
                    input.getExamples().remove(0);
                    i++;
                }
                outputText.add("===============================\n");
                line++;
            }
            String outputTextFinal = "";
            while (!outputText.isEmpty()) {
                if (outputText.getFirst() != null) {
                    outputTextFinal += (outputText.removeFirst() + "\r\n");
                }
            }

            out = new PrintWriter(filePath);
            out.println(outputTextFinal);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Problem z zapisem do pliku");
        } finally {
            out.close();
        }
    }

    public static void printOutputFR(Deque<Phrase> listOfPhrases, String filePath) {
        PrintWriter out = null;
        try {
            int line = 0;
            Deque<String> outputText = new LinkedList<String>();
            while (!listOfPhrases.isEmpty()) {
                Phrase input = listOfPhrases.removeFirst();
                outputText.add("<q>" + input.getPos() + " " + input.getName() + " " + input.getPron() + "</q>");

                outputText.add("<a><b>Definition:</b>");
                int i = 1;
                while (!input.getDef().isEmpty()) {
                    String text = input.getDef().first().text();
                    text = text.replace(input.getName(), "***");
                    outputText.add(i + ". " + text);
                    input.getDef().remove(0);
                    i++;
                }

                outputText.add("\n<b>Examples:</b>");
                i = 1;
                while (!input.getExamples().isEmpty()) {
                    String text = input.getExamples().first().text();
                    text = text.replace(input.getName(), "***");
                    outputText.add(i + ". " + text);
                    input.getExamples().remove(0);
                    i++;
                }
                outputText.add("</a>\n");
                line++;
            }
            String outputTextFinal = "";
            while (!outputText.isEmpty()) {
                if (outputText.getFirst() != null) {
                    outputTextFinal += (outputText.removeFirst() + "\r\n");
                }
            }

            out = new PrintWriter(filePath);
            out.println(outputTextFinal);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Problem z zapisem do pliku");
        } finally {
            out.close();
        }
    }

    public static void printOutput(Deque<Phrase> listOfPhrases) {
        while (!listOfPhrases.isEmpty()) {
            Phrase input = listOfPhrases.removeFirst();
            System.out.println(input.getPos() + " " + input.getName() + " " + input.getPron() + "\n");

            System.out.println("Definition:");
            int i = 1;
            while (!input.getDef().isEmpty()) {
                String text = input.getDef().first().text();
                text = text.replace(input.getName(), "***");
                System.out.println(i + ". " + text);
                input.getDef().remove(0);
                i++;
            }

            System.out.println("\nExamples:");
            i = 1;
            while (!input.getExamples().isEmpty()) {
                String text = input.getExamples().first().text();
                text = text.replace(input.getName(), "***");
                System.out.println(i + ". " + text);
                input.getExamples().remove(0);
                i++;
            }
            System.out.println("===============================\n");
        }
    }

    public static Phrase checkupPhrase(String name, int number) {
        try {
            String url = "http://dictionary.cambridge.org/dictionary/british/" + name + "_" + number;
            Document fullBody = Jsoup.connect(url).get();

            Document question = Jsoup.parseBodyFragment(fullBody.getElementsByClass("di-head").html());
            Document answer = Jsoup.parseBodyFragment(fullBody.getElementsByClass("di-body").html());
            Document runon = Jsoup.parseBodyFragment(fullBody.getElementsByClass("runon").html());

            Elements pos = question.getElementsByClass("posgram"); //part of speech
            Element ipa = question.getElementsByClass("pron").first(); //pronunciation
            Elements definition = answer.getElementsByClass("def"); //definition
            Elements examples = answer.getElementsByClass("examp"); //redundant examples
            Elements examples_runon = runon.getElementsByClass("examp"); //redundant examples

            //runon removal
            int realExamplesSize = examples.size() - examples_runon.size();
            while (examples.size() > realExamplesSize) {
                examples.remove(examples.last());
            }
            //irregular past verbs removal from examples
            Elements examplesTemp = examples.clone();
            examplesTemp.clear();
            for (Element i : examples) {
                if (i.text().contains(name)) {
                    examplesTemp.add(i);
                }
            }
            examples = examplesTemp.clone();

            //abbreviation of parts of speech

            String pos_string;
            if (!pos.text().isEmpty()) {
                pos_string = pos.text();
                pos_string = pos_string.replace("adjective", "adj.");
                pos_string = pos_string.replace("noun", "n.");
                pos_string = pos_string.replace("adverb", "adv.");
                pos_string = pos_string.replace("verb", "v.");
            } else {
                pos_string = "";
            }

            Phrase word = new Phrase();
            word.setName(name);
            word.setPos(pos_string);
            try {
                word.setPron(ipa.text());
            } catch (NullPointerException ex) {
                System.out.println("No ipa");
            }
            word.setDef(definition);
            word.setExamples(examples);
            return word;
        } catch (IOException ex) {
            //Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            return new Phrase();
        }


    }

    public static Phrase checkupPhraseTest(String name, int number) {
        try {
            String url = "http://dictionary.cambridge.org/dictionary/british/" + name + "_" + number;
            Document fullBody = Jsoup.connect(url).get();

            Document question = Jsoup.parseBodyFragment(fullBody.getElementsByClass("di-head").html());
            Document answer = Jsoup.parseBodyFragment(fullBody.getElementsByClass("di-body").html());
            Document runon = Jsoup.parseBodyFragment(fullBody.getElementsByClass("runon").html());

            Elements pos = question.getElementsByClass("posgram"); //part of speech
            Element ipa = question.getElementsByClass("pron").first(); //pronunciation
            Elements definition = answer.getElementsByClass("def"); //definition
            Elements examples = answer.getElementsByClass("examp"); //examples
            Elements examples_runon = runon.getElementsByClass("examp"); //redundant examples

            Elements elements = answer.select("span.examp > span.eg");
            for (Element e : elements) {
                String text = e.text();
                System.out.println(text);
            }

            System.out.println(answer.getElementsByClass("examp"));
            System.out.println(definition.text());
            //runon removal
            int realExamplesSize = examples.size() - examples_runon.size();
            while (examples.size() > realExamplesSize) {
                examples.remove(examples.last());
            }
            //irregular past verbs removal from examples
            Elements examplesTemp = examples.clone();
            examplesTemp.clear();
            for (Element i : examples) {
                if (i.text().contains(name)) {
                    examplesTemp.add(i);
                }
            }
            examples = examplesTemp.clone();

            //abbreviation of parts of speech
            String pos_string;
            if (!pos.text().isEmpty()) {
                pos_string = pos.text();
                pos_string = pos_string.replace("adjective", "adj.");
                pos_string = pos_string.replace("noun", "n.");
                pos_string = pos_string.replace("adverb", "adv.");
                pos_string = pos_string.replace("verb", "v.");
            } else {
                pos_string = "";
            }

            Phrase word = new Phrase();
            word.setName(name);
            word.setPos(pos_string);
            try {
                word.setPron(ipa.text());
            } catch (NullPointerException ex) {
                System.out.println("No ipa");
            }
            word.setDef(definition);
            word.setExamples(examples);
            return word;
        } catch (IOException ex) {
            //Logger.getLogger(CambridgeDictionaryCrawler.class.getName()).log(Level.SEVERE, null, ex);
            return new Phrase();
        }
    }

    public static Deque<Phrase> checkupPhrases(Deque<Phrase> listOfPhrases, String phraseName, int iterations) {
        for (int i = 1; i < iterations; i++) {
            Phrase phrase = checkupPhrase(phraseName, i);
            if (phrase.getName() != null) {
                if (listOfPhrases.size() > 0 && listOfPhrases.getLast().getPos().equals(phrase.getPos()) && listOfPhrases.getLast().getName().equals(phrase.getName())) { //removal of redundunt phrases by comparing names and pos'.
                    //wyciągnięcie poprzedniego
                    Phrase previous = listOfPhrases.removeLast();
                    //wrzucenie definicji z aktualnego do poprzedniego
                    Elements definitionsNew = phrase.getDef();
                    Elements definitionsOld = previous.getDef();
                    while (!definitionsNew.isEmpty()) {
                        definitionsOld.add(definitionsNew.first());
                        definitionsNew.remove(definitionsNew.first());
                    }
                    //wrzucenie przykładów z aktualnego do poprzedniego
                    Elements examplesNew = phrase.getExamples();
                    Elements examplesOld = previous.getExamples();
                    while (!examplesNew.isEmpty()) {
                        examplesOld.add(examplesNew.first());
                        examplesNew.remove(examplesNew.first());
                    }
                    previous.setDef(definitionsOld);
                    previous.setExamples(examplesOld);
                    listOfPhrases.addLast(previous);
                } else {
                    listOfPhrases.addLast(phrase);
                }
            }
        }
        return listOfPhrases;
    }

    public static Deque<Phrase> checkupPhrasesTest(Deque<Phrase> listOfPhrases, String phraseName, int iterations) {
        for (int i = 1; i < iterations; i++) {
            Phrase phrase = checkupPhraseTest(phraseName, i);
            if (phrase.getName() != null) {
                if (listOfPhrases.size() > 0 && listOfPhrases.getLast().getPos().equals(phrase.getPos()) && listOfPhrases.getLast().getName().equals(phrase.getName())) { //removal of redundunt phrases by comparing names and pos'.
                    //wyciągnięcie poprzedniego
                    Phrase previous = listOfPhrases.removeLast();
                    //wrzucenie definicji z aktualnego do poprzedniego
                    Elements definitionsNew = phrase.getDef();
                    Elements definitionsOld = previous.getDef();
                    while (!definitionsNew.isEmpty()) {
                        definitionsOld.add(definitionsNew.first());
                        definitionsNew.remove(definitionsNew.first());
                    }
                    //wrzucenie przykładów z aktualnego do poprzedniego
                    Elements examplesNew = phrase.getExamples();
                    Elements examplesOld = previous.getExamples();
                    while (!examplesNew.isEmpty()) {
                        examplesOld.add(examplesNew.first());
                        examplesNew.remove(examplesNew.first());
                    }
                    previous.setDef(definitionsOld);
                    previous.setExamples(examplesOld);
                    listOfPhrases.addLast(previous);
                } else {
                    listOfPhrases.addLast(phrase);
                }
            }
        }
        return listOfPhrases;
    }
}