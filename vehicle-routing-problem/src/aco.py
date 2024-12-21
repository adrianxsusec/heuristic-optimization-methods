import copy

import numpy as np

from src.ant import Ant
from src.location import Location


def distance_matrix(locations: list[Location]) -> np.ndarray:
    distances = np.zeros((len(locations), len(locations)))
    for i in range(len(locations)):
        for j in range(len(locations)):
            distances[i][j] = locations[i].distance(locations[j])
    return distances


class ACOAlgorithm:
    def __init__(self, alpha, beta, evaporation_rate, iterations, pheromone_init, ants_per_iteration):
        self.alpha = alpha
        self.beta = beta
        self.evaporation_rate = evaporation_rate
        self.iterations = iterations
        self.pheromone_init = pheromone_init
        self.ants_per_iteration = ants_per_iteration

    def run(self, locations: list[Location], depot: Location, vehicles: int, vehicle_capacity: int) -> list[Ant]:
        distances = distance_matrix(locations)

        incumbent_solution: list[Ant] = []

        pheromones = np.full((len(locations) + 1, len(locations) + 1), self.pheromone_init)

        for i in range(self.iterations):
            print(f'Iteration: {i}')

            unvisited_locations = set(locations)

            best_ants = []
            for j in range(self.ants_per_iteration):
                unvisited_ids = set([location.id for location in locations])
                ants = []
                while unvisited_ids:
                    ant = Ant(vehicle_capacity)

                    current_location: Location = depot
                    current_time = 0

                    ant.add_location(current_location, current_time)

                    while unvisited_ids:
                        next_location = self.select_next_location(locations, current_location, unvisited_ids,
                                                                  pheromones)

                        next_time_start = current_time + current_location.distance(next_location)

                        if next_location.demand > ant.get_capacity():
                            break

                        if next_time_start > next_location.due_time:
                            break

                        if next_time_start <= next_location.ready_time:
                            next_time_start = next_location.ready_time

                        next_time_end = next_time_start + next_location.service_time

                        ant.add_location(next_location, next_time_start)
                        ant.capacity -= next_location.demand
                        current_location = next_location
                        current_time = next_time_end + 1

                        unvisited_ids.remove(next_location.id)

                    ant.add_location(depot, current_time + current_location.distance(depot))

                    ants.append(ant)
                    # print(f'Ant completed route with {len(ant.get_tour())} locations and {ant.get_tour_demand()} demand')

                if not best_ants or len(ants) <= len(best_ants):
                    best_ants = ants

            if incumbent_solution == [] or len(best_ants) < len(incumbent_solution):
                incumbent_solution = best_ants

            pheromones *= self.evaporation_rate
            self.update_pheromones(pheromones, best_ants)

            print(f'Incumbent solution has {len(incumbent_solution)} routes')
            print("------------------------")

        return incumbent_solution

    def select_next_location(self, locations, current_location, unvisited_ids, pheromones) -> Location:
        probabilities = []

        for location_id in unvisited_ids:
            probabilities.append(self.probability(current_location, locations[location_id - 1], pheromones))

        probabilities = np.array(probabilities)
        probabilities /= probabilities.sum()

        next_location_id = np.random.choice(list(unvisited_ids), p=probabilities)
        return locations[next_location_id - 1]

    def probability(self, current_location, location, pheromones):
        return pheromones[current_location.id][location.id] ** self.alpha * \
            (1 / current_location.distance(location)) ** self.beta

    @staticmethod
    def update_pheromones(pheromones, ants):
        for ant in ants:
            tour = ant.get_tour()
            for i in range(len(tour) - 1):
                pheromones[tour[i][0].id][tour[i + 1][0].id] += 1 / ant.get_tour_distance()
                pheromones[tour[i + 1][0].id][tour[i][0].id] += 1 / ant.get_tour_distance()
        return pheromones
