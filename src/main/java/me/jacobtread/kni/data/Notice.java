package me.jacobtread.kni.data;

import java.util.Locale;
import java.util.Objects;

public class Notice {

    private final int index;
    private final Level level;
    private final String subject;
    private final String body;
    private final String teacher;

    /**
     * @param index The order index in which KAMAR has store this notice
     * @param level The level of user this notice is targeted to
     * @param subject The subject/title content of the notice
     * @param body The body/content of the notice
     * @param teacher The teacher that posted the notice
     */
    public Notice(int index, Level level, String subject, String body, String teacher) {
        this.index = index;
        this.level = level;
        this.subject = subject;
        this.body = body;
        this.teacher = teacher;
    }

    public int getIndex() {
        return index;
    }

    public Level getLevel() {
        return level;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getTeacher() {
        return teacher;
    }

    public boolean isMeeting() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notice notice = (Notice) o;
        return index == notice.index && level == notice.level
                && Objects.equals(subject, notice.subject)
                && Objects.equals(body, notice.body)
                && Objects.equals(teacher, notice.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, level, subject, body, teacher);
    }

    @Override
    public String toString() {
        return "Notice{" +
                "index=" + index +
                ", level=" + level +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", teacher='" + teacher + '\'' +
                '}';
    }

    public enum Level {
        ALL,
        JUNIORS,
        SENIORS,
        OTHER;

        public static Level fromString(String text) {
            for (Level value : values()) {
                if (text.toUpperCase(Locale.ROOT).equals(value.name())) {
                    return value;
                }
            }
            return OTHER;
        }

    }

}
