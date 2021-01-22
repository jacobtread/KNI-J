# KAMAR Notices Interface 
Java Edition

KNI (KAMAR Notices Interface) is a project designed to bring a way of accessing notices from
the [KAMAR](https://kamar.nz) portal software. KNIs goal is to produce usable libraries in as many
languages as possible


### Retrieving Notices
```java
// KNI Instance
KNI kni = new KNI("portal.your.school.nz");
try {
    // Retrieve the notices
    Notices notices = kni.retrieve();
    if (notices.isSuccess()) {
        List<Notice> noticeList = notices.getNotices();
        // TODO: Deal with the notices
    } else {
        // TODO: Failed to make the request with KAMAR (Rare but could happen)
        System.err.println("KAMAR sent an error response: " + notices.getErrorMessage());
    }
} catch (IOException e) {
// TODO: Unable to make connection / reading error
} catch (SAXException e) {
// TODO: Unable to parse server response
}
```
Handling meeting notices
```java
if(notice.isMeeting()) {
    MeetingNotice meetingNotice = (MeetingNotice) notice;
    // TODO: Your meeting notice code
} else {
    // TODO: Your general notice code
}
```
Getting only meetings
```java
List<MeetingNotice> noticeList = notices.getMeetings();
```
Filtering notices
```java
List<Notice> noticeList = notices.getNotices(notice -> 
            notice.getLevel().equals(Notice.Level.ALL));
```

By Jacobtread