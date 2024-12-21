from src.location import Location


def parse_instance(instance: int) -> tuple[list[Location], Location, int, int]:
    instance_file = f'instances/inst{instance}.txt'
    locations = []
    with open(instance_file, 'r') as file:
        lines = file.readlines()

        vehicles, vehicle_capacity = map(int, lines[2].split())

        depot_info = lines[7].split()
        depot = Location(*[int(info) for info in depot_info])

        for line in lines[8:]:
            location = line.split()
            locations.append(Location(*[int(info) for info in location]))

    return locations, depot, vehicles, vehicle_capacity
