JRobots:NextGeneration


North American Public Sector Sales Meeting
Demo Jam 2017
at Kingsmill, VA

Michael Epley
John Osborne
Sept 11, 2017

This demo will modernize CRobots a classic programmer’s game with the latest technologies. 

#History

In the late 1980's programmers created a game for them: programmers could write software robots that battled each other in a virtual arena to see who could write the best algorithms. The original robots were small C programs that could move, 

With the rise of the world wide web a decade later, a new version was created using java-based robots and an arena that could be accessed with nothing but a web browser. This drew interest from hundreds of new programmers eager to test their mettle who created new robots and battled in large-scale tournaments.

Never

### Upsides

* have fun "killing" your friends
* Practice your programming skills
* Learn new algorithmic techniques
* Bragging rights for being on top of the competition

### Downsides

* Required you to create your bot on your own computer
* Limited programming API to prevent cheating and 


# Modernizing



# About The Robots

Robots are autonomous players in the arena, each capable of performing a number of actions, such as moving

### Moving

Robots can move across the arena's field to gain tactical advantage. Robots are limited to a top speed based on the Arena's rules, and a top acceleration based on both the Arena's rules and the robot's mass.

Moving is done for a lot of reasons: maneuvering to fire at an enemy, to triangulate positions of other objects, avoiding incoming fire, or hiding behind other objects.

### Turning

Robots can turn their direction of motion to either change their destination while moving, or to point their fixed weapons in that direction.

### Firing

Robots are equiped with various weapons

## Design

### Use Cases

#### Signing in / Creating a Player

Users should be able to sign in to interact

#### Creating A robot

#### Battling robots

#### Viewing Robot Stats

#### Viewing Total Stats

## Architecture

Openshift is the primary mechanism to Manage all componets

#### User Registration Page

A web page is provided to allows users to register themselves as players and manage their profile.

#### User Bot Page

A web page is provided to allow users to

#### Arena Build

An openshift build config is provided to build the arena componens.

#### Arena 

For each battle, an Arena component is instantiated solely for the purpose of running the battle, then disposed of.  

#### Robot Builder

Components to support the

##### Robot 2SI image

#### Automatic Battle Runner

Automatically arranges and schedules battles between all player's registered bots. These should run constantly in the background for the user to measure the bot's quality against other bots, but be "private" battles, with the results and scores known only the bot author.

#### Battle Runner

On demand scheduling of battles between bots, based on a user's desired battle configuration (participating .

Implementation:
1. web page on frontend app, then invoking Openshift API for deploying the battle container(s).

#### Battle Runner

Automatically arranges and schedules battles between all player's registered bots.

Implementation:
1. web page on frontend app, then invoking Openshift API for deploying the battle container(s).

#### Battle Runner

Automatically arranges and schedules battles between all player's registered bots.

Implementation:
1. web page on frontend app, then invoking Openshift API for deploying the battle container(s).

#### Tournament Runner

Automatically arranges and schedules related series of battles between all player's registered bots. Series may be related by battle type (try all variations of team memberships) or causally (eg. hierarchical elimination).

Implementation:
1. web page on frontend app, then invoking Openshift API for deploying the battle container(s).
2. logic may be too complicated to embed (easily) in javascript web app...maybe managed via REST by a JBoss App or BPMS?

#### Leaderboard

A component to show battle information. Show show for all users the current best bots, player statistics (number of battles, number of bots, etc), as well as results of tournaments and one-off events.

Implementation:
1. web page on frontend app, then invoking Openshift API for deploying the battle container(s).

#### Data Store

Maintains persistent storage for game data with a CRUD data 

Implementation Options:
1. MongoDB returning JSON objects
2. PostgresDB with direct JDBC access
3. PostgresDB with FIS-based REST service access

##### User Data Store

For each player, this ma

##### robot data store

For each robot, this will record the robot metadata such as version (by build id?), name, color, icon, git repo, main class. It will also record the robot's battle statistics such as wins/losses/draws, independently for each type of match (for example one-on-one, doubles, teams, scored/unscored, private/public). 

##### battle data store

For each battle, it will record the battle metadata such as the participating robots and teams, time, results.


# Future Ideas

### ShowBack/ChargeBack

* Use 3Scale to measure the number of battles that are triggered and show the cost back to the individual user(s)

