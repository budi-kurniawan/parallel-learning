package common.gui;

import javafx.scene.control.TextField;

public class NumberField extends TextField {
    
    public NumberField() {
        super();
    }

    public NumberField(String text) {
        super(text);
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (numberOnly(text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (numberOnly(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean numberOnly(String text) {
        return text.matches("[0-9]*");
    }
}