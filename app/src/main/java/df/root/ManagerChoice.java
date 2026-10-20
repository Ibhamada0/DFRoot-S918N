package df.root;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public final class ManagerChoice {
    private static final String PREF_FILE = "dfroot_manager_settings";
    private static final String KEY = "selected_manager";
    private static final String NEXT = "next";
    private static final String CLASSIC = "classic";

    private ManagerChoice() {}

    private static SharedPreferences prefs(Context ctx) {
        // Device-protected storage also works when BootReceiver runs before first unlock.
        return ctx.createDeviceProtectedStorageContext()
            .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public static boolean isNext(Context ctx) {
        return !CLASSIC.equals(prefs(ctx).getString(KEY, NEXT));
    }

    public static void save(Context ctx, boolean next) {
        prefs(ctx).edit().putString(KEY, next ? NEXT : CLASSIC).apply();
    }

    public static String packageName(Context ctx) {
        return isNext(ctx) ? "com.rifsxd.ksunext" : "me.weishu.kernelsu";
    }

    public static String releaseUrl(Context ctx) {
        return isNext(ctx)
            ? "https://github.com/KernelSU-Next/KernelSU-Next/releases/latest"
            : "https://github.com/diabl0w/KernelSU/releases/latest";
    }

    public static boolean isInstalled(Context ctx) {
        try {
            // Both manager package IDs are listed in manifest <queries>.
            ctx.getPackageManager().getApplicationInfo(packageName(ctx), 0);
            return true;
        } catch (PackageManager.NameNotFoundException ex) {
            return false;
        }
    }

    public static boolean isBackendSupported(Context ctx) {
        // This fork's kernel module hardcodes the Next package and bundles Next ksud.
        // Do NOT pretend selecting the classic APK also changes the exploit backend.
        return isNext(ctx);
    }

    public static boolean canLaunch(Context ctx) {
        return isBackendSupported(ctx) && isInstalled(ctx);
    }
}
