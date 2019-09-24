**참고: 이 라이브러리는 개발 초기 단계에 있습니다. Stable version 출시 전 까지 공개 API에 큰 변화가 있을 수 있습니다.**

GraphView
===========

Android GraphView는 데이터를 그래프 형태로 보여주는 데 사용됩니다.

![alt Logo](image/GraphView_logo.png "Graph Logo")

개요
========
이 라이브러리는 다른 그래프 레이아웃을 지원하기 위해 개발되었습니다.

다운로드
========

```groovy
dependencies {
    implementation 'de.blox:graphview:0.6.0'
}
```
레이아웃
======
### 트리
Walker's algorithm with Buchheim's runtime improvements(`BuchheimWalkerAlgorithm` class)를 사용하였습니다. 다양한 orientation들을 지원합니다. `BuchheimWalkerConfiguration.Builder.setOrientation(int)` 또는 `ORIENTATION_LEFT_RIGHT`, `ORIENTATION_RIGHT_LEFT`, `ORIENTATION_TOP_BOTTOM` 그리고
`ORIENTATION_BOTTOM_TOP` 만 사용하면 됩니다(default). 더 나아가 sibling-, level-, subtree separation과 같은 매개변수 설정 또한 가능합니다.
### 방향 그래프
방향 그래프는 인력/척력을 시뮬레이션하여 그려집니다. 이를 위해  Fruchterman과 Reingold (`FruchtermanReingoldAlgorithm` class)의 알고리즘이 구현되었습니다 (현재 작은 그래프에서만 작동함).
### 계층 그래프
Sugiyama et al의 알고리즘(`SugiyamaAlgorithm` class)을 사용합니다. 여러 계층을 가진 그래프를 그리기 위해, 그래프의 계층적 구조를 이용합니다. 또한 `SugiyamaConfiguration.Builder`를 활용하여 노드, 레벨 분할을 위한 매개변수를 설정할 수 있습니다.

사용
======

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <de.blox.graphview.GraphView
     android:id="@+id/graph"
     android:layout_width="match_parent"
     android:layout_height="match_parent">
    </de.blox.graphview.GraphView>
</LinearLayout>
```
노드 레이아웃을 원하는 대로 만들 수 있습니다. ```node.xml```같은 레이아웃 파일을 정의하기만 하면 됩니다.
```xml
<android.support.v7.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
                                    xmlns:card_view="http://schemas.android.com/apk/res-auto"
                                    android:id="@+id/card_view"
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_gravity="center"
                                    android:layout_margin="5dp"
                                    card_view:cardBackgroundColor="@android:color/holo_blue_dark"
                                    card_view:cardElevation="16dp"
                                    card_view:contentPadding="10dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <TextView
            android:id="@+id/textView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="@android:color/white"
            android:textStyle="bold"/>

    </LinearLayout>
</android.support.v7.widget.CardView>
```

다음은 adapter와 함께 사용한 예시 입니다.

```java
public class MainActivity extends AppCompatActivity {
    private int nodeCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GraphView graphView = findViewById(R.id.graph);

        // 예시 트리
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

        graph.addEdge(node1, node2);
        graph.addEdge(node1, node3);
        graph.addEdge(node1, node4);
        graph.addEdge(node2, node5);
        graph.addEdge(node2, node6);
        graph.addEdge(node6, node7);
        graph.addEdge(node6, node8);
        graph.addEdge(node4, node9);
        graph.addEdge(node4, node10);
        graph.addEdge(node4, node11);
        graph.addEdge(node11, node12);

        // 생성자 또는 dapter.setGraph(Graph) 매서드를 사용하여 그래프를 설정할 수 있습니다.
        final BaseGraphAdapter<ViewHolder> adapter = new BaseGraphAdapter<ViewHolder>(graph) {
        
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.node, parent, false);
                return new SimpleViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                ((SimpleViewHolder)viewHolder).textView.setText(data.toString());
            }
        };
        graphView.setAdapter(adapter);
        
        // 이 부분에 알고리즘을 설정합니다
        final BuchheimWalkerConfiguration configuration = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
                .build();
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(configuration));
    }
    
    private String getNodeText() {
        return "Node " + nodeCount++;
    }
}
```

ViewHolder 클래스는 ViewHolder를 extend 해야 합니다:
```java
class SimpleViewHolder extends ViewHolder {
    TextView textView;

    SimpleViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.textView);
    }
}
```

사용자 지정
=============

커스텀 속성을 사용하려면 먼저 namespace 를 추가해야합니다: ```
    xmlns:app="http://schemas.android.com/apk/res-auto"```

| 속성              | 형식      | 예시                              | 설명       |
|------------------|-----------|--------------------------------|------------|
| lineThickness   | Dimension | 10dp                           | 연결선의 두꺼운 정도를 설정합니다
| lineColor       | Color     | "@android:color/holo_red_dark" | 연결선의 색상을 설정합니다
| useMaxSize      | Boolean   | true                           | 각 노드를 동일한 크기로 사용합니다

프로그래밍 방식으로 이를 사용하길 하려는 경우, 각각의 속성은 GraphView 클래스에서 그에 따른 설정을 가집니다.

GraphView는 내부적으로 [ZoomLayout](https://github.com/natario1/ZoomLayout)를 줌 기능에 사용합니다. 줌 정도 값을 변경하려면 ZoomLayout 프로젝트 사이트에 명시된 다른 속성을 사용하십시오.

예시
========
#### 트리
![alt Example](image/Tree.png "Tree Example")

#### 방향 그래프
![alt Example](image/Graph.png "Graph Example")

#### 계층 그래프
![alt Example](image/LayeredGraph.png "Layered Graph Example")

라이선스
=======

    Copyright 2019 Team-Blox

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.