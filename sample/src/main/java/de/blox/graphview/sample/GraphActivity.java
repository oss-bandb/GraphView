package de.blox.graphview.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphAdapter;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;

public abstract class GraphActivity extends AppCompatActivity {
    private int nodeCount = 1;
    private Node currentNode;
    protected GraphView graphView;
    protected GraphAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        final Graph graph = createGraph();
        setupToolbar();
        setupFab(graph);
        setupAdapter(graph);
    }

    private void setupAdapter(Graph graph) {
        graphView = findViewById(R.id.graph);
        setLayout(graphView);
        adapter = new GraphAdapter<GraphView.ViewHolder>(graph) {

            @Override
            public int getCount() {
                return graph.getNodeCount();
            }

            @Override
            public Object getItem(int position) {
                return graph.getNodeAtPosition(position);
            }

            @Override
            public boolean isEmpty() {
                return graph.hasNodes();
            }

            @NonNull
            @Override
            public GraphView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.node, parent, false);
                return new SimpleViewHolder(view);
            }

            @Override
            public void onBindViewHolder(GraphView.ViewHolder viewHolder, Object data, int position) {
                ((SimpleViewHolder) viewHolder).textView.setText(data.toString());
            }

            class SimpleViewHolder extends GraphView.ViewHolder {
                TextView textView;

                SimpleViewHolder(View itemView) {
                    super(itemView);
                    textView = itemView.findViewById(R.id.textView);
                }
            }
        };
        graphView.setAdapter(adapter);
        graphView.setOnItemClickListener((parent, view, position, id) -> {
            currentNode = (Node) adapter.getItem(position);
            Snackbar.make(graphView, "Clicked on " + currentNode.getData().toString(), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void setupFab(final Graph graph) {
        FloatingActionButton addButton = findViewById(R.id.addNode);
        addButton.setOnClickListener(v -> {
            final Node newNode = new Node(getNodeText());

            if (currentNode != null) {
                graph.addEdge(currentNode, newNode);
            } else {
                graph.addNode(newNode);
            }
            adapter.notifyDataSetChanged();
        });

        addButton.setOnLongClickListener(v -> {
            if (currentNode != null) {
                graph.removeNode(currentNode);
                currentNode = null;
            }
            return true;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public abstract Graph createGraph();

    public abstract void setLayout(GraphView view);
    protected String getNodeText() {
        return "Node " + nodeCount++;
    }
}
