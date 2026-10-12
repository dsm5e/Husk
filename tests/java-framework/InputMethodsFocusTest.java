package husk;

import android.content.ContextWrapper;
import android.view.View;

public final class InputMethodsFocusTest {
    static final class Editor extends View {
        boolean attached = true, visible = true, focused = true, windowFocused = true;
        Editor() { super(new ContextWrapper(null)); }
        @Override public boolean isAttachedToWindow() { return attached; }
        @Override public boolean isShown() { return visible; }
        @Override public boolean isFocused() { return focused; }
        @Override public boolean hasWindowFocus() { return windowFocused; }
        @Override public boolean onCheckIsTextEditor() { return true; }
        // Like WebView, no Android InputConnection: editing belongs to the native host.
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        Editor previous = new Editor(), web = new Editor();
        check(InputMethods.canShowFor(previous), "the visible focused Android editor can request its keyboard");
        InputMethods.focusChanged(null, previous);
        previous.focused = false;
        InputMethods.focusChanged(previous, web);
        check(InputMethods.focused() == web, "web focus replaces the previous Android editor");
        check(!InputMethods.show(previous), "a delayed show from the old editor must be rejected");
        check(InputMethods.focused() == web, "a rejected request must not steal web focus");
        check(!InputMethods.show(web), "a host editor without an InputConnection must not open the Android keyboard");
        web.attached = false;
        check(!InputMethods.show(web), "detached editors cannot show the keyboard");
        web.attached = true; web.visible = false;
        check(!InputMethods.show(web), "hidden editors cannot show the keyboard");
        web.visible = true; web.windowFocused = false;
        check(!InputMethods.show(web), "a background window cannot show the keyboard");
        check(!InputMethods.show(null), "a missing editor cannot show the keyboard");
        System.out.println("Keyboard focus ownership: PASS");
    }
}
