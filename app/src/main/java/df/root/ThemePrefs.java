package df.root;

import android.content.Context;

public final class ThemePrefs {
    private static final int[] STYLES = {
        R.style.Theme_RMG_Obsidian,
        R.style.Theme_RMG_Aurora,
        R.style.Theme_RMG_Glacier,
        R.style.Theme_RMG_Ember,
        R.style.Theme_RMG_Matrix,
        R.style.Theme_RMG_Galaxy
    };
    private ThemePrefs() {}

    public static int index(Context ctx) {
        int n = ctx.getSharedPreferences("rmg_ui", Context.MODE_PRIVATE).getInt("theme", 0);
        return n >= 0 && n < STYLES.length ? n : 0;
    }

    public static int theme(Context ctx) {
        return STYLES[index(ctx)];
    }

    public static void choose(Context ctx, int selected) {
        if (selected < 0 || selected >= STYLES.length) throw new IllegalArgumentException("theme");
        ctx.getSharedPreferences("rmg_ui", Context.MODE_PRIVATE).edit()
            .putInt("theme", selected).apply();
    }

    public static int color(Context ctx, int attribute) {
        android.util.TypedValue v = new android.util.TypedValue();
        if (!ctx.getTheme().resolveAttribute(attribute, v, true))
            throw new IllegalStateException("Missing theme color: " + attribute);
        if (v.resourceId != 0) return androidx.core.content.ContextCompat.getColor(ctx,v.resourceId);
        return v.data;
    }
}
