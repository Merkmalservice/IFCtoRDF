package be.ugent;

import org.apache.jena.ext.com.google.common.util.concurrent.AtomicDouble;

import java.util.Objects;

public class ProgressReporter {
    private final ProgressData progressData;
    private ProgressListener progressListener;
    private double startValue;
    private double targetValue;
    private double stepSize;
    private AtomicDouble position = new AtomicDouble(0);
    private long steps = 100L;
    private long step;
    private String taskName = "Progress";
    private double nextStep;
    private MessageGenerator messageGenerator = progressData -> String.format("Step %d of %d", progressData.getStep(), progressData.getSteps());

    public static ProgressReporterBuilder builder(ProgressListener listener, double targetValue) {
        return new ProgressReporterBuilder(listener, targetValue);
    }

    public static class ProgressReporterBuilder {

        private ProgressReporter reporter;

        private ProgressReporterBuilder(ProgressListener progressListener, double targetValue) {
            reporter = new ProgressReporter(progressListener, targetValue);
        }

        public ProgressReporter build() {
            reporter.setup();
            return reporter;
        }

        public ProgressReporterBuilder steps(long steps) {
            reporter.steps = steps;
            return this;
        }

        public ProgressReporterBuilder taskName(String taskName) {
            reporter.taskName = taskName;
            return this;
        }

        public ProgressReporterBuilder startValue(double startValue) {
            reporter.startValue = startValue;
            return this;
        }

        public ProgressReporterBuilder messageGenerator(MessageGenerator messageGenerator) {
            Objects.requireNonNull(messageGenerator);
            reporter.messageGenerator = messageGenerator;
            return this;
        }
    }

    private void setup() {
        this.position.set(this.startValue);
        Objects.requireNonNull(taskName);
        this.step = 0;
        this.stepSize = (targetValue - startValue) / (double) steps;
        if (stepSize == 0) {
            throw new IllegalArgumentException("stepSize cannot be 0");
        }
        this.nextStep = this.position.get() + this.stepSize;
    }

    private ProgressReporter(ProgressListener progressListener, double targetValue) {
        this.progressListener = progressListener;
        Objects.requireNonNull(progressListener);
        this.targetValue = targetValue;
        this.progressData = new ProgressData();
    }

    public void setMessageGenerator(MessageGenerator messageGenerator) {
        this.messageGenerator = messageGenerator;
    }

    public void advanceBy(double relativeValue) {
        this.position.addAndGet(relativeValue);
        adjustStepAndReportIfNewStep();
    }

    public void advanceTo(double absoluteValue) {
        this.position.set(absoluteValue);
        adjustStepAndReportIfNewStep();
    }

    private void adjustStepAndReportIfNewStep() {
        if (isNewstep()) {
            synchronized (this) {
                if (isNewstep()) {
                    reportProgress();
                    this.step = (long) (Math.floor(this.position.get() - this.startValue) / this.stepSize);
                    this.nextStep = this.startValue + this.stepSize * (double) (this.step + 1);
                }
            }
        }
    }

    private boolean isNewstep() {
        double stepDist = this.position.get() - this.startValue;
        return Math.abs(stepDist) >= Math.abs(nextStep);
    }

    private void reportProgress() {
        double percentage = this.progressData.getProgressPercentage();
        progressListener.notifyProgress(this.taskName, messageGenerator.generateProgressMessage(progressData), (float) percentage);
    }

    public interface MessageGenerator {
        String generateProgressMessage(ProgressData progressData);
    }

    public class ProgressData {
        public double getStartValue() {
            return startValue;
        }

        public double getTargetValue() {
            return targetValue;
        }

        public double getStepSize() {
            return stepSize;
        }

        public double getPosition() {
            return position.get();
        }

        public long getSteps() {
            return steps;
        }

        public long getStep() {
            return step;
        }

        public double getNextStep() {
            return nextStep;
        }

        public double getProgressPercentage() {
            return (this.getPosition() - this.getStartValue()) / (this.getTargetValue() - this.getStartValue()) * 100;
        }
    }

}