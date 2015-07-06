import sys
import numpy as np
import matplotlib.pyplot as plt
import json

def main(input_filename_1, input_filename_2):
    
    with open(input_filename_1, 'r') as f:
        data_1 = json.load(f)

    with open(input_filename_2, 'r') as f:
        data_2 = json.load(f)

    times_1 = [(x, data_1[x]['totalTime']) for x in sorted([y for y in data_1])]
    times_2 = [(x, data_2[x]['totalTime']) for x in sorted([y for y in data_2])]
        
    x = np.array(zip(*times_1)[0])
    y_1 = np.array(zip(*times_1)[1])
    y_2 = np.array(zip(*times_2)[1])
    colors_1 = '#eeefff'
    colors_2 = '#11ee55'

    plt.scatter(x, y_1, c=colors_1, marker=',')
    plt.scatter(x, y_2, c=colors_2, marker='v')
    plt.show()


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
