import jm.music.data.Note;
import jm.music.data.Phrase;
import jm.music.tools.NoteListException;
import java.util.ArrayList;


public class FitnessFunction{

    /**
     * Fitness Function
     *
     * @param phrase phrase to be rated
     * @return rating of phrase
     */
    double fitnessFunction(Phrase phrase){
        return  jumps(phrase) +
                repetitionDensity(phrase) +
                pitchRepetitionRate(phrase.getNoteArray()) * 2  +
                rhythmRepetitionRate(phrase.getNoteArray()) * 2 +
                restDensity(phrase) +
                barRest(phrase) +
                pitchVariety(phrase) * 2;

    }


    /**
     * Sub rater that checks if all notes of a scale are used.
     * @param phrase
     * @return
     */
    double pitchVariety(Phrase phrase) {
        double differentNotes = Variables.scale.length;
        ArrayList<Integer> pitches = new ArrayList<>();
        for (Note note: phrase.getNoteArray()) {
            if (note.isRest())
                continue;
            if (!pitches.contains(note.getPitch()))
                pitches.add(note.getPitch());
        }
        return pitches.size() / differentNotes;
    }

    /**
     * Sub rater that checks that there are notes at the start of a bar. If there is a note missing a penalty of 1 is added
     *
     * @param phrase phrase to be rated
     * @return Integer value of 1 - penalty
     */
    double barRest(Phrase phrase){
        Note[] notes = phrase.getNoteArray();
        double curDuration = 0;
        int penalty = 0;
        for (int q = 0; q < notes.length; q++) {
            if (curDuration == 0) if (notes[q].isRest()) penalty++;
            curDuration += notes[q].getRhythmValue();
            curDuration %= 4;
        }
        return 1 - penalty;
    }

    /**
     * Sub rater that computes ratio of silence to total duration of melody.
     *
     * @param phrase phrase to be rated
     * @return 1 - distance to target rating, which is specified in Variables class
     */
    double restDensity(Phrase phrase){
        double restRhytmCount = 0;
        double totalTime = 0;
        for (Note note : phrase.getNoteArray()) {
            if (note.isRest()) restRhytmCount += note.getRhythmValue();
            totalTime += note.getRhythmValue();
        }
        return 1 - Math.abs(Variables.restRatio - restRhytmCount / totalTime);
    }

    /**
     * Sub rater for pitch repetition
     *
     * @param notes phrase to be rated
     * @return ratio of notes that are contained within repetition to total number of notes
     */
    double pitchRepetitionRate(Note[] notes){
        int[] repetitionIndexes = repeatedPitches(notes);
        ArrayList<ArrayList<Integer>> repeatedPitches = new ArrayList<>();
        for (int q = 0; q < repetitionIndexes.length; q++) {
            if (repetitionIndexes[q] > 3) {
                ArrayList<Integer> pitches = new ArrayList<>();
                for (int w = 0; w < repetitionIndexes[q]; w++) {
                    pitches.add(notes[q - repetitionIndexes[q] + w + 1].getPitch());
                }
                if (!repeatedPitches.contains(pitches)) repeatedPitches.add(pitches);
            }
        }
        boolean[] contained = new boolean[notes.length];
        for (ArrayList<Integer> pitches : repeatedPitches) {
            int len = pitches.size();
            for (int q = 0; q < notes.length - len; q++) {
                if (notes[q].getPitch() == pitches.get(0)) {
                    boolean flag = true;
                    for (int w = 1; w < len; w++) {
                        if (notes[q + w].getPitch() != pitches.get(w)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        for (int w = 0; w < len; w++) {
                            contained[q + w] = true;
                        }
                    }
                }
            }
        }
        double containedNotes = 0.0;
        for (boolean b : contained) {
            if (b) containedNotes++;
        }
        return containedNotes / notes.length;
    }

    /**
     * Method for getting repeating pitches patterns. Partially uses The Longest Common String Algorithm
     *
     * @param notes phrase to be rated, represented as an array of notes
     * @return array with values corresponding to repeated pattern end.
     */
    int[] repeatedPitches(Note[] notes){
        int n = notes.length;
        int[][] LCSRe = new int[n + 1][n + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (notes[i - 1].getPitch() == notes[j - 1].getPitch() && !notes[i - 1].isRest() && !notes[j - 1].isRest() && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = LCSRe[i - 1][j - 1] + 1;
                } else {
                    LCSRe[i][j] = 0;
                }
            }
        }
        int[] arr = new int[LCSRe.length - 1];
        for (int q = 1; q < LCSRe.length; q++) {
            int best = 0;
            for (int w = 1; w < LCSRe.length; w++) {
                if (LCSRe[w][q] > best) best = LCSRe[w][q];
            }
            arr[q - 1] = best;
        }
        int[] copy = new int[arr.length];
        int q = 1;
        while (q < arr.length) {
            if (arr[q] < arr[q - 1]) {
                copy[q - 1] = arr[q - 1];
                for (int w = 1; w <= arr[q]; w++) {
                    copy[q - w - 2] = 0;
                }
            }
            q++;
        }
        copy[q - 1] = arr[q - 1];
        return copy;
    }


