import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

public final class ContextWrapperTest {
    static final BroadcastReceiver RECEIVER = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {}
    };
    static final IntentFilter FILTER = new IntentFilter("test.RECEIVE");
    static final Handler HANDLER = new Handler((Looper) null);

    static class RecordingContext extends ContextWrapper {
        int calls, overload, flags;
        String permission;
        Handler handler;
        final Intent sticky = new Intent("test.STICKY");
        RecordingContext() { super(null); }
        Intent record(int n, BroadcastReceiver r, IntentFilter f, String p, Handler h, int options) {
            check(r == RECEIVER && f == FILTER, "receiver and filter must reach the base context");
            calls++; overload = n; permission = p; handler = h; flags = options;
            return sticky;
        }
        @Override public Intent registerReceiver(BroadcastReceiver r, IntentFilter f) { return record(2, r, f, null, null, 0); }
        @Override public Intent registerReceiver(BroadcastReceiver r, IntentFilter f, int flags) { return record(3, r, f, null, null, flags); }
        @Override public Intent registerReceiver(BroadcastReceiver r, IntentFilter f, String p, Handler h) { return record(4, r, f, p, h, 0); }
        @Override public Intent registerReceiver(BroadcastReceiver r, IntentFilter f, String p, Handler h, int flags) { return record(5, r, f, p, h, flags); }
    }

    // Apps targeting modern Android commonly add receiver flags through ContextCompat.
    // Its five-argument call must reach the base, not re-enter this two-argument override.
    static final class ModernApp extends ContextWrapper {
        int entries;
        ModernApp(Context base) { super(base); }
        @Override public Intent registerReceiver(BroadcastReceiver r, IntentFilter f) {
            check(++entries == 1, "registerReceiver re-entered the app override");
            return registerReceiver(r, f, "test.PERMISSION", HANDLER, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        RecordingContext base = new RecordingContext();
        ModernApp app = new ModernApp(new ContextWrapper(base));
        check(app.registerReceiver(RECEIVER, FILTER) == base.sticky, "sticky result must be returned");
        check(base.calls == 1 && base.overload == 5, "modern registration must reach the base once");
        check(base.flags == Context.RECEIVER_NOT_EXPORTED && "test.PERMISSION".equals(base.permission)
                && base.handler == HANDLER, "registration options must be preserved");

        ContextWrapper wrapper = new ContextWrapper(base);
        check(wrapper.registerReceiver(RECEIVER, FILTER) == base.sticky && base.overload == 2, "two-argument dispatch");
        check(wrapper.registerReceiver(RECEIVER, FILTER, Context.RECEIVER_EXPORTED) == base.sticky
                && base.overload == 3 && base.flags == Context.RECEIVER_EXPORTED, "flagged dispatch");
        check(wrapper.registerReceiver(RECEIVER, FILTER, "test.OTHER", HANDLER) == base.sticky
                && base.overload == 4 && base.handler == HANDLER && "test.OTHER".equals(base.permission), "permission dispatch");
        System.out.println("ContextWrapper receiver registration: PASS");
    }
}
