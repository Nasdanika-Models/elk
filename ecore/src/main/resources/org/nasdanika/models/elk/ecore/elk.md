```drawio-resource
elk.drawio
```

* [Sources](https://github.com/Nasdanika-Models/elk)    


## ELK ↔ Draw.io Integration

This section explains how to integrate ELK layout with Draw.io model objects using the Nasdanika helper code (examples drawn from `TestDrawioLayout.java`).
It shows how to generate a small diagram, apply ELK layout options and tune spacing (including setting node spacing to 150 px).

### Summary / contract

- Inputs: a `Document` composed of `Page` → `Layer` → `Node` and `Connection` elements (Nasdanika drawio model).
- Outputs: a Draw.io XML string saved to `target/generated.drawio` (and an XMI layout snapshot optionally).
- Error modes: unknown ELK option keys will be logged; if ELK option parsing fails, the `LayoutMetaDataService`/`LayoutOptionData` will indicate unknown keys.
- Success criteria: layout engine returns non-zero graph size and node geometries are updated on Draw.io nodes.

### Contents
- Quickstart
- Configuring ELK options (set spacing to 150 px)
- `applyLayoutOptions` helper (how it works + debug)
- Running and verifying the generated drawio
- Troubleshooting / tips

---

### Quickstart (core flow)

1. Build a small drawio `Document` (pages, layers, nodes, connections).
2. Convert Nasdanika drawio model to ELK graph elements via `DrawioElkGraphFactory` + `Transformer`.
3. Wire ELK `ElkEdge` objects for each `Connection`.
4. Apply ELK options to the root `ElkNode` (page graph).
5. Run `RecursiveGraphLayoutEngine.layout(...)`.
6. Copy calculated geometry back to drawio nodes and edges, then save the document.

Minimal example (derived from `TestDrawioLayout.java`):

```java
// 1. Create drawio document
Document document = Document.create(false, null);
Page page = document.createPage();
Model model = page.getModel();
Root root = model.getRoot();
Layer<?> layer = root.createLayer();

// add two nodes
Node source = layer.createNode();
source.setLabel("Source");
source.getGeometry().setWidth(120);
source.getGeometry().setHeight(30);

Node target = layer.createNode();
target.setLabel("Target");
target.getGeometry().setWidth(120);
target.getGeometry().setHeight(30);

// add connection
Connection connection = layer.createConnection(source, target);
connection.setLabel("Edge");

// 2. Transform to ELK graph elements
DrawioElkGraphFactory factory = new DrawioElkGraphFactory();
Transformer<Element,ElkGraphElement> transformer = new Transformer<>(factory);
Map<Element, ElkGraphElement> graphElements =
    transformer.transform(document.stream().toList(), false, new PrintStreamProgressMonitor());

// 3. Create ELK edges for drawio connections
Map<ElkEdge, Connection> connectionMap = new HashMap<>();
document.stream().filter(Connection.class::isInstance)
    .map(Connection.class::cast)
    .forEach(conn -> {
        ElkConnectableShape elkSource = (ElkConnectableShape) graphElements.get(conn.getSource());
        ElkConnectableShape elkTarget = (ElkConnectableShape) graphElements.get(conn.getTarget());
        ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(elkSource, elkTarget);
        elkEdge.setIdentifier(conn.getId());
        connectionMap.put(elkEdge, conn);
    });

// 4. Apply options & layout
ElkNode pageGraph = (ElkNode) graphElements.get(page);
applyLayoutOptions(pageGraph, LAYERED_CONFIG_MAP); // config map shown later
RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
engine.layout(pageGraph, new BasicProgressMonitor());

// 5. Copy geometry back to drawio and save
// iterate Node elements, read ElkNode x/y/width/height and set drawio node geometry
```

---

### Configuration: set spacing to 150 px (layered algorithm)

ELK option keys are namespaced. For layered algorithm options you need to set the layered-prefixed keys so that they resolve to `org.eclipse.elk.layered.*` keys.

Use keys like:
- `layered.spacing.nodeNode` => `org.eclipse.elk.layered.spacing.nodeNode`
- `layered.spacing.nodeNodeBetweenLayers` => `org.eclipse.elk.layered.spacing.nodeNodeBetweenLayers`

Example config (YAML-like / JSON-like shown in `TestDrawioLayout.java`):

```yaml
algorithm: org.eclipse.elk.layered
direction: RIGHT

hierarchyHandling: INCLUDE_CHILDREN

layered.spacing.nodeNodeBetweenLayers: 150
spacing.nodeNode: 150
spacing.edgeNode: 40
spacing.edgeEdge: 20

layered.layering.strategy: NETWORK_SIMPLEX
layered.nodePlacement.strategy: BRANDES_KOEPF
layered.nodePlacement.favorStraightEdges: true

edgeRouting: ORTHOGONAL
edgeLabels.inline: false
edgeLabels.placement: CENTER
```

Notes:
- We include both `layered.spacing.nodeNodeBetweenLayers` and `spacing.nodeNode` to cover both layered-specific and generic spacing keys. After processing by `applyLayoutOptions`, these become `org.eclipse.elk.layered.spacing.nodeNodeBetweenLayers` and `org.eclipse.elk.spacing.nodeNode` respectively.
- The most direct / reliable key for layered spacing is `org.eclipse.elk.layered.spacing.nodeNode` (use `"layered.spacing.nodeNode": 150` in your map).

Measurement:
- ELK spacing values are device/coordinate units (effectively pixels in typical renderings). Setting `150` is a large spacing and should be visually apparent.

---

### `applyLayoutOptions` helper — how it works

`applyLayoutOptions(ElkNode graph, Map<String,Object> config)` concatenates `"org.eclipse.elk." + key` and asks `LayoutMetaDataService` for `LayoutOptionData`. It parses the string value into the typed option value and sets the property on the graph.

Conceptual code:

```java
public static void applyLayoutOptions(ElkNode graph, Map<String,Object> config) {
    for (Map.Entry<String, Object> configEntry : config.entrySet()) {
        String key = "org.eclipse.elk." + configEntry.getKey();
        LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(key);
        if (optionData == null) {
            System.err.println("Unknown ELK option: " + key);
            continue;
        }
        Object typedValue = optionData.parseValue(String.valueOf(configEntry.getValue()));
        graph.setProperty((IProperty<Object>) optionData, typedValue);
    }
}
```

Important:
- `LayoutMetaDataService` maps the string key to an ELK option descriptor and knows the expected type. This is safer than trying to set raw primitives directly.
- Options must be applied to the root `ElkNode` (page graph) before calling `engine.layout(...)`.

---

### Debugging / verifying options (recommended temporary changes)

To make sure ELK accepted your spacing setting, temporarily augment `applyLayoutOptions` with debug prints:

```java
LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(key);
if (optionData == null) {
    System.err.println("Unknown ELK option: " + key);
    continue;
}
Object typedValue = optionData.parseValue(String.valueOf(configEntry.getValue()));
System.out.println("Applying ELK option: " + key + " => " + typedValue + " (type=" + optionData.getType().getSimpleName() + ")");
graph.setProperty((IProperty<Object>) optionData, typedValue);

// read back and confirm
try {
    Object readBack = graph.getProperty((IProperty<Object>) optionData);
    System.out.println("Readback value for " + key + ": " + readBack);
} catch (Exception ex) {
    System.err.println("Unable to read property for " + key + ": " + ex.getMessage());
}
```

Look for a printed line such as:

`Applying ELK option: org.eclipse.elk.layered.spacing.nodeNode => 150 (type=Integer)`

If you see `Unknown ELK option: org.eclipse.elk.layered.spacing.nodeNode`, the option key string is wrong or the ELK version in classpath doesn't expose that option — verify ELK version.

---

### Run the test and inspect the result

From the module root that contains the test, run the test class (example using Maven). Replace path/redirection as appropriate for your project root.

Windows (cmd.exe):

```cmd
mvn -Dtest=org.nasdanika.models.elk.drawio.tests.TestDrawioLayout#testGenerateAndLayout test
```

What to inspect:
- `target/generated.drawio` (open with draw.io / diagrams.net).
- `target/generated.xmi` (if produced) — contains ELK graph snapshot.
- Console logs: look for the debug prints suggested above and ensure the applied spacing shows `150`.

---

### Troubleshooting / tips

- Option key names:
  - For layered-specific options use the `layered.*` prefix in your map so `applyLayoutOptions` produces `org.eclipse.elk.layered.*`.
  - Example: `"layered.spacing.nodeNode": 150` -> `org.eclipse.elk.layered.spacing.nodeNode`.
- Ensure `algorithm` is set to `org.eclipse.elk.layered` before applying layered-prefixed options — otherwise layered-specific keys might not influence the actual algorithm choice or ordering.
- If spacing still appears incorrect:
  - Check actual node bounds: spacing is gap between node borders; if nodes are large, required gap can look small.
  - Check whether nodes ended up on same layer or different layers; set both `nodeNode` and `nodeNodeBetweenLayers` to control both intra-layer and inter-layer spacing.
- ELK version: Some option keys are added in newer ELK releases. If `LayoutMetaDataService.getOptionData(...)` returns null for a valid key, you may be on an older ELK. Upgrade ELK or change to keys available in your ELK version.
- Ports & alignment:
  - Port constraints and alignment can affect how edges attach and influence layout. Add:
    - `layered.portConstraints: FIXED_SIDE`
    - `layered.portAlignment.default: CENTER`
- If you rely on connection points/ports (explicit port locations), ensure their constraints don't force short edge segments that visually look like nodes are close.

---

### Examples in the repo (where to look)

- `TestDrawioLayout.java` contains:
  - `LAYERED_CONFIG_MAP` — shows a real config you can reuse.
  - `applyLayoutOptions` — the canonical helper.
  - Test cases: `testGenerateAndLayout()` and `testGenerateAndLayoutConnectionPoints()` — full end-to-end examples (create nodes, connections, transform, layout, copy back geometry, save).
