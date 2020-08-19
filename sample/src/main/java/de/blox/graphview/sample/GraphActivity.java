package de.blox.graphview.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import de.blox.graphview.AbstractGraphAdapter;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;

public abstract class GraphActivity extends AppCompatActivity {

    private int nodeCount = 1;

    private Node currentNode;

    protected RecyclerView graphView;
    protected AbstractGraphAdapter<NodeViewHolder> adapter;
    private FloatingActionButton fab;

    private void setupGraphView(Graph graph) {
        adapter = new AbstractGraphAdapter<NodeViewHolder>() {
            @NonNull
            @Override
            public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.node, parent, false);
                return new NodeViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
                holder.textView.setText(Objects.requireNonNull(getNodeData(position)).toString());
            }
        };

        graphView.setAdapter(adapter);
        adapter.submitGraph(graph);
    }

    protected abstract Graph createGraph();

    protected abstract void setLayoutManager();

    private void setupFab(final Graph graph) {
        fab = findViewById(R.id.addNode);
        fab.setOnClickListener(v -> {
            final Node newNode = new Node(getNodeText());

            if (currentNode != null) {
                graph.addEdge(currentNode, newNode);
            } else {
                graph.addNode(newNode);
            }
            adapter.notifyDataSetChanged();
        });

        fab.setOnLongClickListener(v -> {
            if (currentNode != null) {
                graph.removeNode(currentNode);
                currentNode = null;
                adapter.notifyDataSetChanged();
                fab.hide();
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

    protected abstract void setEdgeDecoration();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        final Graph graph = createGraph();
        setupToolbar();
        setupFab(graph);
        graphView = findViewById(R.id.graphView);
        setLayoutManager();
        setEdgeDecoration();
        setupGraphView(graph);

    }

    protected class NodeViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        NodeViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);

            itemView.setOnClickListener(v -> {
                if (!fab.isShown()) {
                    fab.show();
                }
                currentNode = adapter.getNode(getAdapterPosition());
                Snackbar.make(itemView, "Clicked on " + Objects.requireNonNull(adapter.getNodeData(getAdapterPosition())).toString(),
                        Snackbar.LENGTH_SHORT).show();
            });
        }
    }

    protected String getNodeText() {
        return "Node " + nodeCount++;
    }
}
