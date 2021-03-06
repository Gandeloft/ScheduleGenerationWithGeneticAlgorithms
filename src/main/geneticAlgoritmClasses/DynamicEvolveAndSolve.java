package main.geneticAlgoritmClasses;

import main.classes.*;
import org.jgap.*;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.MutationOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DynamicEvolveAndSolve {

    public static final double FINAL_FITNESS_VALUE = 500;
    static final int EVOLUTION_REVOLUTIONS = 500;
    static final int POPULATION_SIZE = 250;
    public static  ArrayList<ClassGroup> classGroupsArrayList;
    public static ArrayList<Course> coursesArrayList;
    public static ArrayList<Lecturer> lecturersArrayList;
    public static ArrayList<LectureHall> lectureHallsArrayList;

    public static DynamicSchedule execute(ArrayList<ClassGroup> classGroupsArrayList,
                               ArrayList<Course> coursesArrayList, ArrayList<Lecturer> lecturersArrayList,
                               ArrayList<LectureHall> lectureHallsArrayList) {

        DynamicSchedule result = null;
        DynamicEvolveAndSolve.classGroupsArrayList = classGroupsArrayList;
        DynamicEvolveAndSolve.coursesArrayList = coursesArrayList;
        DynamicEvolveAndSolve.lecturersArrayList = lecturersArrayList;
        DynamicEvolveAndSolve.lectureHallsArrayList = lectureHallsArrayList;

        Configuration.reset();
        Configuration conf = new DefaultConfiguration();

        DynamicSchedule fitnessFunction = new DynamicSchedule();
        
        try {

            conf.setFitnessFunction(fitnessFunction);
            conf.addGeneticOperator(new CrossoverOperator(conf));
            conf.addGeneticOperator(new MutationOperator(conf));
            conf.setKeepPopulationSizeConstant(false);
            conf.setPreservFittestIndividual(false);

            Chromosome chromosome = ChromosomeGeneration.setDynamicChromosome(conf, classGroupsArrayList, coursesArrayList, lecturersArrayList, lectureHallsArrayList);
            conf.setSampleChromosome(chromosome);
            conf.setPopulationSize(POPULATION_SIZE);

            Genotype population = Genotype.randomInitialGenotype(conf);

            long startTime = System.currentTimeMillis();
            int evolucija = 0;

            for (int i = 0; i < EVOLUTION_REVOLUTIONS; i++) {
                evolucija += 1;
                population.evolve();
                IChromosome theFittestOne = population.getFittestChromosome();
                if (theFittestOne.getFitnessValue() == FINAL_FITNESS_VALUE) {
                    break;
                }

            }
            long finishTime = System.currentTimeMillis();
            System.out.println((double) (finishTime - startTime) / 1000 + " sekundi");
            IChromosome theFittestOne = population.getFittestChromosome();
            DynamicSchedule.finalEvaluate(theFittestOne);
            System.out.println("Najbolje rješenje pronađeno nakon: " + evolucija + " evolucija");
            System.out.println("Fitness vrijednost: " + theFittestOne.getFitnessValue() + " \n ");
            result = new DynamicSchedule(theFittestOne);

        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static LinkedList<Number> executeMeasuringPerformance(ArrayList<ClassGroup> classGroupsArrayList,
                                                                 ArrayList<Course> coursesArrayList, ArrayList<Lecturer> lecturersArrayList,
                                                                 ArrayList<LectureHall> lectureHallsArrayList) {

        DynamicEvolveAndSolve.classGroupsArrayList = classGroupsArrayList;
        DynamicEvolveAndSolve.coursesArrayList = coursesArrayList;
        DynamicEvolveAndSolve.lecturersArrayList = lecturersArrayList;
        DynamicEvolveAndSolve.lectureHallsArrayList = lectureHallsArrayList;

        Configuration.reset();
        Configuration conf = new DefaultConfiguration();

        DynamicSchedule fitnessFunction = new DynamicSchedule();
        int evolucija = 0;
        double time = 0;
        double fitnessAchieved = 0;

        try {

            conf.setFitnessFunction(fitnessFunction);
            conf.addGeneticOperator(new CrossoverOperator(conf));
            conf.addGeneticOperator(new MutationOperator(conf));
            conf.setKeepPopulationSizeConstant(false);
            conf.setPreservFittestIndividual(false);

            Chromosome chromosome = ChromosomeGeneration.setDynamicChromosome(conf, classGroupsArrayList, coursesArrayList, lecturersArrayList, lectureHallsArrayList);
            conf.setSampleChromosome(chromosome);
            conf.setPopulationSize(POPULATION_SIZE);

            Genotype population = Genotype.randomInitialGenotype(conf);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < EVOLUTION_REVOLUTIONS; i++) {
                evolucija += 1;
                population.evolve();
                IChromosome theFittestOne = population.getFittestChromosome();
                if (theFittestOne.getFitnessValue() == FINAL_FITNESS_VALUE) {
                    break;
                }

            }
            long finishTime = System.currentTimeMillis();
            time = (double) (finishTime - startTime);
            fitnessAchieved = population.getFittestChromosome().getFitnessValue();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        LinkedList<Number> result = new LinkedList<>();
        result.add(evolucija);
        result.add(time);
        result.add(fitnessAchieved);
        return result;
    }
}