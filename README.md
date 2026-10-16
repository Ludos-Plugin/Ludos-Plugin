# LudosPlugin

## Introduction
Ludos is a Java Plugin for Spigot Minecraft Servers; its aim is to make it as easy as possible for users to play popular minigames with their friends, with the possibility of using custom-built Roles.
Ludos includes extensible Group, World and Teams Management systems along with modular Games and Roles frameworks to ease add-on development. 

## Installation
To build Ludos Plugin, [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) needs to be installed and set up.

### Docker
If you want to quickly test the Plugin, you can use Docker compose to run a compatible Minecraft server.
```docker compose up ludos-server```
Using the `build.bat`/`build.sh` scripts will automatically update the plugin in the server, you only need to reload the plugins in-game to apply changes: `/reload` or `/reload confirm`, depending on the server.


### VSCode
When using VSCode, you can use the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) extension to make development simpler.
If at any point during development, the code analyzer stops working correctly (showing errors that don't actually exist when building, failing to fetch dependencies...), use the `Java: Clean Java Language Server Workspace` command, which will then prompt an editor restart.
This should fix the code analyzer; if not, then check your JDK installation.

## Ingame
If the Plugin install was successful, you should see a message in chat offering a Ludos Guidebook.
The Guidebook is a more user-friendly interface to pick a Role and start Games.
For a more in-depth interface, use the `/ludos` command:
- `/ludos role` for role actions
- `/ludos group` for group actions
- `/ludos game` for game actions

To Start a Game, you need to be in a group `/ludos group create` (`ludos group invite` to invite players to the group), you can then configure the game if you want `/ludos group config <game_id>` and start a game `/ludos game start <game_id>`.
