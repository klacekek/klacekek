"""A simple terminal-based gladiator game inspired by Sword and Sandals 2.

Run with `python sword_and_sandals.py`.
"""
from __future__ import annotations

import random
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Item:
    name: str
    cost: int
    attack_bonus: int = 0
    defense_bonus: int = 0
    health_bonus: int = 0


WEAPONS = [
    Item("Bare Fists", cost=0, attack_bonus=0),
    Item("Rusty Dagger", cost=30, attack_bonus=2),
    Item("Gladius", cost=75, attack_bonus=4),
    Item("Greatsword", cost=130, attack_bonus=6),
]

ARMOURS = [
    Item("Ragged Tunic", cost=0, defense_bonus=0, health_bonus=0),
    Item("Leather Vest", cost=25, defense_bonus=1, health_bonus=5),
    Item("Chainmail", cost=60, defense_bonus=2, health_bonus=10),
    Item("Steel Plate", cost=120, defense_bonus=3, health_bonus=15),
]


@dataclass
class Gladiator:
    name: str
    level: int
    base_health: int
    strength: int
    agility: int
    gold: int = 50
    experience: int = 0
    weapon: Item = field(default_factory=lambda: WEAPONS[0])
    armour: Item = field(default_factory=lambda: ARMOURS[0])
    current_health: Optional[int] = None

    def __post_init__(self) -> None:
        if self.current_health is None:
            self.current_health = self.max_health

    @property
    def max_health(self) -> int:
        return self.base_health + self.armour.health_bonus + self.level * 2

    @property
    def attack(self) -> int:
        return self.strength + self.weapon.attack_bonus + self.level

    @property
    def defense(self) -> int:
        return self.agility + self.armour.defense_bonus + self.level // 2

    def heal_full(self) -> None:
        self.current_health = self.max_health

    def is_alive(self) -> bool:
        return self.current_health is not None and self.current_health > 0

    def take_damage(self, amount: int) -> None:
        if self.current_health is None:
            self.current_health = self.max_health
        self.current_health = max(0, self.current_health - amount)


@dataclass
class BattleResult:
    winner: Gladiator
    loser: Gladiator
    gold_reward: int
    xp_reward: int


def prompt(prompt_text: str, default: Optional[str] = None) -> str:
    response = input(prompt_text).strip()
    if not response and default is not None:
        return default
    return response


def create_player() -> Gladiator:
    print("=== Welcome to the Arena ===")
    name = prompt("Name your gladiator: ", default="Hero")

    while True:
        try:
            strength = int(prompt("Allocate strength (1-10): ", default="5"))
            agility = int(prompt("Allocate agility (1-10): ", default="5"))
        except ValueError:
            print("Please enter whole numbers.")
            continue

        if 1 <= strength <= 10 and 1 <= agility <= 10 and strength + agility <= 14:
            break
        print("Choose values between 1 and 10 with a combined maximum of 14.")

    base_health = 25 + agility * 2
    player = Gladiator(
        name=name or "Hero",
        level=1,
        base_health=base_health,
        strength=strength,
        agility=agility,
    )
    print(f"\n{name} enters the arena!\n")
    return player


def generate_opponent(level: int) -> Gladiator:
    name = random.choice([
        "Brutus",
        "Cassia",
        "Vandal",
        "Titus",
        "Maxima",
        "Mercury",
        "Cyra",
    ])
    strength = random.randint(3, 6) + level
    agility = random.randint(3, 6) + level // 2
    base_health = 30 + level * 3
    weapon = random.choice(WEAPONS[: min(len(WEAPONS), 1 + level)])
    armour = random.choice(ARMOURS[: min(len(ARMOURS), 1 + level)])
    return Gladiator(
        name=name,
        level=level,
        base_health=base_health,
        strength=strength,
        agility=agility,
        gold=random.randint(15, 40),
        weapon=weapon,
        armour=armour,
    )


