
package com.marlos.selfupdate.install;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.marlos.selfupdate.core.UpdateConfig;
import com.marlos.selfupdate.model.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class ApkUpdateInstaller {

    public interface ProgressListener {
        void onProgress(int progress);
    }

    private final Context context;
    private final UpdateConfig config;

    public ApkUpdateInstaller(Context context, UpdateConfig config) {
        this.context = context.getApplicationContext();
        this.config = config;
    }

    public File downloadAndValidate(
            UpdateInfo info,
            ProgressListener listener
    ) throws Exception {

        String downloadUrl = info.getDownloadUrl();
        String expectedHash = info.getSha256();

        if (downloadUrl == null || !downloadUrl.startsWith("https://")) {
            throw new Exception("URL de download inválida.");
        }

        if (expectedHash == null || !expectedHash.matches("(?i)[a-f0-9]{64}")) {
            throw new Exception("SHA-256 inválido no manifesto.");
        }

        File directory = new File(context.getCacheDir(), "updates");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new Exception("Não foi possível criar a pasta de atualização.");
        }

        File temporaryFile = new File(directory, "update.apk.part");
        File apkFile = new File(directory, "update.apk");

        if (temporaryFile.exists()) temporaryFile.delete();
        if (apkFile.exists()) apkFile.delete();

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(downloadUrl).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();

            if (!"https".equalsIgnoreCase(
                    connection.getURL().getProtocol())) {
                throw new Exception("O download foi redirecionado para uma conexão insegura.");
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Falha no download. HTTP " + responseCode);
            }

            long totalSize = connection.getContentLength();
            long downloaded = 0;
            int lastProgress = -1;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(temporaryFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    digest.update(buffer, 0, bytesRead);

                    downloaded += bytesRead;

                    if (totalSize > 0 && listener != null) {
                        int progress = (int) (downloaded * 100 / totalSize);

                        if (progress != lastProgress) {
                            lastProgress = progress;
                            listener.onProgress(progress);
                        }
                    }
                }

                output.flush();
            }

            String actualHash = toHex(digest.digest());

            if (!actualHash.equalsIgnoreCase(expectedHash)) {
                throw new Exception("SHA-256 não confere. APK descartado.");
            }

            PackageInfo archive = context.getPackageManager()
                    .getPackageArchiveInfo(temporaryFile.getAbsolutePath(), 0);

            if (archive == null) {
                throw new Exception("O arquivo baixado não é um APK válido.");
            }

            if (!config.getApplicationId().equals(archive.packageName)) {
                throw new Exception("O APK pertence a outro aplicativo.");
            }

            long apkVersionCode = Build.VERSION.SDK_INT >= 28
                    ? archive.getLongVersionCode()
                    : archive.versionCode;

            if (apkVersionCode != info.getVersionCode()) {
                throw new Exception("O versionCode do APK não corresponde ao manifesto.");
            }

            if (!info.getVersion().equals(archive.versionName)) {
                throw new Exception("A versão do APK não corresponde ao manifesto.");
            }

            PackageInfo installed = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            long installedVersionCode = Build.VERSION.SDK_INT >= 28
                    ? installed.getLongVersionCode()
                    : installed.versionCode;

            if (apkVersionCode <= installedVersionCode) {
                throw new Exception("O APK não é uma versão superior à instalada.");
            }

            if (!temporaryFile.renameTo(apkFile)) {
                throw new Exception("Não foi possível finalizar o arquivo APK.");
            }

            return apkFile;

        } catch (Exception e) {
            temporaryFile.delete();
            apkFile.delete();
            throw e;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean openInstaller(File apkFile) throws Exception {

        PackageManager packageManager = context.getPackageManager();

        if (Build.VERSION.SDK_INT >= 26
                && !packageManager.canRequestPackageInstalls()) {

            Intent settingsIntent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.getPackageName())
            );

            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);

            return false;
        }

        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".selfupdate.fileprovider",
                apkFile
        );

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(
                apkUri,
                "application/vnd.android.package-archive"
        );

        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(installIntent);

        return true;
    }

    private String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte value : bytes) {
            result.append(String.format("%02x", value & 0xff));
        }

        return result.toString();
    }
}
