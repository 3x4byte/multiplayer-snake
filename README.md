# Snake Royale

A multiplayer snake game built as a web engineering project.

## Gameplay

The main difference between the traditional Snake and Snake Royale is that, it is played in lobbies ranging from 
2 to 9 players where apples are shared between all lobby members, meaning that only one player can eat an apple.

Every 30 seconds, the shortest player loses one life. Additionally, the length of all other players' snakes is reduced by the length of the shortest player to keep the gameplay dynamic.

In the sense of a battle roayale game its the players main goal to be the last snake standing, which requires
smart path choices, good reflexes and a bit of luck. 

If two players are tied for the shortest snake or eat an apple at the same time, the effects are applied to both players.

## Getting Started

### Dependencies

- Java (with the path variable set)
- A low latency internet connection (preferably wired)

### Installation

1. Clone the repository and navigate to the root folder
2. Run the gradle commands `gradlew runServer` to build and run the project
3. Open a web browser and navigate to `http://localhost:5000` to play the game

In case you want to build / run the components separately:
- `gradlew build` builds the entire project
- `gradlew runWebsocket` starts the websocket (gameserver)
- `gradlew runWebserver` starts the webserver 

### Stopping the game

+ On **Windows** it should be as simple as closing the command prompt. 
+ On **Mac** and **Linux** you have to execute the provided kill-script via `./kill.sh` from project root to stop the websocket and webserver processes.
  - when you try to kill the processes with `./kill.sh` but it returns **permission denied**, try to change the rights of the kill.sh script with `chmod 777 kill.sh`

## Enjoy the game!
