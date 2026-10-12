package husk;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;

public final class TextEditMenuTest {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void selection(Editable text, int position) {
        check(Selection.getSelectionStart(text) == position && Selection.getSelectionEnd(text) == position,
                "cursor must follow the actual inserted text");
    }
    public static void main(String[] args) {
        int editable = TextEditMenu.actions(true, false, 6, 0, 6);
        check(editable == 15, "selected editable text offers all four actions");
        check(TextEditMenu.actions(true, false, 0, 0, 0) == TextEditMenu.PASTE,
                "an empty field must still offer paste");
        check(TextEditMenu.actions(false, false, 6, 0, 6) == (TextEditMenu.COPY | TextEditMenu.SELECT_ALL),
                "read-only selections must not offer cut or paste");
        check((TextEditMenu.actions(true, true, 6, 0, 6) & (TextEditMenu.COPY | TextEditMenu.CUT)) == 0,
                "password fields must not export their text");
        check((TextEditMenu.actions(true, false, 6, 3, 3) & (TextEditMenu.COPY | TextEditMenu.CUT)) == 0,
                "a caret alone cannot be copied or cut");

        SpannableStringBuilder text = new SpannableStringBuilder("hello world");
        Selection.setSelection(text, 11, 6);
        TextEditMenu.replaceSelection(text, "VK");
        check(text.toString().equals("hello VK"), "paste replaces a reversed selection");
        selection(text, 8);
        TextEditMenu.replaceSelection(text, "\nhttps://example.test:443/🙂");
        check(text.toString().equals("hello VK\nhttps://example.test:443/🙂"), "paste preserves punctuation, Unicode and newlines");
        selection(text, text.length());

        text = new SpannableStringBuilder("abcd");
        Selection.setSelection(text, 1, 3);
        text.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
        final int[] changes = {0};
        text.setSpan(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { changes[0]++; }
            public void afterTextChanged(Editable s) {}
        }, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        TextEditMenu.replaceSelection(text, "123456");
        check(text.toString().equals("a123d"), "paste must respect the app's input filters");
        check(changes[0] == 1, "paste must notify the app's text watcher once");
        selection(text, 4);
        Selection.setSelection(text, 1, 4);
        TextEditMenu.replaceSelection(text, "");
        check(text.toString().equals("ad"), "cut removes only the selection");
        selection(text, 1);

        text = new SpannableStringBuilder("end");
        TextEditMenu.replaceSelection(text, "!");
        check(text.toString().equals("end!"), "missing selection defaults to the end");
        selection(text, 4);
        TextEditMenu.deliver("mgarbage");
        TextEditMenu.deliver("mx:y:ignored");
        TextEditMenu.deliver("m123:4:stale callback");
        System.out.println("Text edit menu actions: PASS");
    }
}
