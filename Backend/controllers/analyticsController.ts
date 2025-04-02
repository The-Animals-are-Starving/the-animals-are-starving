import { Request, Response } from "express";
import Log, { ILog } from "../models/Log";
import Pet, { IPet } from "../models/Pet";
import { Types } from "mongoose";
import Household from "../models/Household";

export const getFeedingAnomalies = async (req: Request, res: Response): Promise<void> => {
  try {
    const { householdId } = req.params;
    if (!householdId) {
      res.status(400).json({ message: "householdId is required" });
      return;
    }

    let household = null;
      if (householdId) {
          household = await Household.findById(householdId);
          if (!household) {
              console.log("Household not found")
              res.status(404).json({ message: "Household not found" });
              return;
          }
      }
    

    // Get pets in the household
    const pets: IPet[] = await Pet.find({ householdId: new Types.ObjectId(householdId) });
    if (!pets.length) {
      res.json({ anomalies: [] });
      return;
    }

    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

    // Retrieve all logs for this household from the past week
    const logs: ILog[] = await Log.find({
      householdId: new Types.ObjectId(householdId),
      timestamp: { $gte: oneWeekAgo },
    });

    const anomalies: Array<{
      pet: string;
      largeDeviation: boolean;
      significantlyLate: boolean;
      averageAmount: number;
      feedingCount: number;
    }> = [];

    for (const pet of pets) {
      const petName = pet.name;

      const petLogs = logs.filter(log => log.petName.toString() === petName);
      let largeDeviation = false;
      let significantlyLate = false;
      let totalAmount = 0;

      if (petLogs.length > 0) {
        for (const log of petLogs) {
          totalAmount += log.amount;
        }
        const avgAmount = totalAmount / petLogs.length;
        // deviation threshold of 30% of the average
        const deviationThreshold = avgAmount * 0.3;

        for (const log of petLogs) {
          if (Math.abs(log.amount - avgAmount) > deviationThreshold) {
            largeDeviation = true;
          }

          // Compare log timestamp with scheduled feeding time
          const scheduledTimeStr = pet.feedingTime;
          const [scheduledHour, scheduledMinute] = scheduledTimeStr.split(":").map(Number);
          const scheduledDate = new Date(log.timestamp);
          scheduledDate.setHours(scheduledHour, scheduledMinute, 0, 0);
          // Allow a 30 minute delay threshold
          const lateThreshold = 30 * 60 * 1000;

          if (log.timestamp.getTime() - scheduledDate.getTime() > lateThreshold) {
            significantlyLate = true;
          }
        }

        anomalies.push({
          pet: petName,
          largeDeviation,
          significantlyLate,
          averageAmount: avgAmount,
          feedingCount: petLogs.length,
        });
      } else {
        // If no logs exist for the pet in the past week, assume no anomalies
        anomalies.push({
          pet: petName,
          largeDeviation: false,
          significantlyLate: false,
          averageAmount: 0,
          feedingCount: 0,
        });
      }
    }

    res.json({ anomalies });
  } catch (error) {
    console.error("Error computing feeding anomalies:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};

// Helper function to get the ISO week number of a given date
function getWeekNumber(date: Date): number {
  const d = new Date(date.getTime());
  d.setHours(0, 0, 0, 0);
  // Adjust to nearest Thursday (ISO week rule)
  d.setDate(d.getDate() + 4 - (d.getDay() || 7));
  const yearStart = new Date(d.getFullYear(), 0, 1);
  const weekNo = Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  return weekNo;
}

// Compute user rankings based on feeding contributions using three nested loops
export const getUserRankings = async (req: Request, res: Response): Promise<void> => {
  try {
    const { householdId } = req.params;
    if (!householdId) {
        res.status(400).json({ message: "householdId is required" });
        return;
    }

    // Retrieve all logs for the given household
    const logs: ILog[] = await Log.find({ householdId: new Types.ObjectId(householdId) });
    if (!logs.length) {
        res.json({ userContributions: {} });
        return;
    }

    // Group logs by pet and then by week
    const petWeeklyLogs: { [petName: string]: { [week: number]: ILog[] } } = {};
    logs.forEach(log => {
      const pet = log.petName.toString();
      const week = getWeekNumber(log.timestamp);
      if (!petWeeklyLogs[pet]) {
        petWeeklyLogs[pet] = {};
      }
      if (!petWeeklyLogs[pet][week]) {
        petWeeklyLogs[pet][week] = [];
      }
      petWeeklyLogs[pet][week].push(log);
    });

    // Compute user contributions with three nested loops:
    // Loop 1: Iterate over pets.
    // Loop 2: Iterate over weeks for each pet.
    // Loop 3: Iterate over each log for that week.
    const userContributions: { [userName: string]: { [petName: string]: Array<{ week: number; contribution: number }> } } = {};
    for (const pet in petWeeklyLogs) {
      for (const week in petWeeklyLogs[pet]) {
        const weekNumber = Number(week);
        const weekLogs = petWeeklyLogs[pet][weekNumber];
        const totalFood = weekLogs.reduce((sum, log) => sum + log.amount, 0);
        if (totalFood === 0) continue;

        weekLogs.forEach(log => {
          const contribution = (log.amount / totalFood) * 100;
          const user = log.userName.toString();
          if (!userContributions[user]) {
            userContributions[user] = {};
          }
          if (!userContributions[user][pet]) {
            userContributions[user][pet] = [];
          }
          userContributions[user][pet].push({ week: weekNumber, contribution });
        });
      }
    }

    res.json({ userContributions });
  } catch (error) {
    console.error("Error computing user rankings:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};