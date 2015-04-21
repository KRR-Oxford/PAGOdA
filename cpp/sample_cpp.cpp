#include<iostream>
#include<fstream>
#include<sstream>
#include<cstdlib>
#include<map>
#include<string>
#include<list>
#include<utility>
#include<set>
#include<stack>
#include<time.h>

using namespace std;

const string rdftype = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
const int MAXN = 100000000;

map<string, int> inverseIndex4Individual;
map<string, int> inverseIndex4Predicate;
string *indexingIndividual;
string *indexingPredicate;
int noOfIndividuals(0), noOfPredicates(0), noOfStatements(0);

list<int> *labels;
list<pair<int,int> > *edges;
bool *visited;

int lineNumber = 0;
pair<int,int> **edges_array; 
int *edges_array_size; 

int getID(string s, bool isPredicate) {
  int ret; 
  if (isPredicate) {
    if (!(ret = inverseIndex4Predicate[s])) {
      inverseIndex4Predicate[s] = (ret = ++noOfPredicates);
      indexingPredicate[ret] = s;
      cout << "new predicate: " << s << " " << ret << endl;
    }
  }
  else {
    if (!(ret = inverseIndex4Individual[s])) {
      inverseIndex4Individual[s] = (ret = ++noOfIndividuals);
      indexingIndividual[ret] = s;
    }
  }
  return ret; 
}

void addTriple(string s, string p, string o) {
  //cout << s << " " << p << " " << o << endl;
  if (!p.compare(rdftype)) {
    int A(getID(o, true)), c(getID(s, false));
    labels[c].push_back(A);
  }
  else {
    int r(getID(p, true)), a(getID(s, false)), b(getID(o, false));
    //cout << r << " " << a << " " << b << endl;
    //cout << edges[a].size() << endl;
    edges[a].push_back(make_pair(r, b));
  }
  ++noOfStatements;
  if (noOfStatements % 1000000 == 0) {
    cout <<"No.of statments: " << noOfStatements
	 << ", No.of individuals " << noOfIndividuals
	 << ", No.of predicates " << noOfPredicates << endl;
  }
}

ofstream output;

int visit(int id ) {
  int num = 0;
  if (!visited[id]) {
    visited[id] = true;
    for (list<int>::iterator it = labels[id].begin(); it != labels[id].end(); ++it, ++num)
      output << indexingIndividual[id] << " " << rdftype << " " << indexingPredicate[*it] << " ." << endl;
  }
  return num;
}

void printList(int u, list<pair<int, int> >::iterator first, list<pair<int, int> >::iterator last) {
  cout << "-------------" << endl;
  for (; first != last; ++first)
    cout << u << " " << first->first << " " << first->second << endl;
  cout << "-------------" << endl;
}

void sample(int percentage) {
  stringstream ss;
  ss << "sample_" << percentage << ".nt";
  string outputName;
  ss >> outputName;

  int noOfRestIndividuals = noOfIndividuals;
  int *restIndividualsArray = new int[noOfIndividuals];
  int *number2index = new int[noOfIndividuals + 1];
  for (int i = 0, j = 1; i < noOfIndividuals; ++i, ++j) {
    restIndividualsArray[i] = j;
    number2index[j] = i;
  }

  cout << "The sampled data is going to save in " << outputName << endl;
  cout << "The number of individuals: " << noOfIndividuals << endl;

  output.open(outputName.c_str(), ofstream::out);
  int limit(noOfStatements * percentage / 100), choosen(0);

  bool flag;
  stack<int> s;
  int p_from, p_to; 
  int u, v, individualIndex, t, pick, len;
  while (true) {
    if (choosen >= limit) break;

    if (s.empty()) {
      //cout << noOfIndividuals << " " << noOfRestIndividuals << endl;
      s.push(u = restIndividualsArray[rand() % noOfRestIndividuals]);
      choosen += visit(u);
      //cout << "A new start " << indexing[u] << endl;
    }
    u = s.top();
    //cout << "The current element is :" << indexing[u] << endl;
    //if (choosen % 1000000 == 0)
    //cout << "No of choosen statement: "  << choosen << " No of rest Individuals: " << noOfRestIndividuals << endl;

    if (rand() % 100 < 15) {
      //cout << "back to its father" << endl;
      s.pop();
      continue;
    }

    //cout << "outgoing edges: " << edges_array_size[u] << endl;
    if (!edges_array_size[u]) {
      //cout << "empty node" << endl;
      while (!s.empty()) s.pop();
      t = number2index[u];
      if (t == -1) continue;
      --noOfRestIndividuals;
      //cout << indexing[u] << " has been removed with current index " << index << " and number " << u << endl;
      v = restIndividualsArray[noOfRestIndividuals];
      restIndividualsArray[t] = v;
      number2index[v] = t;
      number2index[u] = -1;
      continue;
    }

    pick = rand() % edges_array_size[u]; 
    if (pick < 0) pick += edges_array_size[u]; 
    p_from = edges_array[u][pick].first; 
    p_to = edges_array[u][pick].second;
    len = --edges_array_size[u]; 
    edges_array[u][pick].first = edges_array[u][len].first;
    edges_array[u][pick].second = edges_array[u][len].second;
    s.push(p_to);
    choosen += visit(p_to) + 1;
    if (!indexingPredicate[p_from].size()) {
      cout << indexingIndividual[u] << " " << indexingPredicate[p_from] << " " << indexingIndividual[p_to] << " ." << endl;
      return ;
    }
    output << indexingIndividual[u] << " " << indexingPredicate[p_from] << " " << indexingIndividual[p_to] << " ." << endl;
  }

  delete[] restIndividualsArray;
  delete[] number2index;
  cout << "The number of statements: " << noOfStatements << " (choosen: " << choosen << ")" << endl;
  cout << "The number of rest Individuals: " << noOfRestIndividuals << endl;
  output.close();
}

