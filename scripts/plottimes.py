import sys
import numpy as np
import matplotlib.pyplot as plt
import json

MISSING_VALUE = -100

def fill(v):
    i = 0
    while i < len(v)-1:
        if v[i][0]+1 != v[i+1][0]:
            v = v[:i+1] + [(v[i][0]+1, None)] + v[i+1:]
        i += 1
    return v


def get_list_from_data(data):
    return sorted([(int(x), float(data[x]['totalTime'])) for x in [y for y in data]])

def main(input_filename_1, input_filename_2):
    
    with open(input_filename_1, 'r') as f:
        data_1 = json.load(f)

    with open(input_filename_2, 'r') as f:
        data_2 = json.load(f)

    max_value = max([int(x) for x in data_1] + [int(x) for x in data_2])

    times_1 = fill(get_list_from_data(data_1))
    times_2 = fill(get_list_from_data(data_2))

    x = np.array(zip(*times_1)[0])
    y_1 = np.array(zip(*times_1)[1])
    y_2 = np.array(zip(*times_2)[1])
    colors_1 = '#eeefff'
    colors_2 = '#11ee55'

 #   plt.axvline([i for i in range(max_value)], color='k', linestyle='solid')
    plt.scatter(x, y_1, c=colors_1, marker=',', s=50)
    plt.scatter(x, y_2, c=colors_2, marker='v', s=50)
    plt.xticks(np.arange(1, 21, 1.0))
    plt.grid()
    plt.show()


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])