package nathan;

import java.util.ArrayList;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;

	static Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };
	static RobotType[] spawnedByMiner = { RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
			RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN };

	static int turnCount;

	// HQ
	static int numMiners = 0;

	// MINER
	static MapLocation hqLoc;
	static MapLocation soupLoc;
	static MapLocation[][] visionCircles = { { l(0, 0) }, { l(-1, 0), l(0, -1), l(1, 0), l(0, 1) },
			{ l(-2, -1), l(-2, 0), l(-2, 1), l(-1, 1), l(-1, 2), l(0, 2), l(1, 2), l(1, 1), l(2, 1), l(2, 0), l(2, -1),
					l(1, -1), l(1, -2), l(0, -2), l(-1, -2), l(-1, -1) },
			{ l(-3, -2), l(-3, -1), l(-3, 0), l(-3, 1), l(-3, 2), l(-2, 2), l(-2, 3), l(-1, 3), l(0, 3), l(1, 3),
					l(2, 3), l(2, 2), l(3, 2), l(3, 1), l(3, 0), l(3, -1), l(3, -2), l(2, -2), l(2, -3), l(1, -3),
					l(0, -3), l(-1, -3), l(-2, -3), l(-2, -2) },
			{ l(-4, -2), l(-4, -1), l(-4, 0), l(-4, 1), l(-4, 2), l(-3, 3), l(-2, 4), l(-1, 4), l(0, 4), l(1, 4),
					l(2, 4), l(3, 3), l(4, 2), l(4, 1), l(4, 0), l(4, -1), l(4, -2), l(3, -3), l(2, -4), l(1, -4),
					l(0, -4), l(-1, -4), l(-2, -4), l(-3, -3) },
			{ l(-5, -3), l(-5, -2), l(-5, -1), l(-5, 0), l(-5, 1), l(-5, 2), l(-5, 3), l(-4, 3), l(-4, 4), l(-3, 4),
					l(-3, 5), l(-2, 5), l(-1, 5), l(0, 5), l(1, 5), l(2, 5), l(3, 5), l(3, 4), l(4, 4), l(4, 3),
					l(5, 3), l(5, 2), l(5, 1), l(5, 0), l(5, -1), l(5, -2), l(5, -3), l(4, -3), l(4, -4), l(3, -4),
					l(3, -5), l(2, -5), l(1, -5), l(0, -5), l(-1, -5), l(-2, -5), l(-3, -5), l(-3, -4), l(-4, -4),
					l(-4, -3) },
			{ l(0, 0) } };

	// REFINERY
	static int refineryCount = 0;
	// VAPORATOR
	static int vaporatorCount = 0;
	// DESIGN_SCHOOL
	static int schoolCount = 0;
	// FULFILLMENT_CENTER
	static int fulfillmentCount = 0;
	// LANDSCAPER
	static int landscaperCount = 0;
	// DELIVERY DRONE
	static int droneCount = 0;
	// NET_GUN
	static int gunCount = 0;
	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) throws GameActionException {

		// This is the RobotController object. You use it to perform actions from this
		// robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;

		turnCount = 0;

		System.out.println("I'm a " + rc.getType() + " and I just got created!");
		while (true) {
			turnCount += 1;
			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
			try {
				// Here, we've separated the controls into a different method for each
				// RobotType.
				// You can add the missing ones or rewrite this into your own control structure.
				switch (rc.getType()) {
				case HQ:
					runHQ();
					break;
				case MINER:
					runMiner();
					break;
				case REFINERY:
					runRefinery();
					break;
				case VAPORATOR:
					runVaporator();
					break;
				case DESIGN_SCHOOL:
					runDesignSchool();
					break;
				case FULFILLMENT_CENTER:
					runFulfillmentCenter();
					break;
				case LANDSCAPER:
					runLandscaper();
					break;
				case DELIVERY_DRONE:
					runDeliveryDrone();
					break;
				case NET_GUN:
					runNetGun();
					break;
				}

				// Clock.yield() makes the robot wait until the next turn, then it will perform
				// this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println(rc.getType() + " Exception");
				e.printStackTrace();
			}
		}
	}

	static void runHQ() throws GameActionException {
		if (numMiners < 6) {
			for (Direction dir : directions)
				if (tryBuild(RobotType.MINER, dir)) {
					numMiners++;
				}
		}
	}

	static void runMiner() throws GameActionException {
		if (hqLoc == null) {
			RobotInfo[] nearbyBots = rc.senseNearbyRobots();
			for (RobotInfo bot : nearbyBots) {
				if (bot.type == RobotType.HQ && bot.team == rc.getTeam()) {
					hqLoc = bot.location;
				}
			}
		}
		// tryBlockchain();
		nearbySoup();
		if (soupLoc != null && rc.canSenseLocation(soupLoc) && rc.senseSoup(soupLoc) <= 0) {
			soupLoc = null;
		}
		System.out.println(soupLoc);
		for (Direction dir : directions)
			tryRefine(dir);
		for (Direction dir : directions) {
			if (tryMine(dir)) {
			}
		}
		tryMove(randomDirection());
		if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit && refineryCount < 1) {
			Direction direction = randomDirection();
			if (rc.canBuildRobot(RobotType.REFINERY, direction)) {
				tryBuild(RobotType.REFINERY, direction);
				refineryCount += 1;
			}
		}
		Direction direction = randomDirection();
		if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, direction)&&schoolCount < 1) {
			tryBuild(RobotType.DESIGN_SCHOOL, direction);
			schoolCount += 1;
		}
		if (soupLoc != null) {
			Direction dirToSoup = rc.getLocation().directionTo(soupLoc);
			tryMove(dirToSoup);
		} else if (tryMove(randomDirection())) {
			
		} else {}
			
			// tryBuild(randomSpawnedByMiner(), randomDirection());
			//for (Direction dir : directions)
				//tryBuild(RobotType.FULFILLMENT_CENTER, dir);

	}

	static void runRefinery() throws GameActionException {
		// System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
	}

	static void runVaporator() throws GameActionException {

	}

	static void runDesignSchool() throws GameActionException {
		Direction dir = randomDirection();
		if(rc.canBuildRobot(RobotType.LANDSCAPER, dir)&&landscaperCount < 1) {
			tryBuild(RobotType.LANDSCAPER, dir);
			landscaperCount += 1;
		}
	}

	static void runFulfillmentCenter() throws GameActionException {
		for (Direction dir : directions)
			tryBuild(RobotType.DELIVERY_DRONE, dir);
	}

	static void runLandscaper() throws GameActionException {
		if (rc.getDirtCarrying() == 0) {
			tryDig();
		}

		MapLocation bestPlaceToBuildWall = null;
		// find best place to build
		if (hqLoc != null) {
			int lowestElevation = 9999999;
			for (Direction dir : directions) {
				MapLocation tileToCheck = hqLoc.add(dir);
				if (rc.getLocation().distanceSquaredTo(tileToCheck) < 4
						&& rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
					if (rc.senseElevation(tileToCheck) < lowestElevation) {
						lowestElevation = rc.senseElevation(tileToCheck);
						bestPlaceToBuildWall = tileToCheck;
					}
				}
			}
		}

		if (Math.random() < 0.4) {
			// build the wall
			if (bestPlaceToBuildWall != null) {
				rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
				System.out.println("building a wall");
			}
		}

		// otherwise try to get to the hq
		if (hqLoc != null) {
			goTo(hqLoc);
		} else {
			tryMove(randomDirection());
		}
	}

	static void runDeliveryDrone() throws GameActionException {
		Team enemy = rc.getTeam().opponent();
		if (!rc.isCurrentlyHoldingUnit()) {
			// See if there are any enemy robots within striking range (distance 1 from
			// lumberjack's radius)
			RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

			if (robots.length > 0) {
				// Pick up a first robot within range
				rc.pickUpUnit(robots[0].getID());
				System.out.println("I picked up " + robots[0].getID() + "!");
			}
		} else {
			// No close robots, so search for robots within sight radius
			tryMove(randomDirection());
		}
	}

	static void runNetGun() throws GameActionException {

	}

	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	/**
	 * Returns a random RobotType spawned by miners.
	 *
	 * @return a random RobotType
	 */
	static RobotType randomSpawnedByMiner() {
		return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
	}

	static boolean tryDig() throws GameActionException {
		Direction dir = randomDirection();
		if (rc.canDigDirt(dir)) {
			rc.digDirt(dir);
			return true;
		}
		return false;
	}

	static boolean tryMove() throws GameActionException {
		for (Direction dir : directions)
			if (tryMove(dir))
				return true;
		return false;
		// MapLocation loc = rc.getLocation();
		// if (loc.x < 10 && loc.x < loc.y)
		// return tryMove(Direction.EAST);
		// else if (loc.x < 10)
		// return tryMove(Direction.SOUTH);
		// else if (loc.x > loc.y)
		// return tryMove(Direction.WEST);
		// else
		// return tryMove(Direction.NORTH);
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {
		// System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " +
		// rc.getCooldownTurns() + " " + rc.canMove(dir));
		if (rc.isReady() && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

	static boolean goTo(Direction dir) throws GameActionException {
		Direction[] toTry = { dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(),
				dir.rotateRight().rotateRight() };
		for (Direction d : toTry) {
			if (tryMove(d))
				return true;
		}
		return false;
	}

	static boolean goTo(MapLocation destination) throws GameActionException {
		return goTo(rc.getLocation().directionTo(destination));
	}

	/**
	 * Attempts to build a given robot in a given direction.
	 *
	 * @param type The type of the robot to build
	 * @param dir  The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			return true;
		} else
			return false;
	}

	/**
	 * Attempts to mine soup in a given direction.
	 *
	 * @param dir The intended direction of mining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMine(Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canMineSoup(dir)) {
			rc.mineSoup(dir);
			return true;
		} else
			return false;
	}

	/**
	 * Attempts to refine soup in a given direction.
	 *
	 * @param dir The intended direction of refining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryRefine(Direction dir) throws GameActionException {
		if (rc.isReady() && rc.canDepositSoup(dir)) {
			rc.depositSoup(dir, rc.getSoupCarrying());
			return true;
		} else
			return false;
	}

	static void tryBlockchain() throws GameActionException {
		if (turnCount < 3) {
			int[] message = new int[7];
			for (int i = 0; i < 7; i++) {
				message[i] = 123;
			}
			if (rc.canSubmitTransaction(message, 10))
				rc.submitTransaction(message, 10);
		}
		// System.out.println(rc.getRoundMessages(turnCount-1));
	}

	/**
	 * 
	 * @return the current radius in tiles
	 * @throws GameActionException
	 */
	static int radiusInTiles() throws GameActionException {
		int radiusInTiles = 1;
		while (rc.canSenseRadiusSquared(radiusInTiles * radiusInTiles)) {
			radiusInTiles++;
		}
		radiusInTiles--;
		return radiusInTiles;
	}

	static void nearbySoup() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		int x = currentLoc.x;
		int y = currentLoc.y;
		int radiusInTiles = radiusInTiles();
		for (int radius = 0; radius <= radiusInTiles; radius++) {
			for (int i = 0; i < visionCircles[radius].length; i++) {
				MapLocation loc = new MapLocation(x + visionCircles[radius][i].x, y + visionCircles[radius][i].y);
				if (rc.canSenseLocation(loc) && rc.senseSoup(loc) > 0) {
					soupLoc = loc;
					return;
				}
			}
		}
	}

	static MapLocation l(int x, int y) {
		return new MapLocation(x, y);
	}
}
