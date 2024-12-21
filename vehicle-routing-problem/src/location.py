import math


class Location:
    def __init__(self, id, x, y, demand, ready_time, due_time, service_time):
        self.id = id
        self.x = x
        self.y = y
        self.demand = demand
        self.ready_time = ready_time
        self.due_time = due_time
        self.service_time = service_time

    def __str__(self):
        return f'{self.id} {self.x} {self.y} {self.demand} {self.ready_time} {self.due_time} {self.service_time}'

    def __eq__(self, other):
        return self.id == other.id

    def __hash__(self):
        return hash(self.id)

    def distance(self, other) -> float:
        return math.sqrt((self.x - other.x)**2 + (self.y - other.y)**2)
