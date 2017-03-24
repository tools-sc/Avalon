package util;

import java.util.function.Consumer;

/**
 * Created by Eldath Ray on 2017/3/19 0019.
 *
 * @author Eldath Ray
 */
public class GOption {
    private char option;
    private String question;
    private Consumer<Boolean> sequel;

    public GOption(char option, String question, Consumer<Boolean> sequel) {
        this.option = option;
        this.question = question;
        this.sequel = sequel;
    }

    public char getOption() {
        return option;
    }

    public String getQuestion() {
        return question;
    }

    public void doSequel() {
        sequel.accept(true);
    }
}