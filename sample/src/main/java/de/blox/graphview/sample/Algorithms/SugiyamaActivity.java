package de.blox.graphview.sample.Algorithms;

import de.blox.graphview.Graph;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.layered.SugiyamaAlgorithm;
import de.blox.graphview.sample.GraphActivity;

public class SugiyamaActivity extends GraphActivity {

    @Override
    public Graph createGraph() {
        final Graph graph = new Graph();

        final Node node1 = new Node(getNodeText());
        final Node node2 = new Node(getNodeText());
        final Node node3 = new Node(getNodeText());
        final Node node4 = new Node(getNodeText());
        final Node node5 = new Node(getNodeText());
        final Node node6 = new Node(getNodeText());
        final Node node8 = new Node(getNodeText());
        final Node node7 = new Node(getNodeText());
        final Node node9 = new Node(getNodeText());
        final Node node10 = new Node(getNodeText());
        final Node node11 = new Node(getNodeText());
        final Node node12 = new Node(getNodeText());
        final Node node13 = new Node(getNodeText());
        final Node node14 = new Node(getNodeText());
        final Node node15 = new Node(getNodeText());
        final Node node16 = new Node(getNodeText());
        final Node node17 = new Node(getNodeText());
        final Node node18 = new Node(getNodeText());
        final Node node19 = new Node(getNodeText());
        final Node node20 = new Node(getNodeText());
        final Node node21 = new Node(getNodeText());
        final Node node22 = new Node(getNodeText());
        final Node node23 = new Node(getNodeText());

        graph.addEdge(node1, node13);
        graph.addEdge(node1, node21);
        graph.addEdge(node1, node4);
        graph.addEdge(node1, node3);
        graph.addEdge(node2, node3);
        graph.addEdge(node2, node20);
        graph.addEdge(node3, node4);
        graph.addEdge(node3, node5);
        graph.addEdge(node3, node23);
        graph.addEdge(node4, node6);
        graph.addEdge(node5, node7);
        graph.addEdge(node6, node8);
        graph.addEdge(node6, node16);
        graph.addEdge(node6, node23);
        graph.addEdge(node7, node9);
        graph.addEdge(node8, node10);
        graph.addEdge(node8, node11);
        graph.addEdge(node9, node12);
        graph.addEdge(node10, node13);
        graph.addEdge(node10, node14);
        graph.addEdge(node10, node15);
        graph.addEdge(node11, node15);
        graph.addEdge(node11, node16);
        graph.addEdge(node12, node20);
        graph.addEdge(node13, node17);
        graph.addEdge(node14, node17);
        graph.addEdge(node14, node18);
        graph.addEdge(node16, node18);
        graph.addEdge(node16, node19);
        graph.addEdge(node16, node20);
        graph.addEdge(node18, node21);
        graph.addEdge(node19, node22);
        graph.addEdge(node21, node23);
        graph.addEdge(node22, node23);

        return graph;
    }

    @Override
    public void setLayout(GraphView view) {
        view.setLayout(new SugiyamaAlgorithm());
    }
}
