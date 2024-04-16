import jm.constants.ProgramChanges;

import static jm.constants.Pitches.*;
import static jm.constants.ProgramChanges.STEEL_GUITAR;
import static jm.constants.Scales.*;

public class Variables{
    protected static final int numOfBars = 12;
    protected static final int iterations = 1001;
    protected static final int population = 300;
    protected static final int tempo = 180;
    protected static final int numOfSavedBest = 3;

    static int[] scale = HARMONIC_MINOR_SCALE;
    static int key = C4;
    static int instrument = ProgramChanges.BANJO;

    //crossover
    protected static final int numOfParents = 10;
    protected static final int randomGeneChildren = 1;
    protected static final int onePointCrossoverChildren = 1;
    protected static final int twoPointCrossoverChildren = 1;

    //mutation
    protected static final double pitchMutationStartValue = 0.4;
    protected static final double pitchMutationEndValue = 0.2;
    protected static final double pitchMutationDecay = 0.03;
    protected static final double duplicationRate = 0.3;

    //fitness
    protected static final double restRatio = 0.2;

}
