import json
import argparse
from os.path import dirname, join

class Cleaner:
    """Given a violation, it can make it very readable.

    For example from:
        atLeast(1 <RO_0002110> <FBbt_00003972>)(X) :- <auxiliary#NC36>(X)
    to:
        active(X) -> across(X,Y), act(Y)
    """

    def __init__(self):
        with open(join(dirname(__file__),'list_of_words.txt'), 'r') as f:
            self._words = [x.strip() for x in f]
            self._used_words = 0
            self._cache = {}

    def clean(self, violation, prefix):
        j = 0
        atoms = []
        for i in range(3):
            i = violation.find('<', j) + 1
            j = violation.find('>', i)
            atom = violation[i:j]
            if atom not in self._cache:
                self._cache[atom] = str(prefix) + self._words[self._used_words]
                self._used_words += 1
            atoms.append(self._cache[atom])
        return '%10s(X) -> %10s(X,Y), %10s(Y)' % (atoms[2], atoms[0], atoms[1])


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Transform violations (as output by PAGOdA) into a very readable form.')
    parser.add_argument('input', help='json file containing violations')
    args = parser.parse_args()

    cleaner = Cleaner()
    with open(args.input, 'r') as f:
        violations_list_list = json.load(f)['violationClauses']
        clean_rules_list_list = []
        i = 0
        for violations_list in violations_list_list:
            clean_rules_list_list.append(sorted(map(lambda x: cleaner.clean(x, i), violations_list)))
            i += 1
    print json.dumps(clean_rules_list_list, indent=2)
        
