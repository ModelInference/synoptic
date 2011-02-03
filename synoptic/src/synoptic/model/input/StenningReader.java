package synoptic.model.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.model.Action;

public class StenningReader<T> {
    VectorTime time = new VectorTime("0");
    private final boolean identifyMessages = false;
    private final HashMap<String, T> messageMap = new HashMap<String, T>();
    private final IBuilder<T> builder;

    class Occurence {
        public T message;
        private final VectorTime time;
        public String nodeName;

        public Occurence(T message, VectorTime time, String nodeName) {
            this.message = message;
            this.time = time;
            this.nodeName = nodeName;
        }

        public VectorTime getTime() {
            return time;
        }
    }

    private Occurence readLine(String line) {
        line = line.trim();
        if (line.length() == 0 || line.charAt(0) == '#') {
            return null;
        }
        String[] fields = line.split(" ");
        String nodeName = fields[0];
        Action action = new Action(fields[2]);
        time = time.step(0);
        action.setStringArgument("role", fields.length > 0 ? fields[0] : "");
        String messageId = fields[1];
        action.setStringArgument("fragment", messageId);
        T message = null;
        if (!messageId.equals("") && identifyMessages) {
            if (messageMap.containsKey(messageId)) {
                message = messageMap.get(messageId);
            } else {
                message = builder.insert(action);
                messageMap.put(messageId, message);
            }
        } else {
            message = builder.insert(action);
        }
        return new Occurence(message, time, nodeName);
    }

    private List<Occurence> readFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        ArrayList<Occurence> occurences = new ArrayList<Occurence>();
        while ((line = reader.readLine()) != null) {
            Occurence o = readLine(line);
            if (o != null) {
                occurences.add(o);
            }
        }
        reader.close();
        return occurences;
    }

    public StenningReader(IBuilder<T> b) {
        builder = b;
    }

    public void readGraphSet(String baseName, int n) throws IOException {
        for (int i = 1; i <= n; ++i) {
            readGraphDirect(baseName.replace("?", "" + i));
            builder.split();
        }
    }

    public void readGraphDirect(String baseName) throws IOException {
        List<Occurence> set = readFile(baseName);
        // generateDirectTemporalRelation(set, g, null, "t");
        generateDirectTemporalRelation(set, "i", false);
    }

    private void generateDirectTemporalRelation(List<Occurence> set,
            String relation, boolean nodeInternal) {
        HashMap<Occurence, HashSet<Occurence>> directSuccessors = new HashMap<Occurence, HashSet<Occurence>>();
        Set<Occurence> noPredecessor = new HashSet<Occurence>(set);
        for (Occurence m1 : set) {
            directSuccessors.put(m1, new HashSet<Occurence>());
            for (Occurence m2 : set) {
                if (nodeInternal && !m2.nodeName.equals(m1.nodeName)) {
                    continue;
                }
                if (m1.getTime().lessThan(m2.getTime())) {
                    boolean add = true;
                    List<Occurence> removeSet = new ArrayList<Occurence>();
                    for (Occurence m : directSuccessors.get(m1)) {
                        if (m2.getTime().lessThan(m.getTime())) {
                            add = true;
                            removeSet.add(m);
                        }
                        if (m.getTime().lessThan(m2.getTime())) {
                            add = false;
                            break;
                        }
                    }
                    directSuccessors.get(m1).removeAll(removeSet);
                    if (add) {
                        directSuccessors.get(m1).add(m2);
                    }
                }
            }
        }

        for (Occurence m : directSuccessors.keySet()) {
            for (Occurence s : directSuccessors.get(m)) {
                builder.connect(m.message, s.message, relation);
                noPredecessor.remove(s);
            }
        }
        for (Occurence m : noPredecessor) {
            builder.addInitial(m.message, relation);
        }
    }

}
