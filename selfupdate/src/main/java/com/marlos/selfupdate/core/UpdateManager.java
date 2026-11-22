
package com.marlos.selfupdate.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.marlos.selfupdate.install.ApkUpdateInstaller;
import com.marlos.selfupdate.model.UpdateInfo;
import com.marlos.selfupdate.model.UpdateResult;
import com.marlos.selfupdate.network.UpdateChecker;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {

    public interface CheckCallback {
        void onResult(UpdateResult result);
    }

    public interface DownloadCallback {
        void onProgress(int progress);
        void onInstallStarted();
        void onPermissionRequired();
        void onError(String message);
    }

    private final Context context;
    private final UpdateConfig config;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public UpdateManager(Context context, UpdateConfig config) {
        this.context = context.getApplicationContext();
        this.config = config;
    }

    public void check(CheckCallback callback) {
        executor.execute(() -> {
            String installedVersion;

            try {
                PackageInfo info = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0);

                installedVersion = info.versionName;

            } catch (Exception e) {
                callback.onResult(UpdateResult.error(
                        "Não foi possível ler a versão instalada."
                ));
                return;
            }

            UpdateResult result =
                    new UpdateChecker().check(config, installedVersion);

            callback.onResult(result);
        });
    }

    public void downloadAndInstall(
            UpdateInfo info,
            DownloadCallback callback
    ) {
        executor.execute(() -> {
            try {
                ApkUpdateInstaller installer =
                        new ApkUpdateInstaller(context, config);

                File apk = installer.downloadAndValidate(
                        info,
                        callback::onProgress
                );

                boolean opened = installer.openInstaller(apk);

                if (opened) {
                    callback.onInstallStarted();
                } else {
                    callback.onPermissionRequired();
                }

            } catch (Exception e) {
                callback.onError(
                        e.getMessage() == null
                                ? "Falha ao atualizar."
                                : e.getMessage()
                );
            }
        });
    }
}
