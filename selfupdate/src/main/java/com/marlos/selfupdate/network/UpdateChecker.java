package com.marlos.selfupdate.network;

import com.marlos.selfupdate.core.UpdateConfig;
import com.marlos.selfupdate.model.*;
import com.marlos.selfupdate.version.*;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateChecker {
    public UpdateResult check(UpdateConfig config, String installedVersion) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(config.getManifestUrl()).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);
            connection.setRequestProperty("Cache-Control", "no-cache");
            int code = connection.getResponseCode();
            if (code != 200) return UpdateResult.error("Servidor respondeu HTTP " + code);

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }

            JSONObject json = new JSONObject(body.toString());
            String remoteVersionText = json.getString("version");
            int remoteCode = json.getInt("versionCode");
            String downloadUrl = json.optString("downloadUrl", "");
            String sha256 = json.optString("sha256", "");
            JSONArray changes = json.optJSONArray("changelog");
            List<String> changelog = new ArrayList<>();
            if (changes != null) for (int i = 0; i < changes.length(); i++) changelog.add(changes.getString(i));

            Version installed = Version.parse(installedVersion);
            Version remote = Version.parse(remoteVersionText);
            if (remote.compareTo(installed) <= 0) return UpdateResult.upToDate();

            UpdateType type = UpdateClassifier.classify(installed, remote);
            UpdateInfo info = new UpdateInfo(remoteVersionText, remoteCode, downloadUrl, sha256, changelog, type);
            return UpdateResult.available(info);
        } catch (Exception e) {
            return UpdateResult.error(e.getMessage() == null ? "Erro desconhecido" : e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
