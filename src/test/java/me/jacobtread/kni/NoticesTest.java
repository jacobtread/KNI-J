package me.jacobtread.kni;

import me.jacobtread.kni.data.Notice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

public class NoticesTest {

    private static final String DOMAIN = "demo.school.kiwi";
    private static final String DATE = "01/01/2020";

    @Test
    void checkResponse() {
        KNI kni = new KNI(DOMAIN);
        try {
            Notices notices = kni.retrieve(DATE);
            if (notices.isSuccess()) {
                List<Notice> noticesList = notices.getNotices();
                Assertions.assertEquals(2, noticesList.size(), "Expected two notices");
                Notice firstNotice = noticesList.get(0);
                Assertions.assertEquals(Notice.Level.ALL, firstNotice.getLevel());
                Assertions.assertEquals("Test 1", firstNotice.getSubject());
                Assertions.assertEquals("", firstNotice.getBody());
                Assertions.assertEquals("SD", firstNotice.getTeacher());
                Notice secondNotice = noticesList.get(1);
                Assertions.assertEquals(Notice.Level.OTHER, secondNotice.getLevel());
                Assertions.assertEquals("Quote of the day", secondNotice.getSubject());
                Assertions.assertEquals("\"Success belongs to those who learn, grow and study\"", secondNotice.getBody());
                Assertions.assertEquals("SD", secondNotice.getTeacher());
            } else {
                System.err.println("Failed to complete test unable to make request: " + notices.getErrorMessage());
            }
        } catch (IOException | SAXException e) {
            System.err.println("Unable to complete test server/connection issue: ");
            e.printStackTrace();
        }
    }

}
