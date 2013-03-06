package synoptic.benchmarks;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A class to facilitate synoptic.benchmarks. Just set your benchmark up as a
 * ConfigureableBenchmark, and pass it to the constructor. The
 * synoptic.benchmarks can be run by passing the configuration arguments to run.
 * The benchmark will be run for each argument in the collection. Afterwards,
 * the ConfigureableBenchmark can be queried for the results.
 * 
 * @author Sigurd Schneider
 * @param <ArgumentType>
 *            The argument type for the benchmark.
 */
public class Benchmark<ArgumentType> {
    /**
     * The number of repetitions to run the benchmark.
     */
    private int repetitions = 2;
    /**
     * The factory that creates the actual synoptic.benchmarks from the configs.
     */
    private final ConfigureableBenchmark<ArgumentType> taskFactory;

    /**
     * An interface to a configurable benchmark.
     * 
     * @author Sigurd Schneider
     * @param <ArgumentType>
     *            The argument for the benchmark.
     */
    static public abstract class ConfigureableBenchmark<ArgumentType> {
        /**
         * A hash map to store the results of the runs.
         */
        private final LinkedHashMap<ArgumentType, PerformanceMetrics> results = new LinkedHashMap<ArgumentType, PerformanceMetrics>();

        /**
         * Records the results of a benchmark run (identified by the arguments).
         * 
         * @param arg
         *            identifies the benchmark
         * @param performanceMetrics
         *            the results
         */
        private synchronized void record(ArgumentType arg,
                PerformanceMetrics performanceMetrics) {
            results.put(arg, performanceMetrics);
        }

        /**
         * The method our clients have to implement. This method should run the
         * benchmark for the configure given in val.
         * 
         * @param val
         *            the config for the benchmark.
         */
        protected abstract void run(ArgumentType val);

        /**
         * Get the results for the run with argument arg. Will return null if
         * the benchmark was not run.
         * 
         * @param arg
         *            the argument
         * @return the metrics collected for the benchmark run
         */
        public synchronized PerformanceMetrics getResults(ArgumentType arg) {
            return results.get(arg);
        }

        /**
         * Returns a runnable that represents the benchmark. This is provided
         * for future parallelization. TODO: fix the race for performance
         * metrics.
         * 
         * @param val
         *            the configuration to use
         * @return a runnable that encapsulates the benchmark
         */
        public final Runnable configure(final ArgumentType val) {
            return new Runnable() {
                public void run() {
                    ConfigureableBenchmark.this.run(val);
                }
            };
        }
    }

    /**
     * Constructs a benchmark.
     * 
     * @param repetitions
     *            the number of repetitions to average about
     * @param taskFactory
     *            the taskFactory that creates synoptic.benchmarks from
     *            configuration arguments
     */
    public Benchmark(int repetitions,
            ConfigureableBenchmark<ArgumentType> taskFactory) {
        this.repetitions = repetitions;
        this.taskFactory = taskFactory;
    }

    /**
     * Run the synoptic.benchmarks for each arg in arguments.
     * 
     * @param arguments
     *            the arguments to run the benchmark for
     */
    public void run(Collection<ArgumentType> arguments) {
        boolean isFirst = true;
        for (ArgumentType arg : arguments) {
            for (int i = 0; i < repetitions; ++i) {
                Runnable task = taskFactory.configure(arg);
                TimedTask total = new TimedTask("total");
                task.run();
                total.stop();
                PerformanceMetrics.get().record(total);

            }
            if (isFirst || true) {
                System.out.println(PerformanceMetrics.get().getHeader());
                isFirst = false;
            }
            System.out.println(PerformanceMetrics.get().getDataDividedBy(
                    repetitions));
            PerformanceMetrics metrics = PerformanceMetrics.get();
            PerformanceMetrics.clear();
            taskFactory.record(arg, metrics);
        }
    }
}