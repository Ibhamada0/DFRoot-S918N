package df.root;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import df.root.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements IReporter {
    private static final String TAG = "dfroot";
    private ActivityMainBinding binding;
    private int appliedTheme;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean running = false;

    @Override
    public void report(String msg) {
        Log.i(TAG, msg.trim());
        main.post(() -> {
            if (isFinishing() || isDestroyed()) return;
            binding.outputView.append(msg);
            binding.outputScroll.post(() ->
                binding.outputScroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appliedTheme = ThemePrefs.index(this);
        setTheme(ThemePrefs.theme(this));
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.tvDeviceModel.setText(Build.MANUFACTURER + " " + Build.MODEL);
        binding.tvDeviceBuild.setText(Build.DISPLAY);
        binding.tvKernel.setText(System.getProperty("os.version", "Unavailable"));
        refreshSelinux();

        binding.btnRun.setOnClickListener(v -> {
            if (!ManagerChoice.canLaunch(this)) {
                refreshRootState();
                Toast.makeText(this, "Choose a supported installed Manager in Settings", Toast.LENGTH_LONG).show();
                return;
            }
            if (running || new File("/dev/df").exists()) return;
            running = true;
            setStatus("WORKING", "Executing DFRoot. Do not restart your phone during this step.",
                R.color.df_ice);
            binding.progressRoot.setIndeterminate(true);
            binding.progressRoot.setVisibility(View.VISIBLE);
            binding.btnRun.setEnabled(false);
            binding.outputView.setText("> Starting DFRoot session...\n");
            boolean softReboot = binding.switchManualSoftReboot.isChecked();
            executor.execute(() -> runExploit(softReboot));
        });

        binding.btnManager.setOnClickListener(v -> openManager());
        binding.btnCopyLogs.setOnClickListener(v -> {
            ClipboardManager cm =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("RMG logs",
                    binding.outputView.getText()));
                Toast.makeText(this, "Logs copied", Toast.LENGTH_SHORT).show();
            }
        });

        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        int state = getPackageManager().getComponentEnabledSetting(receiver);
        boolean bootEnabled = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        binding.switchBootStart.setChecked(bootEnabled);
        binding.switchBootStart.setOnCheckedChangeListener((btn, checked) -> {
            getPackageManager().setComponentEnabledSetting(receiver,
                checked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
            binding.switchAutoSoftReboot.setEnabled(checked);
        });

        boolean autoReboot = createDeviceProtectedStorageContext()
            .getSharedPreferences("dfroot", MODE_PRIVATE)
            .getBoolean("auto_soft_reboot", true);
        binding.switchAutoSoftReboot.setChecked(autoReboot);
        binding.switchAutoSoftReboot.setEnabled(bootEnabled);
        binding.switchAutoSoftReboot.setOnCheckedChangeListener((btn, checked) ->
            createDeviceProtectedStorageContext()
                .getSharedPreferences("dfroot", MODE_PRIVATE)
                .edit().putBoolean("auto_soft_reboot", checked).apply());
        refreshRootState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ThemePrefs.index(this) != appliedTheme) {
            recreate();
            return;
        }
        if (binding != null && !running) {
            refreshRootState();
            refreshSelinux();
        }
    }

    private void openManager() {
        OfflineManagerInstaller.openOrInstall(this);
    }

    private void updateManagerButton() {
        String name = ManagerChoice.isNext(this) ? "KERNELSU NEXT" : "KERNELSU CLASSIC";
        binding.btnManager.setText(
            (ManagerChoice.isInstalled(this) ? "OPEN " : "INSTALL OFFLINE ") + name);
    }

    private void refreshRootState() {
        if (running) return;
        boolean active = new File("/dev/df").exists();
        boolean next = ManagerChoice.isNext(this);
        boolean installed = ManagerChoice.isInstalled(this);
        binding.btnRun.setEnabled(!active && ManagerChoice.canLaunch(this));
        binding.btnRun.setText(active ? "ROOT SESSION ACTIVE"
            : !next ? "CLASSIC BACKEND UNAVAILABLE"
            : !installed ? "INSTALL MANAGER FIRST" : "START ROOT SESSION");

        if (active) {
            setStatus("ROOT ACTIVE",
                "DFRoot marker found. Changing the manager selection does not switch an active root backend.",
                R.color.df_good);
        } else if (!next) {
            setStatus("CLASSIC BACKEND UNAVAILABLE",
                "Classic KernelSU Manager is selectable for installation, but the bundled root backend is Next-only. Select Next to enable root.",
                R.color.df_warning);
        } else if (!installed) {
            setStatus("MANAGER REQUIRED",
                "Install KernelSU Next Manager before launching the bundled Next root backend.",
                R.color.df_warning);
        } else {
            setStatus("NOT ACTIVE",
                "KernelSU Next selected. Firmware compatibility is not guaranteed.",
                R.color.df_ice);
        }
        updateManagerButton();
    }

    private void setStatus(String label, String detail, int color) {
        binding.tvStatus.setText(label);
        int themeAttr = color == R.color.df_good ? R.attr.rmgGood
            : color == R.color.df_warning ? R.attr.rmgWarning : R.attr.rmgIce;
        binding.tvStatus.setTextColor(ThemePrefs.color(this, themeAttr));
        binding.tvStatusDetail.setText(detail);
    }

    private void refreshSelinux() {
        try (BufferedReader r = new BufferedReader(
                new FileReader("/sys/fs/selinux/enforce"))) {
            String v = r.readLine();
            binding.tvSelinux.setText("1".equals(v) ? "Enforcing" :
                "0".equals(v) ? "Permissive" : "Unavailable");
        } catch (Exception ignored) {
            binding.tvSelinux.setText("Unavailable (restricted)");
        }
    }

    private void runExploit(boolean softReboot) {
        int code = -1;
        String error = null;
        try {
            code = ExploitRunner.run(this, this, softReboot);
        } catch (Exception e) {
            Log.e(TAG, "exploit exception", e);
            error = e.toString();
            report("\nException: " + error + "\n");
        }
        final int result = code;
        final String detail = error;
        main.post(() -> {
            if (isFinishing() || isDestroyed()) return;
            running = false;
            binding.progressRoot.setVisibility(View.GONE);
            refreshSelinux();
            if (new File("/dev/df").exists()) {
                refreshRootState();
                Toast.makeText(this, "DFRoot session active", Toast.LENGTH_LONG).show();
            } else {
                binding.btnRun.setEnabled(true);
                binding.btnRun.setText("RETRY ROOT SESSION");
                String reason = detail != null ? "Exception: " + detail :
                    result == 0 ? "Exploit returned success, but no active root marker was found." :
                    result == 1 ? "KernelSU daemon exited with an error." :
                    result == 2 ? "Check the console for details." :
                    "Could not patch required files.";
                setStatus("NOT ACTIVE", reason, R.color.df_warning);
                Toast.makeText(this, "DFRoot did not confirm an active session",
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}
