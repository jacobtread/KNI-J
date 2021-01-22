package me.jacobtread.kni.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MeetingNotice extends Notice {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EE F MMM"); // The format used by the dates

    private final String place;
    private final String date;
    private final String time;

    /**
     * @param index The order index in which KAMAR has store this notice
     * @param level The level of user this notice is targeted to
     * @param subject The subject/title content of the notice
     * @param body The body/content of the notice
     * @param teacher The teacher that posted the notice
     * @param place The place where this notice will occur
     * @param date The date this notice is for
     * @param time The time this notice is for (can be blank)
     */
    public MeetingNotice(int index, Level level, String subject, String body, String teacher, String place, String date, String time) {
        super(index, level, subject, body, teacher);
        this.place = place;
        this.date = date;
        this.time = time;
    }

    /**
     * Attempts to parse the date with the expected format
     *
     * @return The Java date object
     */
    public Date getDateParsed() {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getPlace() {
        return place;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean isMeeting() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MeetingNotice that = (MeetingNotice) o;
        return Objects.equals(place, that.place) && Objects.equals(date, that.date) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), place, date, time);
    }

    @Override
    public String toString() {
        return "MeetingNotice{" +
                "place='" + place + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
