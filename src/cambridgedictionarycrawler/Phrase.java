/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cambridgedictionarycrawler;

import org.jsoup.select.Elements;

/**
 *
 * @author hubert
 */
public class Phrase {

    private String name = null;
    private String pos = "";
    private String pron = "";
    private Elements definition;
    private Elements examples;

    void setName(String s) {
        name = s;
    }

    void setPos(String s) {
        pos = s;
    }

    void setPron(String s) {
        pron = s;
    }

    void setDef(Elements e) {
        definition = e;
    }

    void setExamples(Elements e) {
        examples = e;
    }
    
    String getName() {
        return name;
    }

    String getPos() {
        return pos;
    }

    String getPron() {
        return pron;
    }

    Elements getDef() {
        return definition;
    }

    Elements getExamples() {
        return examples;
    }
}
