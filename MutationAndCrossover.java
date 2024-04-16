import jm.music.data.Note;
import jm.music.data.Phrase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static jm.constants.Pitches.*;
import static jm.constants.Scales.HARMONIC_MINOR_SCALE;
import static jm.constants.Scales.MAJOR_SCALE;

public class MutationAndCrossover{
    /**
     * Crossover method that generates specified amount of children by random gene selection,
     * one and two point crossover mutation.
     * Adjustable by Variables class.
     *
     * @param dad first parent
     * @param mom second parent
     * @return an array of children
     */
    ArrayList<Phrase> crossover(Phrase dad, Phrase mom){

        ArrayList<Phrase> children = new ArrayList<>();
        ArrayList<ArrayList<Note>> firstParent = new ArrayList<>();
        ArrayList<ArrayList<Note>> secondParent = new ArrayList<>();
        //dividing each parent into bars
        double curDuration = 0;

        ArrayList<Note> tempArray = new ArrayList<>();
        for (int w = 0; w < dad.getNoteArray().length; w++) {
            tempArray.add(dad.getNoteArray()[w]);
            curDuration += dad.getNoteArray()[w].getRhythmValue();
            if (curDuration == 4) {
                firstParent.add(tempArray);
                tempArray.clear();
                curDuration = 0;
            }
        }

        for (int w = 0; w < mom.getNoteArray().length; w++) {
            tempArray.add(mom.getNoteArray()[w]);
            curDuration += mom.getNoteArray()[w].getRhythmValue();
            if (curDuration == 4) {
                secondParent.add(tempArray);
                tempArray.clear();
                curDuration = 0;
            }
        }


        //random gene
        for (int q = 0; q < Variables.randomGeneChildren; q++) {
            Phrase phrase = new Phrase();
            for (int w = 0; w < Variables.numOfBars; w++) {
                if (Math.random() < 0.5) {
                    Note[] notes = new Note[firstParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = firstParent.get(w).get(e);
                    phrase.addNoteList(notes);
                } else {
                    Note[] notes = new Note[secondParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = secondParent.get(w).get(e);
                    phrase.addNoteList(notes);
                }
            }
            children.add(phrase);
        }


        //one point crossover
        Random random = new Random();
        for (int q = 0; q < Variables.onePointCrossoverChildren; q++) {
            Phrase phrase = new Phrase();
            int crossoverPoint = random.nextInt(Variables.numOfBars - 2) + 2;
            for (int w = 0; w < Variables.numOfBars; w++) {
                Note[] notes;
                if (w <= crossoverPoint) {
                    notes = new Note[firstParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = firstParent.get(w).get(e);
                } else {
                    notes = new Note[secondParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = secondParent.get(w).get(e);
                }
                phrase.addNoteList(notes);
            }
            children.add(phrase);
        }

        //two point crossover
        for (int q = 0; q < Variables.twoPointCrossoverChildren; q++) {
            Phrase phrase = new Phrase();
            int firstCrossoverPoint = random.nextInt(Variables.numOfBars / 2);
            int secondCrossoverPoint = random.nextInt(Variables.numOfBars / 2) + Variables.numOfBars / 2;
            for (int w = 0; w < Variables.numOfBars; w++) {
                Note[] notes;
                if (w <= firstCrossoverPoint) {
                    notes = new Note[firstParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = firstParent.get(w).get(e);
                } else if (w < secondCrossoverPoint) {
                    notes = new Note[secondParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = secondParent.get(w).get(e);
                } else {
                    notes = new Note[firstParent.get(w).size()];
                    for (int e = 0; e < notes.length; e++)
                        notes[e] = firstParent.get(w).get(e);
                }
                phrase.addNoteList(notes);
            }
            children.add(phrase);
        }

        return children;
    }

    /**
     * Method for phrase mutation, mutates pitch of notes and has a chance to duplicate random bar.
     *
     * @param phrase    phrase to be mutated
     * @param iteration number of iteration
     */
    void mutation(Phrase phrase, int iteration){
        pitchMutation(phrase, iteration);
        if (Math.random() > 1 - Variables.duplicationRate) duplicate(phrase);
    }

    /**
     * Method for pitchMutation.
     *
     * @param phrase    phrase to be mutated
     * @param iteration number of iteration
     */
    void pitchMutation(Phrase phrase, int iteration){
        double mutationProbability = Math.max(Variables.pitchMutationStartValue - Variables.pitchMutationDecay * iteration, Variables.pitchMutationEndValue);
        Random random = new Random();
        boolean[] contained = containedPitches(phrase.getNoteArray());
        for (int q = 0; q < phrase.getNoteArray().length; q++) {
            if (mutationProbability > Math.random()) {
                if (contained[q]) continue;
                int pitch = Variables.key + Variables.scale[random.nextInt(Variables.scale.length)];
                phrase.getNoteArray()[q].setPitch(pitch);
            }
        }
    }

    boolean[] containedPitches(Note[] notes){
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
        return contained;
    }

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
     * Method for random bar duplication.
     * Copies random bar in place of another bar.
     *
     * @param phrase phrase to be mutated
     */
    void duplicate(Phrase phrase){
        ArrayList<ArrayList<Note>> result = new ArrayList<>(Variables.numOfBars);
        double curDuration = 0;

        ArrayList<Note> tempArray = new ArrayList<>();
        for (int w = 0; w < phrase.getNoteArray().length; w++) {
            tempArray.add(phrase.getNoteArray()[w]);
            curDuration += phrase.getNoteArray()[w].getRhythmValue();
            if (curDuration >= 4) {
                ArrayList<Note> copy = new ArrayList<>();
                for (Note note : tempArray) {
                    copy.add(note);
                }
                result.add(copy);
                curDuration = 0;
                tempArray.clear();
            }
        }
        Random random = new Random();
        int randomDuplicateBar = random.nextInt(Variables.numOfBars);
        int destinationBar = random.nextInt(Variables.numOfBars);
        while (destinationBar == randomDuplicateBar) destinationBar = random.nextInt(Variables.numOfBars);
        result.set(destinationBar, result.get(randomDuplicateBar));
        int numberOfNotes = 0;
        for (ArrayList<Note> arrayList : result) {
            numberOfNotes += arrayList.size();
        }
        Note[] notes = new Note[numberOfNotes];
        int pos = 0;
        for (int q = 0; q < Variables.numOfBars; q++) {
            for (int w = 0; w < result.get(q).size(); w++) {
                notes[pos] = result.get(q).get(w);
                pos++;
            }
        }
        phrase = new Phrase(notes);
    }
}
