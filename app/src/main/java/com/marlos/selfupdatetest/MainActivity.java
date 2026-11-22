
package com.marlos.selfupdatetest;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marlos.selfupdate.core.UpdateManager;
import com.marlos.selfupdate.core.UpdateConfig;
import com.marlos.selfupdate.model.UpdateInfo;
import com.marlos.selfupdate.model.UpdateResult;

public class MainActivity extends Activity {

    private TextView status;
    private TextView updateIndicator;

    private Button checkButton;
    private Button installButton;

    private UpdateManager updateManager;
    private UpdateInfo availableUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UpdateConfig config = new UpdateConfig(
                "https://raw.githubusercontent.com/marlos-dev-em-dev/self_update/main/version/update.json",
                getPackageName()
        );

        updateManager = new UpdateManager(this, config);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("SELFUPDATE TEST");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Versão instalada: " + BuildConfig.VERSION_NAME
                + "\nPronto para jogar.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 32, 0, 32);

        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);
        buttonContainer.setPadding(0, 8, 0, 8);

        checkButton = new Button(this);
        checkButton.setText("Verificar atualizações");
        checkButton.setOnClickListener(v -> checkForUpdates(true));

        updateIndicator = new TextView(this);
        updateIndicator.setVisibility(View.GONE);

        GradientDrawable redDot = new GradientDrawable();
        redDot.setShape(GradientDrawable.OVAL);
        redDot.setColor(Color.RED);
        updateIndicator.setBackground(redDot);

        LinearLayout.LayoutParams indicatorParams =
                new LinearLayout.LayoutParams(16, 16);
        indicatorParams.setMargins(8, 0, 0, 0);

        buttonContainer.addView(checkButton);
        buttonContainer.addView(updateIndicator, indicatorParams);

        installButton = new Button(this);
        installButton.setText("Baixar e instalar atualização");
        installButton.setVisibility(View.GONE);
        installButton.setOnClickListener(v -> downloadUpdate());

        layout.addView(title);
        layout.addView(status);
        layout.addView(buttonContainer);
        layout.addView(installButton);

        setContentView(layout);

        // Consulta automática sem bloquear a abertura do jogo.
        checkForUpdates(false);
    }

    private boolean hasInternetConnection() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        if (manager == null) return false;

        Network network = manager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities =
                manager.getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                );
    }

    private void checkForUpdates(boolean manual) {

        if (!hasInternetConnection()) {
            if (manual) {
                status.setText(
                        "Sem internet. Você pode continuar jogando normalmente."
                );
            }
            return;
        }

        if (manual) {
            checkButton.setEnabled(false);
            status.setText("Consultando servidor...");
        }

        updateManager.check(result -> runOnUiThread(() -> {

            if (manual) {
                checkButton.setEnabled(true);
            }

            switch (result.getStatus()) {

                case UPDATE_AVAILABLE:
                    availableUpdate = result.getUpdateInfo();

                    updateIndicator.setVisibility(View.VISIBLE);
                    installButton.setVisibility(View.VISIBLE);

                    if (manual) {
                        status.setText(
                                "Atualização encontrada: "
                                + availableUpdate.getVersion()
                                + "\nTipo: " + availableUpdate.getType()
                                + "\n\n"
                                + availableUpdate.getChangelogText()
                        );
                    }
                    break;

                case UP_TO_DATE:
                    availableUpdate = null;

                    updateIndicator.setVisibility(View.GONE);
                    installButton.setVisibility(View.GONE);

                    if (manual) {
                        status.setText("Seu jogo está atualizado.");
                    }
                    break;

                case ERROR:
                    if (manual) {
                        status.setText(
                                "Falha ao verificar: " + result.getMessage()
                        );
                    }
                    break;
            }
        }));
    }

    private void downloadUpdate() {

        if (availableUpdate == null) return;

        installButton.setEnabled(false);
        checkButton.setEnabled(false);

        status.setText("Preparando download...");

        updateManager.downloadAndInstall(
                availableUpdate,
                new UpdateManager.DownloadCallback() {

                    @Override
                    public void onProgress(int progress) {
                        runOnUiThread(() ->
                                status.setText(
                                        "Baixando atualização: " + progress + "%"
                                )
                        );
                    }

                    @Override
                    public void onInstallStarted() {
                        runOnUiThread(() -> {
                            installButton.setEnabled(true);
                            checkButton.setEnabled(true);

                            status.setText(
                                    "APK validado. Aguardando confirmação do Android."
                            );
                        });
                    }

                    @Override
                    public void onPermissionRequired() {
                        runOnUiThread(() -> {
                            installButton.setEnabled(true);
                            checkButton.setEnabled(true);

                            status.setText(
                                    "Permita instalar aplicativos desta fonte nas configurações. "
                                    + "Depois toque novamente em Baixar e instalar."
                            );
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            installButton.setEnabled(true);
                            checkButton.setEnabled(true);

                            status.setText("Erro na atualização: " + message);
                        });
                    }
                }
        );
    }
}
