package nlp.wsd.co;

/**
 * Created by Saeed on 12/13/2016.
 */
public class DebugItem {

    String ambiguousId;
    String explain;
    String selectedSense;
    int senseCount;
    double maxScore;
    double beforeMaxScore;
    boolean isContain;

    public DebugItem(String ambiguousId,
                     String selectedSense,
                     double maxScore,
                     double beforeMaxScore,
                     String explain,
                     int senseCount,
                     boolean isContain) {
        this.ambiguousId = ambiguousId;
        this.explain = explain;
        this.maxScore = maxScore;
        this.beforeMaxScore = beforeMaxScore;
        this.selectedSense = selectedSense;
        this.senseCount = senseCount;
        this.isContain = isContain;
    }

    public String getAmbiguousId() {
        return ambiguousId;
    }

    public String getExplain() {
        return explain;
    }

    public String getSelectedSense() {
        return selectedSense;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public double getBeforeMaxScore() {
        return beforeMaxScore;
    }

    public boolean isContain() {
        return isContain;
    }
}
