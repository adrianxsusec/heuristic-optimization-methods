import math
from typing import List

from src.ant import Ant


def export(ants: List[Ant], instance: int):
    tour_length = 0
    vehicles = len(ants)

    for ant in ants:
        tour_length += ant.get_tour_distance()

    tour_length = round(tour_length, 2)

    # write to file in this format
    # 21
    # 1: 0(0)->53(85)->58(180)->2(200)->0(228)
    # 2: 0(0)->27(27)->28(44)->12(81)->80(168)->77(185)->50(203)->0(230)
    # ...
    # 19: 0(0)->44(59)->38(80)->0(133)
    # 20: 0(0)->39(34)->23(58)->67(80)->0(134)
    # 21: 0(0)->65(50)->0(110)
    # 1836.87

    with open(f'res-1m-i{instance}.txt', 'w') as file:
        file.write(f'{vehicles}\n')
        for i in range(vehicles):
            file.write(f'{i + 1}: ')
            for location in ants[i].get_tour():
                file.write(f'{location[0].id}({math.ceil(location[1])})')
                if location != ants[i].get_tour()[-1]:
                    file.write('->')
            file.write('\n')
        file.write(f'{tour_length}')

