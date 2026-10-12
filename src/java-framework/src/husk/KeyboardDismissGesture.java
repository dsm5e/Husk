package husk;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/** A single tap outside the editor, in host screen coordinates. Never consumes a touch. */
final class KeyboardDismissGesture {
    private View editor;
    private final Rect bounds = new Rect();
    private float startX, startY;
    private int slop;
    private long started;

    View onTouch(MotionEvent event, View focused, boolean keyboardShown, int touchSlop) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            editor = null;
            if (!keyboardShown || focused == null || event.getPointerCount() != 1) return null;
            // getGlobalVisibleRect is window-relative; touches from the host are screen-relative.
            if (focused.getGlobalVisibleRect(bounds)) {
                int[] screen = new int[2], window = new int[2];
                focused.getLocationOnScreen(screen);
                focused.getLocationInWindow(window);
                bounds.offset(screen[0] - window[0], screen[1] - window[1]);
            } else bounds.setEmpty();
            startX = event.getRawX(); startY = event.getRawY();
            if (contains(startX, startY)) return null;
            editor = focused;
            slop = touchSlop;
            started = event.getEventTime();
            return null;
        }
        if (editor == null) return null;
        float dx = event.getRawX() - startX, dy = event.getRawY() - startY;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_DOWN
                || event.getPointerCount() != 1 || !keyboardShown || focused != editor
                || dx * dx + dy * dy > slop * slop
                || event.getEventTime() - started >= ViewConfiguration.getLongPressTimeout()) {
            editor = null;
            return null;
        }
        if (action != MotionEvent.ACTION_UP) return null;
        View result = contains(event.getRawX(), event.getRawY()) ? null : editor;
        editor = null;
        return result;
    }

    private boolean contains(float x, float y) {
        return x >= bounds.left && x < bounds.right && y >= bounds.top && y < bounds.bottom;
    }
}
