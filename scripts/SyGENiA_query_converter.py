import sys
import os
from os.path import join
import re
import argparse
import random


# example query
# Q(?0)<-takesCourse(?0,?1), Course(?1)

# example namespace_pairs
# unibench:http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl


var_map = {'0': 'x', '1': 'y', '2': 'z', '3':'u', '4': 'v', '5': 'w'}
atom_regex = '(?P<name>[A-Za-z]+)\(((?P<vars>\?[0-9]+(,\?[0-9]+)*))\)'


def get_var(numeric_var, answer_vars, blank_pct=0):
    """Given a numeric var (e.g. ?1),
       it returns a var (e.g. ?x) or a blank node (e.g. _:x).
    """

    blank_flag = random.random() < (float(blank_pct) / 100)
    var = '?' + var_map[numeric_var[1:]]
    if blank_flag and var not in answer_vars:
        return '_:' + var_map[numeric_var[1:]]
    else:
        return '?' + var_map[numeric_var[1:]]



def parse_query(query, namespace_pair, query_id, blank_pct=0):
    """Translates a query from FOL notation to SPARQL"""

    namespace_id = namespace_pair[:namespace_pair.find(':')]
    namespace = namespace_pair[namespace_pair.find(':') + 1:]

    head, body = query.split('<-')
    answer_vars = map(lambda x: '?' + var_map[x[1:]], head.strip()[2:-1].split(','))
    body_atoms = map(lambda m: (m.group('name'), m.group('vars')), re.finditer(atom_regex, body))

    triples = []
    var_cache = {}
    for name, atom_vars_str in body_atoms:

        atom_vars = []
        for x in atom_vars_str.split(','):
            if x not in var_cache:
                var_cache[x] = get_var(x, answer_vars, blank_pct)
            atom_vars.append(var_cache[x])

        if len(atom_vars) == 1:
            triples.append((atom_vars[0], 'rdf:type', namespace_id + ':' + name))
        elif len(atom_vars) == 2:
            triples.append((atom_vars[0], namespace_id + ':' + name, atom_vars[1]))
        else:
            raise IOError('Predicated of arity > 2')

    query_text = '^[query%d]\n' % query_id
    query_text += 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n'
    query_text += 'PREFIX ' +namespace_id + ': ' + namespace + '\n'
    query_text += 'SELECT ' + ' '.join(answer_vars) + '\n'
    query_text += 'WHERE {\n'
    for triple in triples:
        query_text += '  ' + ' '.join(triple) + ' .\n'
    query_text = query_text[:-2] + '\n}'

    return query_text


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='Convert queries from FOL notation to SPARQL.')
    parser.add_argument('-b', '--blank', default='0' ,
        help='percentage of vars to be randomly replaced with blank nodes')
    parser.add_argument('namespace',
        help='<id>:<namespace>, that is a colon-separated pair with an id and the namespace for all the individuals in the query')
    parser.add_argument('input', help='<input-dir> or <input-file>, that is an input directory or a single file')
    args = parser.parse_args()

    query_id = 1
    for input_file in os.listdir(args.input):
        if os.path.isfile(join(args.input, input_file)):
            with open(join(args.input, input_file), 'r') as in_file:
                query = in_file.read()
            parsed_query = parse_query(query, args.namespace, query_id, args.blank)
            query_id += 1
            print parsed_query
            print
