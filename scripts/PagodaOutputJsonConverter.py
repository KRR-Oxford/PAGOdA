import sys
import re
import json


queryID_regex = '^\s*-+\s*[qQ]uery\s*(\d+)\s*-+\s*$'


def main(args):
    records = []

    query_found = text_found = variables_found = answers_found = False
    cur_record = {}
    answers = []
    with open(args[0], 'r') as input_file:
        for line in input_file:
            if not query_found:
                match = re.search(queryID_regex, line)
                if not match:
                    continue
                query_found = True
                # print 'query found'
                cur_record['queryID'] = int(match.group(1))
                continue
            if not text_found:
                # print 'text found'
                cur_record['queryText'] = line.strip()
                text_found = True
                continue
            if not variables_found:
                # print 'vars found'
                cur_record['answerVariables'] = line.strip().split()
                variables_found = True
                continue
            if not answers_found:
                # print 'answers found'
                answers_found = True
                continue

            if len(line.strip()) > 0:
                answers.append(line.strip())
            else:
                cur_record['answers'] = answers
                records.append(cur_record)
                print '\rParsed ' + str(len(records)) + ' query records',

                query_found = text_found = variables_found = answers_found = False
                cur_record = {}
                answers = []
        print

    with open(args[1], 'w') as output_file:
        output_file.write(json.dumps(records, indent=2))


if __name__ == '__main__':
    main(sys.argv[1:])