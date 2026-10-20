package df.root;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

public final class OfflineManagerInstaller {
    private static final String NEXT_ASSET = "managers/ksu-next-v3.4.2.apk";
    private static final String NEXT_SHA =
        "774a5511f7034a4930e881114e202319cbdc34c5aaaa5c7c6b6d2902364e4c58";
    private static final String CLASSIC_ASSET = "managers/ksu-samsung-v2.2.apk";
    private static final String CLASSIC_SHA =
        "761dffad54ae080a4977971db7b04e2fc65c2d9b5e2336accca4a85661290a69";

    private OfflineManagerInstaller() {}

    public static void openOrInstall(Activity activity) {
        Intent installed = activity.getPackageManager()
            .getLaunchIntentForPackage(ManagerChoice.packageName(activity));
        if (installed != null) {
            activity.startActivity(installed);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            Toast.makeText(activity,
                "Allow RMG to install apps, then tap again. Android will ask you to confirm.",
                Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + activity.getPackageName())));
            return;
        }
        boolean next = ManagerChoice.isNext(activity);
        final String asset = next ? NEXT_ASSET : CLASSIC_ASSET;
        final String expected = next ? NEXT_SHA : CLASSIC_SHA;
        try {
            File output = new File(new File(activity.getCacheDir(), "offline_managers"),
                next ? "ksu-next.apk" : "ksu-classic.apk");
            if (!output.getParentFile().exists() && !output.getParentFile().mkdirs())
                throw new IllegalStateException("Could not create install cache");

            MessageDigest dig = MessageDigest.getInstance("SHA-256");
            try (InputStream in = activity.getAssets().open(asset);
                 FileOutputStream out = new FileOutputStream(output, false)) {
                byte[] buffer = new byte[65536];
                for (int n; (n = in.read(buffer)) != -1;) {
                    dig.update(buffer, 0, n);
                    out.write(buffer, 0, n);
                }
                out.getFD().sync();
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : dig.digest()) hex.append(String.format(Locale.ROOT,"%02x",b&255));
            if (!expected.contentEquals(hex)) {
                output.delete();
                throw new SecurityException("Offline APK checksum mismatch; installation blocked");
            }
            Uri uri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", output);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.setClipData(ClipData.newRawUri("Verified KernelSU Manager", uri));
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(install); // Android system requires explicit user approval.
        } catch (Exception ex) {
            Toast.makeText(activity, "Offline install unavailable: " + ex.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }
}
