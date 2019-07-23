package com.cyrus.jcrawl.demo;

import com.cyrus.jcrawl.dbp.HikariCPDataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cyrus.jcrawl.demo.JsoupCrawl.logger;

/**
 * @author cyrushiker
 * @since 2019/7/18 09:29
 */
class MeizituCrawl extends Thread {

    static int count = 0;

    String savePath;

    String url;

    public MeizituCrawl(String savePath, String url) {
        this.savePath = savePath;
        this.url = url;
    }

    static List<String> extract(String url, String selector, String attrName) throws IOException {
        Document doc = Jsoup.connect(url).timeout(6000).get();
        Elements newsHeadlines = doc.select(selector);
        List<String> pages = new ArrayList<>();
        for (Element headline : newsHeadlines) {
            pages.add(headline.absUrl(attrName));
        }
        return pages;
    }

    synchronized private static void createpath(String path) throws IOException {
        Path savePathObj = Paths.get(path);
        // 第一次创建完成后，后续的进程将判断为已创建
        if (!Files.exists(savePathObj)) {
            Files.createDirectory(savePathObj);
        }
    }

    void downloadPic() {
        URL urlObj = null;
        ReadableByteChannel rbcObj = null;
        FileOutputStream fos = null;

        String[] ps = this.url.split("/");
        String fileName = String.join("_", Arrays.copyOfRange(ps, 3, ps.length));

        synchronized (MeizituCrawl.class) {
            try {
                Path savePathObj = Paths.get(this.savePath);
                // 第一次创建完成后，后续的进程将判断为已创建
                if (!Files.exists(savePathObj)) {
                    Files.createDirectory(savePathObj);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try {
            urlObj = new URL(this.url);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
            rbcObj = Channels.newChannel(urlObj.openConnection(proxy).getInputStream());
            fos = new FileOutputStream(Paths.get(this.savePath, fileName).toString());

            fos.getChannel().transferFrom(rbcObj, 0, Long.MAX_VALUE);
            logger.info("File Successfully Downloaded From The Url: " + this.url);
            synchronized (this) {
                MeizituCrawl.count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (rbcObj != null) {
                    rbcObj.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        synchronized (this) {
            logger.info("total pics download is " + MeizituCrawl.count);
        }
    }


    @Override
    public void run() {
        this.downloadPic();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(HikariCPDataSource.getDatasource());
        logger.info(jdbcTemplate);
        // Connection conn = HikariCPDataSource.getConnection();
        List rest = jdbcTemplate.queryForList("select * from pg_atoms");
        logger.info(rest.size());

    }

    public static void main(String[] args) {
        new MeizituCrawl("/tmp/mzt", "http://pic.topmeizi.com/wp-content/uploads/2017a/01/08/09.jpg").downloadPic();
    }
}
