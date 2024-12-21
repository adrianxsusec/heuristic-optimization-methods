import time

from src.aco import ACOAlgorithm
from src.util.export import export
from src.util.parser import parse_instance

if __name__ == "__main__":
    start_time = time.time()

    instance = 3

    instances = parse_instance(instance)

    locations = instances[0]
    depot = instances[1]
    vehicles = instances[2]
    vehicle_capacity = instances[3]

    aco = ACOAlgorithm(1, 3, 0.2, 300, 0.1, 3)
    solution = aco.run(locations, depot, vehicles, vehicle_capacity)

    export(solution, instance)

    end_time = time.time()
    print(f"Time taken: {end_time - start_time}")
