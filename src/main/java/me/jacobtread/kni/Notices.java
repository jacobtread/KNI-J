package me.jacobtread.kni;

import me.jacobtread.kni.data.MeetingNotice;
import me.jacobtread.kni.data.Notice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Notices {

    private final String date;
    private List<Notice> notices;
    private String errorMessage;

    /**
     * @param date The date this notices object is for
     */
    Notices(String date) {
        this.date = date;
    }

    /**
     * Get all the notices
     *
     * @return A list of all the notices
     */
    public List<Notice> getNotices() {
        return notices;
    }

    void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    /**
     * Get all notices that match the filter
     *
     * @param filter A filter for the notices to get
     * @return A list of notices filtered based on the filter
     */
    public List<Notice> getNotices(Predicate<Notice> filter) {
        List<Notice> filtered = new ArrayList<>(notices);
        filtered.removeIf(Predicate.not(filter));
        return filtered;
    }

    /**
     * Gets all the meeting notices
     *
     * @return A list containing all the meeting notices
     */
    public List<MeetingNotice> getMeetings() {
        List<MeetingNotice> meetingNotices = new ArrayList<>();
        for (Notice notice : getNotices(Notice::isMeeting)) {
            meetingNotices.add((MeetingNotice) notice);
        }
        return meetingNotices;
    }

    /**
     * @return The raw date string of what date this request is for
     */
    public String getDate() {
        return date;
    }

    void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return The error message provided by KAMAR
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return Whether or not the request was a success
     */
    public boolean isSuccess() {
        return errorMessage == null || notices == null;
    }

    @Override
    public String toString() {
        return "Notices{" +
                "date='" + date + '\'' +
                ", notices=" + notices +
                '}';
    }
}
