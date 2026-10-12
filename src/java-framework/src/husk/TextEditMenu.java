package husk;

import android.text.Editable;
import android.text.Selection;
import android.widget.TextView;

/** Host edit menu. Only the Android thread reads or changes the editor. */
public final class TextEditMenu {
    // Shared with TLUnityUIView's edit menu. No text is sent to iOS until Copy/Cut is chosen.
    public static final int CUT = 1, COPY = 2, PASTE = 4, SELECT_ALL = 8;
    private static int session;
    private static TextView editor;
    private static String snapshot;
    private static int start, end;
    private static float x, y;
    private TextEditMenu() {}

    public static int actions(boolean editable, boolean password, int length, int start, int end) {
        int result = editable ? PASTE : 0;
        if (length > 0) result |= SELECT_ALL;
        if (!password && start >= 0 && end >= 0 && start != end) {
            result |= COPY;
            if (editable) result |= CUT;
        }
        return result;
    }

    public static boolean show(TextView view, float screenX, float screenY) {
        int actions = view.huskEditActions();
        if (actions == 0) return false;
        editor = view;
        snapshot = view.getText().toString();
        start = view.getSelectionStart(); end = view.getSelectionEnd();
        x = screenX; y = screenY;
        Native.showTextMenu(++session, actions, x, y);
        return true;
    }

    public static void dismiss() {
        if (editor == null) return;
        editor = null; snapshot = null;
        Native.showTextMenu(++session, 0, 0, 0);
    }

    /** m<session>:<action>:<paste text>; split only the header, preserving newlines and colons. */
    static void deliver(String event) {
        int first = event.indexOf(':', 1), second = event.indexOf(':', first + 1);
        if (first < 0 || second < 0) return;
        int token, action;
        try {
            token = Integer.parseInt(event.substring(1, first));
            action = Integer.parseInt(event.substring(first + 1, second));
        } catch (NumberFormatException e) { return; }
        TextView view = editor;
        if (token != session || view == null) return;
        if (!view.isAttachedToWindow() || !view.isEnabled() || !view.getText().toString().equals(snapshot)
                || view.getSelectionStart() != start || view.getSelectionEnd() != end) { dismiss(); return; }
        if ((view.huskEditActions() & action) == 0) return;
        dismiss();
        switch (action) {
        case CUT: view.onTextContextMenuItem(android.R.id.cut); break;
        case COPY: view.onTextContextMenuItem(android.R.id.copy); break;
        case PASTE: view.huskPasteText(event.substring(second + 1)); break;
        case SELECT_ALL:
            view.onTextContextMenuItem(android.R.id.selectAll);
            show(view, x, y);
            break;
        }
    }

    /** Keep filters and text watchers in the normal Editable path, including filtered paste lengths. */
    public static void replaceSelection(Editable text, CharSequence replacement) {
        int a = Selection.getSelectionStart(text), b = Selection.getSelectionEnd(text);
        if (a < 0 || b < 0) a = b = text.length();
        int start = Math.min(text.length(), Math.min(a, b)), end = Math.min(text.length(), Math.max(a, b));
        int previousLength = text.length();
        text.replace(start, end, replacement);
        int cursor = Math.max(0, Math.min(text.length(), end + text.length() - previousLength));
        Selection.setSelection(text, cursor);
    }
}
