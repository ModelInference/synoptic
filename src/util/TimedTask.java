package util;

public class TimedTask {
        private long time = System.currentTimeMillis();
        private int i = 1;
        private Long timeEnd = null;
        private int interval;
		private String task;

        public TimedTask(String task, int interval) {
                this.interval = interval;
                this.task = task;
        }

        public void step() {
                i++;
                if (i % interval == 0) {
                        System.out.print("+");
                }
        }

        public void stop() {
                timeEnd = System.currentTimeMillis();
        }

        public String toString() {
                if (timeEnd == null) {
                        stop();
                }
                return task +" in " + (timeEnd - time)
                                + "ms";
        }

		public String getTask() {
			return task;
		}

		public Long getTime() {
			return timeEnd-time;
		}
}

