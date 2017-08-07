package org.longinus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Silva on 19-03-2016.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            help();
            System.exit(1);
        }

        int downloadWaitTime = 1000;

        for (int i = 0; i < args.length; i++) {
            if (args[i].contentEquals("-d"))
                downloadWaitTime = Integer.parseInt(args[i + 1]);
            if (args[i].contentEquals("-h")) {
                help();
                System.exit(1);
            }
        }

        Pattern pattern = Pattern.compile("(http|https):\\/\\/nhentai.net\\/g\\/[0-9]*\\/");
        Matcher matcher = pattern.matcher(args[args.length - 1]);

        String mainUrl = null;
        if (matcher.find()) {
            mainUrl = matcher.group(0);
        }

        if (mainUrl == null) {
            help();
            System.exit(1);
        }

        GetNHentai getNHentai = new GetNHentai(mainUrl);
        getNHentai.setDownloadWaitTimeMs(downloadWaitTime);
        getNHentai.download();
    }

    private static void help() {
        System.out.println("Get-nHentai release 20170411");
        System.out.println("Copyright (C) 2016-2017 David Silva");
        System.out.println();
        System.out.println("Usage: java -jar getnhentai.jar [-h] [-d waitdownloadtime] URL");
        System.out.println("-h show this message");
        System.out.println("-d time to wait before each downloaded file in miliseconds");
        System.out.println("URL must be something like");
        System.out.println("   http://nhentai.net/g/XXXXXXXX/");
    }
}
