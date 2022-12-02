import org.jsoup.Jsoup;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class BookmarkExporter {
    /**
     * Single clipboard instance should be used throughout. Everytime getSystemClipboard is called we
     * effectively reset the data in our application's clipboard by overwriting it with system clipboard's
     * data. Writing data to AWT clipboard doesn't change the state of system clipboard.
     */
    
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private void saveLinks(String path) throws IOException, UnsupportedFlavorException, InterruptedException {
        String title = null;
        int extensionStartIndex = path.lastIndexOf(".");
        if(extensionStartIndex != -1) {
            title = path.substring(path.lastIndexOf("/")+1, extensionStartIndex);
        } else {
            title = path.substring(path.lastIndexOf("/") + 1);
            path += ".html";
        }
        Path savePath = Paths.get(path);
        Files.writeString(savePath, getBookmarkFile(title), StandardOpenOption.CREATE);
    }

    private StringBuilder getBookmarkFile(String Title) throws IOException, UnsupportedFlavorException, InterruptedException {
        String data = fetchClipboardData();
        String[] links = data.split("\n");

        StringBuilder bookmarkText = new StringBuilder("<TITLE>" + Title + "</TITLE>\n" + "<DT><H3>" + Title + "</H3>\n<DL><p>\n");
        for(String link: links) {
            bookmarkText.append("\n").append(prepareLink(link));
        }
        bookmarkText.append("</DL><p>");
        return bookmarkText;
    }

    private String prepareLink(String link) throws IOException {
        //Generate Url-Title entry for bookmark file
        String pageTitle = "";
        try {
            pageTitle = Jsoup.connect(link).userAgent("Mozilla").get().title();
        } catch (Exception e) {
            pageTitle = link;
        }
        System.out.println(pageTitle);
        return "<DT><A HREF=\"" + link + "\">" + pageTitle + "</A>";
    }

    private String fetchClipboardData() throws IOException, UnsupportedFlavorException, InterruptedException {
        //Flush the clipboard buffer before starting
        clipboard.setContents(new StringSelection(""), null);
        System.out.println("Copy links you want to export!");

        String data = "";
        while(data.equals("")) {
            Thread.sleep(1000);
            data = (String) clipboard.getData(DataFlavor.stringFlavor);
        }
        return data;
    }

    public static void main(String[] args) throws IOException, UnsupportedFlavorException, InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter File Path: ");
        String path = input.nextLine();

        BookmarkExporter linkExporter = new BookmarkExporter();
        linkExporter.saveLinks(path);
    }
}
