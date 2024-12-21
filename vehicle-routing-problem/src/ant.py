from src.location import Location


class Ant:
    def __init__(self, capacity: int):
        self.tour: list[tuple[Location, int]] = []
        self.capacity = capacity

    def add_location(self, location: Location, time: int):
        self.tour.append((location, time))

    def get_tour(self) -> list[tuple[Location, int]]:
        return self.tour

    def get_number_of_locations(self) -> int:
        return len(self.tour)

    def get_tour_distance(self) -> float:
        distance = 0
        for i in range(len(self.tour) - 1):
            distance += self.tour[i][0].distance(self.tour[i + 1][0])
        return distance

    def get_capacity(self) -> int:
        return self.capacity

    def get_tour_time(self) -> int:
        return self.tour[-1][1]

    def get_tour_demand(self) -> int:
        return sum([location[0].demand for location in self.tour])
