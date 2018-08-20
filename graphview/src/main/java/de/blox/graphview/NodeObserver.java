package de.blox.graphview;

/**
 *
 */

public interface NodeObserver {
    void notifyDataChanged(Node node);

    void notifyNodeAdded(Node node);

    void notifyNodeRemoved(Node node);

    void notifyInvalidated();
}