    /**
     * Sub rater for rhythm repetition
     *
     * @param notes phrase to be rated
     * @return ratio of notes that are contained within repetition to total number of notes
     */
    double rhythmRepetitionRate(Note[] notes){
        int[] repetitionIndexes = repeatedRhythms(notes);
        ArrayList<ArrayList<Double>> repeatedRhythms = new ArrayList<>();
        for (int q = 0; q < repetitionIndexes.length; q++) {
            if (repetitionIndexes[q] > 3) {
                ArrayList<Double> rhythms = new ArrayList<>();
                for (int w = 0; w < repetitionIndexes[q]; w++) {
                    rhythms.add(notes[q - repetitionIndexes[q] + w + 1].getRhythmValue());
                }
                if (!repeatedRhythms.contains(rhythms)) repeatedRhythms.add(rhythms);
            }
        }
        boolean[] contained = new boolean[notes.length];
        for (ArrayList<Double> rhythms : repeatedRhythms) {
            int len = rhythms.size();
            for (int q = 0; q < notes.length - len; q++) {
                if (notes[q].getRhythmValue() == rhythms.get(0)) {
                    boolean flag = true;
                    for (int w = 1; w < len; w++) {
                        if (notes[q + w].getPitch() != rhythms.get(w)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        for (int w = 0; w < len; w++) {
                            contained[q + w] = true;
                        }
                    }
                }
            }
        }
        double containedNotes = 0.0;
        for (boolean b : contained) {
            if (b) containedNotes++;
        }
        return containedNotes / notes.length;
    }

    /**
     * Method for getting repeating rhythm patterns. Partially uses The Longest Common String Algorithm
     *
     * @param notes phrase to be rated, represented as an array of notes
     * @return array with values corresponding to repeated pattern end.
     */
    int[] repeatedRhythms(Note[] notes){
        int n = notes.length;
        int[][] LCSRe = new int[n + 1][n + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (notes[i - 1].getRhythmValue() == notes[j - 1].getRhythmValue() && !notes[i - 1].isRest() && !notes[j - 1].isRest() && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = LCSRe[i - 1][j - 1] + 1;
                } else {
                    LCSRe[i][j] = 0;
                }
            }
        }
        int[] arr = new int[LCSRe.length - 1];
        for (int q = 1; q < LCSRe.length; q++) {
            int best = 0;
            for (int w = 1; w < LCSRe.length; w++) {
                if (LCSRe[w][q] > best) best = LCSRe[w][q];
            }
            arr[q - 1] = best;
        }
        int[] copy = new int[arr.length];
        int q = 1;
        while (q < arr.length) {
            if (arr[q] < arr[q - 1]) {
                copy[q - 1] = arr[q - 1];
                for (int w = 1; w <= arr[q]; w++) {
                    copy[q - w - 2] = 0;
                }
            }
            q++;
        }
        copy[q - 1] = arr[q - 1];
        return copy;
    }

    /**
     * Sub rater that gives penalty for consecutive pitches repetition.
     *
     * @param phrase phrase to be rated
     * @return 1 - ratio of repeated notes to total notes
     */
    double repetitionDensity(Phrase phrase){
        Note[] notes = phrase.getNoteArray();
        double count = 0;
        for (int q = 1; q < notes.length; q++) {
            if (notes[q].getPitch() == notes[q - 1].getPitch()) count++;
        }
        return 1 - count / notes.length;
    }


    /**
     * Sub rater that eliminates jumps in melody that are more than 6 semitones apart
     *
     * @param phrase phrase to be rated
     * @return 1 - ratio of jumping notes to total notes
     */
    double jumps(Phrase phrase){
        Note[] notes = phrase.getNoteArray();
        double jumpsCounter = 0.0;
        for (int q = 0; q < notes.length - 1; q++) {
            if (Math.abs(notes[q].getPitch() - notes[q + 1].getPitch()) > 6) {
                if (notes[q].isRest() || notes[q + 1].isRest()) {
                    continue;
                }
                jumpsCounter += 1;
            }
        }

        double ratio = jumpsCounter / notes.length;

        return 1 - ratio;
    }

}
