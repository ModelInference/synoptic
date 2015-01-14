package synoptic.algorithms.graphops;

import java.util.LinkedList;

import synoptic.model.PartitionGraph;

/**
 * Links a series of operation into a sequence.
 * 
 */
public class OperationSequence implements IOperation {
    LinkedList<IOperation> sequence = new LinkedList<IOperation>();

    @Override
    public IOperation commit(PartitionGraph g) {
        OperationSequence rewindOperation = new OperationSequence();
        for (IOperation op : sequence) {
            rewindOperation.addFirst(g.apply(op));
        }
        return rewindOperation;
    }

    /**
     * Add an operation before the head of the sequence.
     * 
     * @param operation
     *            the operation to add
     */
    public void addFirst(IOperation operation) {
        sequence.addFirst(operation);
    }
}
