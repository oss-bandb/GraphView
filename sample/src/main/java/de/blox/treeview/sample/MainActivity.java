package de.blox.treeview.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import de.blox.treeview.BaseTreeAdapter;
import de.blox.treeview.TreeNode;
import de.blox.treeview.TreeView;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private TreeNode mCurrentNode;
    private int nodeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TreeView treeView = findViewById(R.id.tree);
        FloatingActionButton addButton = findViewById(R.id.addNode);

        final BaseTreeAdapter adapter = new BaseTreeAdapter<ViewHolder>(this, R.layout.node) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(View view) {
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                viewHolder.mTextView.setText(data.toString());
            }
        };

        treeView.setAdapter(adapter);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentNode != null) {
                    mCurrentNode.addChild(new TreeNode(getNodeText()));
                } else {
                    adapter.setRootNode(new TreeNode(getNodeText()));
                }
            }
        });

        // example tree
        mCurrentNode = new TreeNode(getNodeText());
        mCurrentNode.addChild(new TreeNode(getNodeText()));
        final TreeNode child3 = new TreeNode(getNodeText());
        child3.addChild(new TreeNode(getNodeText()));
        final TreeNode child6 = new TreeNode(getNodeText());
        child6.addChild(new TreeNode(getNodeText()));
        child6.addChild(new TreeNode(getNodeText()));
        child3.addChild(child6);
        mCurrentNode.addChild(child3);
        final TreeNode child4 = new TreeNode(getNodeText());
        child4.addChild(new TreeNode(getNodeText()));
        child4.addChild(new TreeNode(getNodeText()));
        mCurrentNode.addChild(child4);

        adapter.setRootNode(mCurrentNode);
        treeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentNode = adapter.getNode(position);
                Snackbar.make(treeView, "Clicked on " + mCurrentNode.getData().toString(), LENGTH_SHORT).show();
            }
        });
    }

    private class ViewHolder {
        TextView mTextView;

        ViewHolder(View view) {
            mTextView = view.findViewById(R.id.textView);
        }
    }

    private String getNodeText() {
        return "Node " + nodeCount++;
    }
}