def calculate_damage(attacker: Gladiator, defender: Gladiator) -> int:
    base = random.randint(attacker.attack // 2, attacker.attack)
    mitigation = random.randint(defender.defense // 3, defender.defense)
    damage = max(1, base - mitigation)
    crit_chance = min(30, attacker.agility * 2)
    if random.randint(1, 100) <= crit_chance:
        print(f"Critical strike by {attacker.name}!")
        damage *= 2
    return damage


def fight(player: Gladiator, opponent: Gladiator) -> BattleResult:
    print(f"\nA new challenger approaches: {opponent.name} (Level {opponent.level})")
    opponent.heal_full()
    player.heal_full()

    turn_order = [player, opponent]
    if opponent.agility > player.agility and random.random() < 0.6:
        turn_order.reverse()

    while player.is_alive() and opponent.is_alive():
        for attacker, defender in (turn_order, turn_order[::-1]):
            if not defender.is_alive():
                continue
            damage = calculate_damage(attacker, defender)
            defender.take_damage(damage)
            print(f"{attacker.name} hits {defender.name} for {damage} damage. ({defender.current_health}/{defender.max_health} HP)")
            if not defender.is_alive():
                print(f"{defender.name} has fallen!")
                winner = attacker
                loser = defender
                break
        else:
            continue
        break

    gold_reward = random.randint(20, 40) + opponent.level * 5
    xp_reward = 20 + opponent.level * 10

    if winner is player:
        player.gold += gold_reward
        player.experience += xp_reward
        level_up_if_ready(player)
    else:
        gold_reward = xp_reward = 0

    return BattleResult(winner=winner, loser=loser, gold_reward=gold_reward, xp_reward=xp_reward)


def level_up_if_ready(gladiator: Gladiator) -> None:
    required_xp = gladiator.level * 60
    while gladiator.experience >= required_xp:
        gladiator.level += 1
        gladiator.base_health += 5
        gladiator.strength += 1
        gladiator.agility += 1
        gladiator.experience -= required_xp
        required_xp = gladiator.level * 60
        print(f"{gladiator.name} has reached level {gladiator.level}!")


def visit_shop(player: Gladiator) -> None:
    while True:
        print("\n=== Marketplace ===")
        print(f"Gold: {player.gold}")
        print("1) Buy weapons")
        print("2) Buy armour")
        print("3) Leave shop")
        choice = prompt("Choose an option: ")

        if choice == "1":
            purchase_item(player, WEAPONS, "weapon")
        elif choice == "2":
            purchase_item(player, ARMOURS, "armour")
        elif choice == "3":
            return
        else:
            print("Invalid option.")


def purchase_item(player: Gladiator, items: list[Item], slot: str) -> None:
    print("Available items:")
    for idx, item in enumerate(items, start=1):
        owned = getattr(player, slot)
        marker = " (equipped)" if owned.name == item.name else ""
        bonuses = []
        if item.attack_bonus:
            bonuses.append(f"+{item.attack_bonus} ATK")
        if item.defense_bonus:
            bonuses.append(f"+{item.defense_bonus} DEF")
        if item.health_bonus:
            bonuses.append(f"+{item.health_bonus} HP")
        bonuses_text = ", ".join(bonuses) if bonuses else "No bonuses"
        print(f"{idx}) {item.name} - {item.cost} gold ({bonuses_text}){marker}")

    try:
        selection = int(prompt("Choose item number (0 to cancel): ", default="0"))
    except ValueError:
        print("Please enter a number.")
        return

    if selection == 0:
        return

    if 1 <= selection <= len(items):
        item = items[selection - 1]
        if player.gold < item.cost:
            print("You can't afford that.")
            return
        player.gold -= item.cost
        setattr(player, slot, item)
        print(f"You equipped {item.name}!")
    else:
        print("Invalid selection.")


def main() -> None:
    player = create_player()
    opponents_defeated = 0

    while player.is_alive():
        print("\n=== Town Hub ===")
        print("1) Enter the arena")
        print("2) Visit marketplace")
        print("3) View gladiator stats")
        print("4) Retire")
        choice = prompt("What would you like to do? ")

        if choice == "1":
            opponent_level = max(1, player.level + opponents_defeated // 2)
            opponent = generate_opponent(opponent_level)
            result = fight(player, opponent)
            if result.winner is player:
                opponents_defeated += 1
                print(
                    f"Victory! Earned {result.gold_reward} gold and {result.xp_reward} experience."
                )
            else:
                print("You have been defeated. Your name will be remembered in the arena.")
                break
        elif choice == "2":
            visit_shop(player)
        elif choice == "3":
            print_gladiator_stats(player, opponents_defeated)
        elif choice == "4":
            print("You retire from the arena. Farewell!")
            break
        else:
            print("Invalid option, try again.")

    print("Thanks for playing!")


def print_gladiator_stats(player: Gladiator, victories: int) -> None:
    print("\n=== Gladiator Sheet ===")
    print(f"Name: {player.name}")
    print(f"Level: {player.level}")
    print(f"Experience: {player.experience}")
    print(f"Victories: {victories}")
    print(f"Gold: {player.gold}")
    print(f"Health: {player.current_health}/{player.max_health}")
    print(f"Strength: {player.strength}")
    print(f"Agility: {player.agility}")
    print(f"Weapon: {player.weapon.name}")
    print(f"Armour: {player.armour.name}")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nYou leave the arena abruptly. Farewell!")
