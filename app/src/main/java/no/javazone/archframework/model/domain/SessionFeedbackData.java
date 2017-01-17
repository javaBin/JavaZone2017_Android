package no.javazone.archframework.model.domain;

public class SessionFeedbackData {

    public String sessionId;

    public int sessionRating;

    public int sessionRelevantAnswer;

    public int contentAnswer;

    public int speakerAnswer;

    public String comments;

    public SessionFeedbackData(String sessionId, int sessionRating, int sessionRelevantAnswer,
                               int contentAnswer, int speakerAnswer, String comments) {
        this.sessionId = sessionId;
        this.sessionRating = sessionRating;
        this.sessionRelevantAnswer = sessionRelevantAnswer;
        this.contentAnswer = contentAnswer;
        this.speakerAnswer = speakerAnswer;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "SessionId: " + sessionId +
                " SessionRating: " + sessionRating +
                " SessionRelevantAnswer: " + sessionRelevantAnswer +
                " ContentAnswer: " + contentAnswer +
                " SpeakerAnswer: " + speakerAnswer +
                " Comments: " + comments;
    }
}