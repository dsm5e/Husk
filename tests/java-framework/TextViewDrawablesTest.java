import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import java.lang.reflect.Field;

public final class TextViewDrawablesTest {
    static final class Icon extends Drawable {
        public void draw(Canvas c) {}
        public void setAlpha(int alpha) {}
        public void setColorFilter(ColorFilter filter) {}
        public int getOpacity() { return -3; }
        public int getIntrinsicWidth() { return 12; }
        public int getIntrinsicHeight() { return 14; }
    }
    static final class TintedTextView extends TextView {
        int absoluteCalls;
        TintedTextView() { super(null); }
        @Override public void setCompoundDrawables(Drawable l, Drawable t, Drawable r, Drawable b) {
            if (++absoluteCalls > 1) throw new AssertionError("relative setter reentered the tinted absolute setter");
            super.setCompoundDrawablesRelative(l, t, r, b);
        }
        @Override public void requestLayout() {}
        @Override public void invalidate() {}
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        // These setter tests need no Android resource inflation or native font setup.
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field singleton = unsafeClass.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        Object unsafe = singleton.get(null);
        TintedTextView view = (TintedTextView) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, TintedTextView.class);
        Field drawables = TextView.class.getDeclaredField("mDrawables");
        drawables.setAccessible(true);
        drawables.set(view, new Drawable[4]);

        Icon first = new Icon(), second = new Icon();
        view.setCompoundDrawables(first, null, null, null);
        check(view.absoluteCalls == 1, "the tinted setter must execute only once");
        check(view.getCompoundDrawablesRelative()[0] == first, "the drawable must be installed");
        check(first.getCallback() == view, "the installed drawable must invalidate its view");
        view.absoluteCalls = 0;
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(second, null, first, null);
        check(view.absoluteCalls == 0, "relative intrinsic bounds must not call the absolute setter");
        check(second.getBounds().width() == 12 && second.getBounds().height() == 14, "intrinsic bounds must be applied");
        Drawable[] copy = view.getCompoundDrawablesRelative();
        copy[0] = null;
        check(view.getCompoundDrawablesRelative()[0] == second, "the getter must return a copy");
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        check(view.absoluteCalls == 0, "relative resource overload must stay relative");
        check(first.getCallback() == null && second.getCallback() == null, "replaced drawables must release their callback");
        check(view.getCompoundDrawables()[0] == null, "zero resource IDs must clear drawables");
        System.out.println("TextView drawable dispatch: PASS");
    }
}