int main(int argc, char **argv) {
  if (argc < 3) return 0;
  cout << "Trying to open the file " << argv[1] << endl;
  cout << "Get " << argv[2] << "/100 of the file" << endl;

  int startLine(-1);
  istringstream iss;
  if (argc > 3) {
    iss.str(argv[3]);
    iss >> startLine;
    iss.clear();
  }
  cout << "Problematic line starts at " << startLine << endl;

  ifstream input;
  input.open(argv[1]);
  if (!input) {
    cout << "No such file" << endl;
  }
  cout << "The input file is " << argv[1] << endl;

  int number, index(0);
  string subject, predicate, object;

  clock_t start = clock();
  cout << "Starting constructing the graph..." << endl;
  indexingIndividual = new string[MAXN];
  indexingPredicate = new string[MAXN];
  labels = new list<int>[MAXN];
  edges = new list<pair<int, int> >[MAXN]; 
  cout << "mem alloc time: " <<  ((float) clock() - (float) start) / CLOCKS_PER_SEC << " sec" << endl; 

  int noOfInvalidLines = 6; 
  int invalidLines[] = { 700761, 703992, 750181, 750183, 750185, 750315};
  int point(startLine == -1 ? noOfInvalidLines : 0);
  for (int i = point; i < noOfInvalidLines; ++i)
    // 288884361
    invalidLines[i] = startLine + (invalidLines[i] - 700761) - 1;

  string line, nextLine;
  while (!input.eof()) {
    if (input >> subject >> predicate); else break; 
    getline(input, object);
    ++lineNumber;
    //cout << lineNumber << " " << subject << " " << predicate << " \"" << object << "\"" << endl;

    object.erase(0, 1);
    if (point < noOfInvalidLines && lineNumber == invalidLines[point]) {
      getline(input, nextLine);
      ++lineNumber;
      ++point;
      //cout << lineNumber << endl << object << endl << " " << nextLine << endl;
      object.replace(object.size() - 1, 1, nextLine);
      //cout << object << endl;
    }

    object.erase(object.size() - 2, 2);

    //cout << lineNumber << " " << subject << " " << predicate << " \"" << object << "\"" << endl;
    addTriple(subject, predicate, object);
    //cout << "Triple added!" << endl;
  }
  cout << lineNumber << " " << subject << " " << predicate << " " << object << endl;

  input.close();

  cout << "Graph constructed!" << endl;
  float constructionTime(((float) clock() - (float) start) / CLOCKS_PER_SEC / 60);
  cout << "Construction time: " << constructionTime << " min" << endl;
  //return 0;

  edges_array = new pair<int,int>* [noOfIndividuals + 1]; 
  edges_array_size = new int[noOfIndividuals + 1]; 
  for (int i = 1, j = 0; i <= noOfIndividuals; ++i) {
    edges_array_size[i] = edges[i].size(); 
    edges_array[i] = new pair<int,int>[edges_array_size[i]]; 
    j = 0; 
    for (list<pair<int,int> >::iterator it = edges[i].begin(); it != edges[i].end(); ++it, ++j) {
      edges_array[i][j] = *it;
    }
  }
  
  delete[] edges; 
  constructionTime = ((float) clock() - (float) start) / CLOCKS_PER_SEC / 60;
  cout << "Rearrange done: " << constructionTime << " min" << endl;

  visited = new bool[noOfIndividuals + 1];
  for (int i = 1; i < noOfIndividuals + 1; ++i)
    visited[i] = 0;

  int percentage;

  srand(19900114);
  if (argc > 2) {
    iss.str(argv[2]);
    iss >> percentage;
    iss.clear();
  }
  else percentage = 1;

  if (percentage == 0) return 0;
  sample(percentage);

  cout << "Success!" << endl;
  float totalTime(((float) clock() - (float) start) / CLOCKS_PER_SEC / 60);
  cout << "Total time: " <<  totalTime << " (Sample time: " << totalTime - constructionTime << ") min" << endl;

  delete[] indexingIndividual; 
  delete[] visited; 
  delete[] labels; 

  for (int i = 1; i <= noOfIndividuals; ++i)
    delete[] edges_array[i]; 

  delete[] edges_array; 

  return 0;
}
