package husk;

import android.content.ContextWrapper;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public final class KeyboardDismissGestureTest {
    static final class Editor extends View {
        Editor() { super(new ContextWrapper(null)); }
        @Override public boolean getGlobalVisibleRect(Rect out, Point offset) { out.set(10, 20, 110, 70); return true; }
        @Override public void getLocationInWindow(int[] out) { out[0] = 10; out[1] = 20; }
        @Override public void getLocationOnScreen(int[] out) { out[0] = 40; out[1] = 100; }
    }
    static MotionEvent event(int action, long time, float x, float y) {
        return MotionEvent.obtain(0, time, action, x, y, 0);
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void down(KeyboardDismissGesture gesture, View editor, float x, float y) {
        check(gesture.onTouch(event(MotionEvent.ACTION_DOWN, 0, x, y), editor, true, 8) == null,
                "must not dismiss before the control receives its touch");
    }
    public static void main(String[] args) {
        Editor editor = new Editor();
        KeyboardDismissGesture gesture = new KeyboardDismissGesture();
        down(gesture, editor, 200, 200);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 203, 202), editor, true, 0) == editor,
                "a completed outside tap must dismiss");

        // This point is outside window-relative bounds but inside the editor on screen.
        down(gesture, editor, 60, 120);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 60, 120), editor, true, 0) == null,
                "tapping the editor in an offset window must keep the keyboard");

        down(gesture, editor, 200, 200);
        gesture.onTouch(event(MotionEvent.ACTION_MOVE, 40, 230, 200), editor, true, 0);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 200, 200), editor, true, 0) == null,
                "a scroll that returns to its start must not become a tap");

        down(gesture, editor, 200, 200);
        gesture.onTouch(event(MotionEvent.ACTION_CANCEL, 40, 200, 200), editor, true, 0);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 200, 200), editor, true, 0) == null,
                "cancelled input must not dismiss");

        down(gesture, editor, 200, 200);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 500, 200, 200), editor, true, 0) == null,
                "long presses must not dismiss");

        down(gesture, editor, 200, 200);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 200, 200), new Editor(), true, 0) == null,
                "switching editors must keep the keyboard");

        down(gesture, editor, 200, 200);
        gesture.onTouch(MotionEvent.huskObtain(0, 40, MotionEvent.ACTION_POINTER_DOWN | (1 << 8), 2,
                new int[] {0, 1}, new float[] {200, 220}, new float[] {200, 220}), editor, true, 0);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 200, 200), editor, true, 0) == null,
                "multi-touch must not dismiss");

        gesture.onTouch(event(MotionEvent.ACTION_DOWN, 0, 200, 200), editor, false, 8);
        check(gesture.onTouch(event(MotionEvent.ACTION_UP, 80, 200, 200), editor, true, 0) == null,
                "a keyboard opened during the tap must remain visible");
        System.out.println("Tap outside keyboard dismissal: PASS");
    }
}
