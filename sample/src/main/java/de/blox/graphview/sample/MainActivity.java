package de.blox.graphview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView graphs = findViewById(R.id.graphs);
        graphs.setLayoutManager(new LinearLayoutManager(this));
        graphs.setAdapter(new GraphListAdapter());
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        graphs.addItemDecoration(decoration);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }
    }

    private class GraphListAdapter extends RecyclerView.Adapter<GraphViewHolder> {

        @NonNull
        @Override
        public GraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_item, parent, false);
            return new GraphViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GraphViewHolder holder, final int position) {
            final MainContent.GraphItem graphItem = MainContent.ITEMS.get(position);
            holder.title.setText(graphItem.title);
            holder.description.setText(graphItem.description);

            holder.itemView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, graphItem.clazz)));
        }

        @Override
        public int getItemCount() {
            return MainContent.ITEMS.size();
        }
    }

    private class GraphViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView description;

        public GraphViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }
}
