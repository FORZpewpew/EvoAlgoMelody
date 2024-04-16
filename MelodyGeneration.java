import jm.music.data.*;
import jm.music.tools.NoteListException;
import jm.util.Write;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static jm.constants.Pitches.*;

public class MelodyGeneration{
    public static void main(String[] args) throws NoteListException, IOException{

        ArrayList<PhraseWithRating> population = initialisePopulation();
        population.sort(Comparator.comparing(phraseWithRating -> phraseWithRating.rating));
        Selection selection = new Selection();

        double[] xCoordinates = new double[Variables.iterations];
        double[] yMeanCoordinates = new double[Variables.iterations];
        double[] yBestCoordinates = new double[Variables.iterations];

        for (int q = 0; q < Variables.iterations; q++) {
            population = selection.newGeneration(population, q);
            double[] stats = selection.meanBestRating(population);
            yBestCoordinates[q] = stats[1];
            System.out.println("Generation №" + q + ". Mean rating: " + stats[0]);
            yMeanCoordinates[q] = stats[0];
            xCoordinates[q] = q;

            for (int w = 0; w < Variables.numOfSavedBest; w++) {
                writeEntry(population, w + 1, q);
            }
        }

        Plot plot = Plot.plot(null).
                // setting data
                        series(null, Plot.data().
                        xy(xCoordinates, yMeanCoordinates), null);

        plot.save("meanGraph", "png");
        Plot plot1 = Plot.plot(null).
                // setting data
                        series(null, Plot.data().
                        xy(xCoordinates, yBestCoordinates), null);

        plot1.save("bestGraph", "png");

    }

    /**
     * Method for storing an individual
     *
     * @param population        a population that individual is taken from
     * @param position          position of individual. Higher positions relate to lower fitness value.
     * @param numberOfIteration a number of iteration
     */
    private static void writeEntry(ArrayList<PhraseWithRating> population, int position, int numberOfIteration){
        Phrase phrase = population.get(population.size() - position).phrase;
        phrase.setInstrument(Variables.instrument);
        Score score = new Score(new Part(phrase));
        score.setTempo(Variables.tempo);
        String directoryPath = "output/" + numberOfIteration;

        // Create a File object representing the directory
        File directory = new File(directoryPath);

        // Create the directory using mkdir()
        if (!directory.mkdir()) {
            System.out.println("Failed to create directory.");
            System.exit(2);
        }
        Write.midi(score, "output/" + numberOfIteration + "/" + position + ".mid");
    }

    /**
     * Method for initialising population.
     *
     * @return a population of individual phrases.
     */
    static ArrayList<PhraseWithRating> initialisePopulation() throws NoteListException{
        ArrayList<PhraseWithRating> phrases = new ArrayList<>();
        for (int q = 0; q < Variables.population; q++) {
            Phrase phrase = getRandomPhrase();
            phrases.add(new PhraseWithRating(phrase));
        }
        return phrases;
    }

    /**
     * Method for generating a random phrase. Adjustable by Variables class.
     *
     * @return random phrase
     */
    static Phrase getRandomPhrase(){
        double[] durations = {0.5, 1, 1.5, 1.5};
        Phrase phrase = new Phrase();
        Random random = new Random();
        for (int q = 0; q < Variables.numOfBars; q++) {
            double curBarDuration = 0;
            while (curBarDuration != 4) {
                int pitch;
                if (Math.random() > 0.3)
                    pitch = Variables.key + Variables.scale[random.nextInt(Variables.scale.length)];
                else
                    pitch = REST;
                double duration = durations[random.nextInt(durations.length)];
                while (duration > 4 - curBarDuration)
                    duration = durations[random.nextInt(durations.length)];
                Note randomNote = new Note(pitch, duration);
                phrase.add(randomNote);
                curBarDuration += duration;
            }
        }
        phrase.setDynamic(50);
        return phrase;
    }
}