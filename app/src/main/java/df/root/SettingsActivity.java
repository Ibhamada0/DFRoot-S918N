package df.root;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import df.root.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private static final int[] THEME_IDS = { R.id.themeObsidian, R.id.themeAurora, R.id.themeGlacier, R.id.themeEmber, R.id.themeMatrix, R.id.themeGalaxy };
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeIndex = ThemePrefs.index(this);
        setTheme(ThemePrefs.theme(this));
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.themeGroup.check(THEME_IDS[themeIndex]);
        binding.themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int n = 0; n < THEME_IDS.length; ++n) {
                if (THEME_IDS[n] == checkedId && ThemePrefs.index(this) != n) {
                    ThemePrefs.choose(this, n);
                    recreate();
                    break;
                }
            }
        });

        binding.managerGroup.check(ManagerChoice.isNext(this)
            ? R.id.radioNext : R.id.radioClassic);
        binding.managerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            ManagerChoice.save(this, checkedId == R.id.radioNext);
            updateManager();
        });
        binding.btnOfflineInstall.setOnClickListener(v ->
            OfflineManagerInstaller.openOrInstall(this));
        updateManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) updateManager();
    }

    private void updateManager() {
        boolean installed = ManagerChoice.isInstalled(this);
        boolean next = ManagerChoice.isNext(this);
        binding.tvManagerStatus.setText(
            (next ? "KERNELSU NEXT" : "KERNELSU CLASSIC")
            + (installed ? "  •  INSTALLED" : "  •  NOT INSTALLED"));
        binding.btnOfflineInstall.setText(installed ?
            "OPEN INSTALLED MANAGER" : "INSTALL SELECTED MANAGER OFFLINE");
        binding.tvBackendNotice.setText(next
            ? "The bundled RMG backend is tied to KernelSU Next. Even with Next installed, your device firmware must be vulnerable."
            : "Classic installation works offline, but the bundled DFRoot kernel bootstrap still targets Next. Root launch is disabled for Classic to prevent an incompatible payload.");
    }
}
