import jm.music.data.Phrase;
import jm.music.tools.NoteListException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Selection{
    MutationAndCrossover mutationAndCrossover = new MutationAndCrossover();

    /**
     * Method for generating a new population based on previous one.
     *
     * @param phrases   population to be based on
     * @param iteration number of iteration for mutation probability computing
     * @return new generation of rated phrases
     */
    ArrayList<PhraseWithRating> newGeneration(ArrayList<PhraseWithRating> phrases, int iteration) throws NoteListException{

        ArrayList<Phrase> parents = new ArrayList<>();
        ArrayList<Phrase> mutants = new ArrayList<>();
        double totalRating = 0;
        double curPos = 0;
        for (PhraseWithRating phrase : phrases) {
            totalRating += phrase.rating;
        }
        for (PhraseWithRating phrase : phrases) {
            double part = phrase.rating / totalRating;
            phrase.roulettePosition = curPos + part;
            curPos += part;
        }
        for (int q = 0; q < Variables.numOfParents; q++) {
            double pos = Math.random();
            for (PhraseWithRating phrase : phrases) {
                if (phrase.roulettePosition - pos > 0) {
                    parents.add(phrase.phrase);
                    break;
                }
            }
        }
        for (PhraseWithRating phrase : phrases) {
            mutants.add(phrase.phrase);
        }
        ArrayList<Phrase> newGen = new ArrayList<>();
        for (int q = 0; q < parents.size(); q++) {
            Random random = new Random();
            int pos = random.nextInt(parents.size());
            while (pos == q) {
                pos = random.nextInt(parents.size());
            }
            newGen.addAll(mutationAndCrossover.crossover(parents.get(q), parents.get(pos)));
        }
        for (Phrase phrase : mutants) {
            mutationAndCrossover.mutation(phrase, iteration);
            newGen.add(phrase);
        }
        ArrayList<PhraseWithRating> newGenRated = new ArrayList<>();
        for (Phrase phrase : newGen) {
            if (phrase.size() == 0)
                continue;
            newGenRated.add(new PhraseWithRating(phrase));
        }
        newGenRated.sort(Comparator.comparing(PhraseWithRating -> PhraseWithRating.rating));
        while (newGenRated.size() > Variables.population) {
            newGenRated.remove(0);
        }
        double lastMean = meanBestRating(phrases)[0];
        double newMean = meanBestRating(newGenRated)[0];
        if (lastMean > newMean)
            return phrases;
        return newGenRated;
    }

    /**
     * Method for computing mean and best rating of a population.
     *
     * @param phrases rated population
     * @return an array consisting of mean and best rating respectively
     */
    double[] meanBestRating(ArrayList<PhraseWithRating> phrases){
        double rating = 0.0;
        double bestRating = Double.MIN_VALUE;
        for (PhraseWithRating phraseWithRating : phrases) {
            rating += phraseWithRating.rating;
            if (phraseWithRating.rating > bestRating) {
                bestRating = phraseWithRating.rating;
            }
        }
        return new double[]{rating / phrases.size(), bestRating};
    }
}

class PhraseWithRating{
    Phrase phrase;
    double rating;

    double roulettePosition;

    PhraseWithRating(Phrase phrase) throws NoteListException{
        this.phrase = phrase;
        FitnessFunction function = new FitnessFunction();
        this.rating = function.fitnessFunction(phrase);
    }


}
