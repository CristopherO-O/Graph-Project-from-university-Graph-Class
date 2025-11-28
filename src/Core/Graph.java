/*
 * Autor: Cristopher Resende
 * Data: 20/11/2025
 * Descrição: estrutura do Grafo com ArrayList de arestas quando eu decidi mudar pra lista de adjacencia ja era tarde de mais
 */

package src.Core;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class Graph {
    private int nodesNum;
    private int quantity;
    private ArrayList<Edge> edges;

    public Graph(int nodesNum) {
        this.nodesNum = nodesNum;
        this.quantity = 0;
        this.edges = new ArrayList<>();
    }
    
    
    // ----- getters -----
    public int getNodesNum(){ return this.nodesNum; }
    public int getQuantity(){ return this.quantity; }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    // ----- setters -----
    public void setNodesNum(int x){ this.nodesNum = x; }

    // ----- insere aresta no Grafo -----
    public void insertEdge(int node1, int node2, int weight){
        edges.add(new Edge(node1, node2, weight));
        this.quantity++;
    }

    // ----- imprime o grafo -----
    public void printGraph(){
        System.out.printf("The graph has %d nodes and %d edges:\n", nodesNum, quantity);
        for(Edge e : edges){
            if(e.getName() == ""){
                System.out.printf("%d -> %d ; weight:%d\n", e.getNode1(), e.getNode2(), e.getWeight());
            } else {
                System.out.printf("%d -> %d ; weight:%d; name: %s\n", e.getNode1(), e.getNode2(), e.getWeight(), e.getName());
            }
        }
    }

    

    // ----- Carrega grafo do em formato de Matriz de Adjacência -----
    public static Graph loadGraphFromAdjacencyMatrix(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        List<List<Integer>> matrixData = new ArrayList<>();
        int nodesCount = 0;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            List<Integer> row = new ArrayList<>();

            for (String part : parts) {
                if (!part.isEmpty()) {
                    try {
                        row.add(Integer.parseInt(part));
                    } catch (NumberFormatException e) {
                        System.err.println("Aviso: Valor não numérico encontrado: " + part);
                    }
                }
            }
            if (!row.isEmpty()) {
                matrixData.add(row);
                if (nodesCount == 0) {
                    nodesCount = row.size();
                } else if (nodesCount != row.size()) {
                    throw new IOException("O arquivo não contém uma matriz quadrada válida.");
                }
            }
        }
        br.close();

        if (nodesCount == 0 || nodesCount != matrixData.size()) {
            throw new IOException("O arquivo não contém uma matriz quadrada válida (número de linhas não corresponde ao número de colunas).");
        }
        
        Graph g = new Graph(nodesCount);

        for (int i = 0; i < nodesCount; i++) {       // i+1 = Nó Origem (0-based)
            for (int j = 0; j < nodesCount; j++) {   // j+1 = Nó Destino (0-based)
                
                int weight = matrixData.get(i).get(j);
                
                if (i != j) {                    
                    int node1 = i + 1;
                    int node2 = j + 1;
                    g.insertEdge(node1, node2, weight);
                }
            }
        }

        return g;
    }

    // ------ carrega grafo de uma lista de arestas -----
    public static Graph loadGraphFromEdgeList(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        ArrayList<Edge> list = new ArrayList<>();
        int maxNodeId = 0; 

        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            
            if (parts.length < 3) continue;

            try {
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);

                list.add(new Edge(u, v, w));
                
                maxNodeId = Math.max(maxNodeId, u);
                maxNodeId = Math.max(maxNodeId, v);
            } catch (NumberFormatException e) {
                System.err.println("Aviso: Linha de aresta inválida ignorada: " + line);
            }
        }

        br.close();
        
        if (maxNodeId == 0) {
             throw new IOException("Nenhuma aresta válida encontrada no arquivo de lista de arestas.");
        }

        Graph g = new Graph(maxNodeId); 

        for (Edge e : list)
            g.insertEdge(e.getNode1(), e.getNode2(), e.getWeight());

        return g;
    }

    // ----- Carrega grafo a partir de um arquivo, detectando o formato -----
    public static Graph loadGraphFromFile(String filename) throws IOException {
        try (BufferedReader initialBr = new BufferedReader(new FileReader(filename))) {
            
            String firstLine = initialBr.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IOException("O arquivo está vazio ou contém apenas linhas em branco.");
            }

            String[] parts = firstLine.trim().split("\\s+");
            
            List<String> validParts = new ArrayList<>();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    validParts.add(part);
                }
            }
            int validPartsCount = validParts.size();

            // 1. Tenta extrair o primeiro valor como número
            boolean isFirstValueZero = false;
            try {
                if (validPartsCount > 0) {
                    if (Integer.parseInt(validParts.get(0)) == 0) {
                        isFirstValueZero = true;
                    }
                }
            } catch (NumberFormatException e) {
                // Se o primeiro elemento não for um número (e.g., cabeçalho de texto), assumimos Lista de Arestas (Formato padrão)
                isFirstValueZero = false; 
            }

            // 2. Executa a Heurística Aprimorada
            
            // A. Se tem 3 partes E o primeiro valor não é zero, é claramente Lista de Arestas.
            if (validPartsCount == 3 && !isFirstValueZero) {
                System.out.println("Formato detectado: Lista de Arestas (u, v, w).");
                return loadGraphFromEdgeList(filename); 
            } 
            
            // B. Se tem 3 partes OU mais, E o primeiro valor é zero, é muito provável que seja Matriz de Adjacência.
            else if (validPartsCount >= 3 && isFirstValueZero) {
                System.out.println("Formato detectado: Matriz de Adjacência (primeiro elemento é zero).");
                return loadGraphFromAdjacencyMatrix(filename);
            }
            
            // C. Caso padrão (e.g., se houver cabeçalho de texto ou a contagem não bater)
            else {
                System.out.println("Formato incerto/não-padrão. Tentando carregar como Lista de Arestas (Formato padrão)...");
                // Pela sua lógica original, se não é matriz, deve ser lista de arestas.
                return loadGraphFromEdgeList(filename);
            }

        } catch (IOException e) {
            throw new IOException("Erro ao carregar o grafo de " + filename + ": " + e.getMessage());
        }
    }

    // ----- lê arquivo de GML-----
    public static Graph loadGraphFromGML(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;
        // Estruturas para armazenar dados de arestas temporariamente
        List<Integer> tempSources = new ArrayList<>();
        List<Integer> tempTargets = new ArrayList<>();
        List<String> tempNames = new ArrayList<>();
        HashSet<Integer> gmlNodeIds = new HashSet<>();
        
        // Variáveis temporárias para armazenar os dados de uma única aresta
        int currentSource = -1;
        int currentTarget = -1;
        String currentName = "";

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.contains("node [")) {
                // Prepara para ler o ID do nó
            } else if (line.startsWith("id ")) {
                try {
                    int nodeId = Integer.parseInt(line.split("\\s+")[1]);
                    gmlNodeIds.add(nodeId);
                } catch (NumberFormatException ignored) {}
            }
            
            // 2. Processar Arestas
            if (line.contains("edge [")) {
                currentSource = -1;
                currentTarget = -1;
                currentName = "";
            } else if (line.startsWith("source ")) {
                currentSource = Integer.parseInt(line.split("\\s+")[1]);
            } else if (line.startsWith("target ")) {
                currentTarget = Integer.parseInt(line.split("\\s+")[1]);
            } else if (line.startsWith("name ")) {
                 int start = line.indexOf('"');
                 int end = line.lastIndexOf('"');
                 if (start != -1 && end != -1 && end > start) {
                     // Extrai e limpa o nome da rua.
                     currentName = line.substring(start + 1, end)
                                .replace("&#243;", "ó")
                                .replace("&#237;", "í")
                                .replace("&#227;", "ã")
                                .replace("&#225;", "á")
                                .replace("&#224;", "à")
                                .replace("&#231;", "ç")
                                .replace("&#250;", "ú")
                                .replace("&#245;", "õ")
                                .replace("&#233;", "é")
                                .replace("&#226;", "â")
                                .replace("&#225;", "á")
                                .replace("&#250;", "ú")
                                .replace("&#237;", "í")
                                .replace("&#244;", "ô")
                                .replace("\\u00e1", "á")
                                .replace("\\u00f4", "ô");
                 }
            } else if (line.startsWith("]") && currentSource != -1 && currentTarget != -1) {
                tempSources.add(currentSource);
                tempTargets.add(currentTarget);
                tempNames.add(currentName);
                
                currentSource = -1;
                currentTarget = -1;
            }
        }
        br.close();
        
        // 1. Determina o número total de nós.
        int nodesCount = gmlNodeIds.isEmpty() ? 0 : Collections.max(gmlNodeIds) + 1;

        // 2. Cria o objeto Graph com o número de nós.
        Graph g = new Graph(nodesCount);

        // 3. Adiciona as arestas ao grafo.
        int minSize = tempSources.size();
        for (int i = 0; i < minSize; i++) {
            int gmlSource = tempSources.get(i);
            int gmlTarget = tempTargets.get(i);
            String name = tempNames.get(i);

            // Mapeia GML ID (0-based) para o seu nó (1-based, como esperado pelo seu Dijkstra)
            int mappedSource = gmlSource + 1;
            int mappedTarget = gmlTarget + 1;
            
            // Adiciona aresta com peso 1 (fixo) e o nome extraído.
            g.edges.add(new Edge(mappedSource, mappedTarget, 1, name));
            g.quantity++; 
        }

        return g;
    }

    


    // ----- calcula o peso total do grafo -----
    public int getTotalWeight() {
        int sum = 0;
        for (Edge e : edges) sum += e.getWeight();
        return sum;
    }


    //----- verifica se o grafo é um Directed Acyclic Graph -----
    public boolean isDAG() {
        int n = this.nodesNum;
        boolean[] visited = new boolean[n + 1];
        boolean[] recStack = new boolean[n + 1];

        for (int i = 1; i <= n; i++)
            if (dfsCycle(i, visited, recStack)) return false;

        return true;
    }

    // ----- verifica a ciclicidade do grado -----
    private boolean dfsCycle(int node, boolean[] visited, boolean[] recStack) {
        if (recStack[node]) return true;
        if (visited[node]) return false;

        visited[node] = true;
        recStack[node] = true;

        for (Edge e : this.edges) {
            if (e.getNode1() == node) {
                if (dfsCycle(e.getNode2(), visited, recStack)) return true;
            }
        }

        recStack[node] = false;
        return false;
    }
   
    // ----- Dijkstra padrão (versão requerida) -----
    public void dijkstra(int origin) {

        int n = this.nodesNum;

        // nodeTable[i][0] = visitado
        // nodeTable[i][1] = distancia
        int[][] nodeTable = new int[n + 1][2];
        int[] parent = new int[n + 1];

        // inicializa
        for (int i = 1; i <= n; i++) {
            nodeTable[i][1] = Integer.MAX_VALUE;
            nodeTable[i][0] = 0;
            parent[i] = -1;
        }

        nodeTable[origin][1] = 0;
        parent[origin] = origin;

        for (int i = 1; i <= n; i++) {

            int u = -1;
            int mindist = Integer.MAX_VALUE;

            // escolhe nó não visitado com menor distância
            for (int j = 1; j <= n; j++) {
                if (nodeTable[j][0] == 0 && nodeTable[j][1] < mindist) {
                    mindist = nodeTable[j][1];
                    u = j;
                }
            }

            if (u == -1)
                break;

            nodeTable[u][0] = 1;

            // relaxamento
            for (Edge e : edges) {
                if (e.getNode1() == u) {

                    int v = e.getNode2();
                    int w = e.getWeight();

                    if (nodeTable[u][1] != Integer.MAX_VALUE &&
                        nodeTable[u][1] + w < nodeTable[v][1]) {

                        nodeTable[v][1] = nodeTable[u][1] + w;
                        parent[v] = u;
                    }
                }
            }
        }

        // imprime resultado
        System.out.println("Distance from node " + origin + ":");
        for (int i = 1; i <= n; i++) {

            if (nodeTable[i][1] == Integer.MAX_VALUE) {
                System.out.println("Node " + i + " = INF");
            } else {

                System.out.print("Node " + i + " = " + nodeTable[i][1] + " | Path: ");

                //caminho invertido
                ArrayList<Integer> path = new ArrayList<>();
                int curr = i;

                while (curr != origin) {
                    if (curr == -1) break;
                    path.add(curr);
                    curr = parent[curr];
                }
                path.add(origin);

                // imprime na ordem correta
                for (int k = path.size() - 1; k >= 0; k--) {
                    System.out.print(path.get(k));
                    if (k > 0) System.out.print(" -> ");
                }
                System.out.println();
            }
        }
    }
    
}


