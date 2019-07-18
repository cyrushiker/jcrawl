package com.cyrus.jcrawl.demo;

import org.apache.log4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author cyrushiker
 * @since 2019/7/18 09:17
 */
public class JsoupCrawl {

    static Logger logger = Logger.getLogger(JsoupCrawl.class);

    static {
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");

        ConsoleAppender appender = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT);
        appender.setName("console");
        appender.setEncoding("UTF-8");

        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
    }


    public static void main(String[] args) {

        String url = "https://www.meizitu.com/a/list_1_1.html";
        List<String> pages = null;
        try {
            pages = MeizituCrawl.extract(url, "#maincontent .inWrap ul li .pic a", "href");
        } catch (IOException e) {
            // e.printStackTrace();
            logger.error(e.getMessage());
            System.exit(0);
        }
        logger.info(pages);

        List<String> girls = new ArrayList<>();

        for (String page : pages.subList(1, 4)) {
            List<String> images;
            try {
                images = MeizituCrawl.extract(page, ".postContent #picture p img", "src");
            } catch (IOException e) {
                // e.printStackTrace();
                logger.error(e.getMessage());
                continue;
            }
            girls.addAll(images);
            logger.info(images);
        }

        // scrapy girls images in concurrent
        ExecutorService exec = Executors.newFixedThreadPool(10);
        for (String image : girls) {
            exec.execute(new MeizituCrawl("/tmp/mzt2", image));
        }
        exec.shutdown();
    }
}
