package df.root;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import df.root.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (ManagerChoice.isNext(this)) {
            binding.radioNext.setChecked(true);
        } else {
            binding.radioClassic.setChecked(true);
        }
        binding.managerRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            ManagerChoice.save(this, checkedId != R.id.radioClassic);
            refreshDetails();
        });
        binding.btnInstallSelected.setOnClickListener(v -> {
            Intent installed = getPackageManager().getLaunchIntentForPackage(
                ManagerChoice.packageName(this));
            if (installed != null) {
                startActivity(installed);
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        ManagerChoice.releaseUrl(this))));
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open the official release page",
                        Toast.LENGTH_LONG).show();
                }
            }
        });
        refreshDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) refreshDetails();
    }

    private void refreshDetails() {
        boolean next = ManagerChoice.isNext(this);
        boolean installed = ManagerChoice.isInstalled(this);
        String name = next ? "KernelSU Next" : "KernelSU (Samsung)";
        binding.tvSelectedManager.setText(name);
        binding.tvManagerInstalled.setText(installed
            ? "MANAGER INSTALLED" : "MANAGER NOT INSTALLED");
        binding.tvBackendCompatibility.setText(next
            ? "COMPATIBLE BACKEND • NEXT"
            : "BACKEND NOT AVAILABLE • CLASSIC");
        binding.tvBackendDescription.setText(next
            ? "This DFRoot build uses the bundled KernelSU Next ksud and an in-kernel Next package configuration. Root can be launched with this option after installing the Next Manager."
            : "The original DFRoot project supports manager-aware bootstrapping, but this S918N fork has a Next-only bundled kernel module and ksud. Installing classic KernelSU does NOT switch that exploit backend. Root and start-on-boot are blocked while classic is selected to prevent a mismatched launch.");
        binding.btnInstallSelected.setText(installed
            ? "OPEN SELECTED MANAGER" : "GET SELECTED MANAGER");
        binding.tvSourceInfo.setText(next
            ? "Official source: KernelSU-Next/KernelSU-Next"
            : "Official Samsung-compatible fork: diabl0w/KernelSU");
    }
}
