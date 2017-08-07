package org.longinus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.net.URL;

/**
 * Created by David Silva on 19-03-2016.
 */
public class GetNHentai {
    private String mainUrl;
    private int downloadWaitTimeMs;

    final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";

    public GetNHentai(String mainUrl) {
        super();
        this.mainUrl = mainUrl;
        downloadWaitTimeMs = 1000;
    }

    public void download() {
        if (mainUrl == null)
            throw new RuntimeException("URL not defined");

        try {
            URL url = new URL(mainUrl);

            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().header("User-Agent", USER_AGENT).url(url).build();

            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                System.out.println("Server response: " + response.code());
                System.out.println("Unable to continue");
                return;
            }
            String mainContent = response.body().string();


            String title = null;
            int nPages = -1;
            String galleryCode = null;
            String expectedExt = "jpg";
            String lines[] = mainContent.split("\\r?\\n");
            for (String line : lines) {

                if (line.contains("<title>")) {
                    title = line.substring(line.indexOf('>') + 1, Math.min(line.indexOf('&') - 1, line.indexOf(" &raquo;")))
                            .trim().replaceAll("[:\\\"\\\\\\?\\|\\*]", "_").replace(".", "");
                }

                if (line.contains("<meta itemprop=\"image\"") && line.contains("cover.")) {
                    String[] parts = line.split("/");
                    galleryCode = parts[4];

                    if (line.contains("png")) {
                        expectedExt = "png";
                    }
                }

                if (line.contains(" pages</div>")) {
                    nPages = Integer.parseInt(line.substring(line.indexOf('>') + 1, line.indexOf(" pages")).trim());
                }
            }

            if (nPages == -1 || title == null || galleryCode == null)
                throw new RuntimeException("Unable to detect number of pages / title / gallery code");

            File targetFolder = new File(title.replace("\\", "_").replace("/", "_"));
            if (targetFolder.exists()) {
                System.out.println("Folder " + title + " already exists.");
                System.exit(2);
            }

            targetFolder.mkdir();

            try {
                Thread.sleep(downloadWaitTimeMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int nDone = 0;
            String imgBaseUrl = "http://i.nhentai.net/galleries/" + galleryCode + "/";
            for (int i = 1; i <= nPages; i++) {
                URL fileUrl = new URL(imgBaseUrl + i + "." + expectedExt);
                File downloadedFile = new File(targetFolder, String.format("%03d", i) + "." + expectedExt);
                System.out.print(fileUrl);
                try {
                    request = new Request.Builder().header("User-Agent", USER_AGENT)
                            .header("Referer", "http://nhentai.net/g/" + galleryCode + "/" + i + "/").url(fileUrl).build();
                    response = client.newCall(request).execute();
                    if (response.code() == 404) {
                        System.out.println("...404");
                        String alternativeExt = (expectedExt.equals("png")) ? "jpg" : "png";
                        fileUrl = new URL(imgBaseUrl + i + "." + alternativeExt);
                        downloadedFile = new File(targetFolder, String.format("%03d", i) + "." + alternativeExt);
                        System.out.print(fileUrl);
                        request = new Request.Builder().header("User-Agent", USER_AGENT)
                                .header("Referer", "http://nhentai.net/g/" + galleryCode + "/" + i + "/").url(fileUrl).build();
                        response = client.newCall(request).execute();
                    }
                    if (response.code() != 200) {
                        System.out.println("...error code: " + response.code());
                        continue;
                    }
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                    System.out.println("...done");
                    nDone++;
                } catch (Exception e) {
                    System.out.println("...error:");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(downloadWaitTimeMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Downloaded [" + nDone + "/" + nPages + "]");

        } catch (Exception allDownloadExceptions) {
            allDownloadExceptions.printStackTrace();
        }
    }

    public void setDownloadWaitTimeMs(int downloadWaitTimeMs) {
        this.downloadWaitTimeMs = (downloadWaitTimeMs > 250) ? downloadWaitTimeMs : 250;
    }
}
